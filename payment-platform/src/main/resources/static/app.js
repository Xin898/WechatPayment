(function () {
  "use strict";

  var isPages = location.hostname.endsWith("github.io") || location.protocol === "file:";
  var currentPayment = null;
  var browserPayments = {};
  var browserKeys = {};
  var browserOutbox = [];
  var browserWebhooks = [];
  var browserLedger = [];
  var currentRefunds = [];

  var form = document.getElementById("paymentForm");
  var orderInput = document.getElementById("orderId");
  var amountInput = document.getElementById("amount");
  var currencyInput = document.getElementById("currency");
  var keyInput = document.getElementById("idempotencyKey");
  var confirmButton = document.getElementById("confirmButton");
  var refundButton = document.getElementById("refundButton");
  var modeBadge = document.getElementById("modeBadge");

  function randomToken(prefix) { return prefix + "-" + Math.random().toString(16).slice(2, 10); }
  function now() { return new Date().toISOString(); }
  function initializeFields() {
    var token = Date.now().toString().slice(-8);
    orderInput.value = "ORDER-" + token;
    keyInput.value = "idem-" + token;
    document.getElementById("refundKey").value = "refund-" + token;
  }
  function applySuffix(suffix) {
    var raw = Math.max(100, Number(amountInput.value) || 100);
    amountInput.value = Math.floor(raw / 100) * 100 + Number(suffix);
  }

  document.querySelectorAll(".scenario").forEach(function (button) {
    button.addEventListener("click", function () {
      document.querySelectorAll(".scenario").forEach(function (item) { item.classList.remove("active"); });
      button.classList.add("active"); applySuffix(button.dataset.suffix);
    });
  });
  form.addEventListener("submit", function (event) { event.preventDefault(); runAction(createPayment); });
  confirmButton.addEventListener("click", function () { runAction(confirmPayment); });
  refundButton.addEventListener("click", function () { runAction(createRefund); });
  document.getElementById("refreshOpsButton").addEventListener("click", function () { runAction(refreshOperations); });
  document.getElementById("resetButton").addEventListener("click", reset);

  function reset() {
    currentPayment = null; currentRefunds = []; browserPayments = {}; browserKeys = {};
    browserOutbox = []; browserWebhooks = []; browserLedger = [];
    initializeFields(); amountInput.value = "129900";
    document.querySelectorAll(".scenario").forEach(function (item, index) { item.classList.toggle("active", index === 0); });
    renderPayment(null); renderEvents([]); renderOperations([], [], []); renderResponse({});
  }

  async function runAction(action) {
    setBusy(true);
    try { await action(); }
    catch (error) { renderResponse(error.payload || { code: "client_error", message: error.message }); alert(error.message); }
    finally { setBusy(false); }
  }

  async function createPayment() {
    var request = { merchantOrderId: orderInput.value, amount: Number(amountInput.value), currency: currencyInput.value };
    currentPayment = isPages ? browserCreate(keyInput.value, request) : await api("/v1/payment-intents", {
      method: "POST", headers: { "Idempotency-Key": keyInput.value }, body: JSON.stringify(request)
    });
    renderPayment(currentPayment); await refreshEvents(); await refreshOperations();
  }

  async function confirmPayment() {
    if (!currentPayment) return;
    currentPayment = isPages ? browserConfirm(currentPayment.id) : await api("/v1/payment-intents/" + currentPayment.id + "/confirm", { method: "POST" });
    renderPayment(currentPayment); await refreshEvents(); await refreshOperations();
  }

  async function createRefund() {
    if (!currentPayment) return;
    var amount = Number(document.getElementById("refundAmount").value);
    var key = document.getElementById("refundKey").value;
    var refund = isPages ? browserRefund(currentPayment.id, key, amount) : await api("/v1/payment-intents/" + currentPayment.id + "/refunds", {
      method: "POST", headers: { "Idempotency-Key": key }, body: JSON.stringify({ amount: amount })
    });
    if (!isPages) {
      currentPayment = await api("/v1/payment-intents/" + currentPayment.id);
      currentRefunds = await api("/v1/payment-intents/" + currentPayment.id + "/refunds");
    } else currentRefunds.push(refund);
    document.getElementById("refundKey").value = "refund-" + Date.now().toString().slice(-8);
    renderPayment(currentPayment); await refreshEvents(); await refreshOperations(); renderResponse(refund);
  }

  async function refreshEvents() {
    if (!currentPayment) return renderEvents([]);
    renderEvents(isPages ? browserPayments[currentPayment.id].events : await api("/v1/payment-intents/" + currentPayment.id + "/events"));
  }

  async function refreshOperations() {
    if (isPages) return renderOperations(browserLedger, browserOutbox, browserWebhooks);
    if (!currentPayment) return renderOperations([], await api("/v1/operations/outbox"), await api("/v1/operations/webhooks"));
    var references = [currentPayment.id].concat(currentRefunds.map(function (refund) { return refund.id; }));
    var ledgerGroups = await Promise.all(references.map(function (id) { return api("/v1/ledger/transactions?referenceId=" + id); }));
    renderOperations([].concat.apply([], ledgerGroups), await api("/v1/operations/outbox"), await api("/v1/operations/webhooks"));
  }

  async function api(path, options) {
    var opts = Object.assign({}, options || {});
    opts.headers = Object.assign({ "Content-Type": "application/json" }, (options && options.headers) || {});
    var response = await fetch(path, opts); var payload = await response.json(); renderResponse(payload);
    if (!response.ok) { var error = new Error(payload.message || "API request failed"); error.payload = payload; throw error; }
    return payload;
  }

  function browserCreate(key, request) {
    var fingerprint = JSON.stringify(request);
    if (browserKeys[key]) {
      if (browserKeys[key].fingerprint !== fingerprint) throwPayload("idempotency_conflict", "Idempotency key was already used with different parameters");
      return browserKeys[key].payment;
    }
    var payment = { id: crypto.randomUUID ? crypto.randomUUID() : randomToken("pay"), merchantOrderId: request.merchantOrderId,
      amount: request.amount, refundedAmount: 0, currency: request.currency, status: "REQUIRES_CONFIRMATION", providerReference: null,
      failureCode: null, createdAt: now(), updatedAt: now() };
    browserPayments[payment.id] = { payment: payment, events: [eventItem("payment.created", "Payment intent created and awaiting confirmation")], refundKeys: {} };
    browserKeys[key] = { fingerprint: fingerprint, payment: payment }; renderResponse(payment); return payment;
  }

  function browserConfirm(id) {
    var record = browserPayments[id]; if (!record) throwPayload("payment_not_found", "Payment intent not found");
    var payment = record.payment; if (payment.status !== "REQUIRES_CONFIRMATION") return payment;
    payment.status = "PROCESSING"; payment.updatedAt = now(); record.events.push(eventItem("payment.processing", "Request sent to mock payment provider"));
    var suffix = payment.amount % 100;
    if (suffix === 2) { payment.status = "FAILED"; payment.failureCode = "mock_payment_declined"; record.events.push(eventItem("payment.failed", "Provider declined the payment")); }
    else if (suffix === 77) { payment.providerReference = randomToken("mock"); record.events.push(eventItem("payment.pending", "Provider result is unknown; reconciliation must resolve it")); }
    else {
      payment.status = "SUCCEEDED"; payment.providerReference = randomToken("mock"); record.events.push(eventItem("payment.succeeded", "Provider confirmed the payment"));
      browserLedger.push(paymentLedger(payment)); enqueueEvent("PAYMENT", payment.id, "payment.succeeded");
    }
    payment.updatedAt = now(); renderResponse(payment); return payment;
  }

  function browserRefund(paymentId, key, amount) {
    var record = browserPayments[paymentId]; var payment = record && record.payment;
    if (!payment) throwPayload("payment_not_found", "Payment intent not found");
    if (record.refundKeys[key]) {
      var existing = record.refundKeys[key]; if (existing.amount !== amount) throwPayload("idempotency_conflict", "Refund key already used with different parameters");
      return existing;
    }
    if (["SUCCEEDED", "PARTIALLY_REFUNDED"].indexOf(payment.status) < 0) throwPayload("invalid_payment_state", "Only a succeeded payment can be refunded");
    if (amount <= 0 || payment.refundedAmount + amount > payment.amount) throwPayload("invalid_request", "Refund amount exceeds refundable amount");
    var refund = { id: randomToken("re"), paymentId: paymentId, amount: amount, status: "PENDING", createdAt: now(), updatedAt: now() };
    record.events.push(eventItem("refund.pending", "Refund " + refund.id + " submitted"));
    refund.status = "SUCCEEDED"; refund.updatedAt = now(); payment.refundedAmount += amount;
    payment.status = payment.refundedAmount === payment.amount ? "REFUNDED" : "PARTIALLY_REFUNDED"; payment.updatedAt = now();
    record.events.push(eventItem("refund.succeeded", "Refund completed for " + amount + " " + payment.currency));
    record.refundKeys[key] = refund; browserLedger.push(refundLedger(refund, payment)); enqueueEvent("REFUND", refund.id, "refund.succeeded"); return refund;
  }

  function paymentLedger(payment) {
    var fee = payment.amount > 1 ? Math.max(1, Math.floor(payment.amount / 100)) : 0;
    var entries = [{ account: "provider_receivable", side: "DEBIT", amount: payment.amount, currency: payment.currency },
      { account: "merchant_payable", side: "CREDIT", amount: payment.amount - fee, currency: payment.currency }];
    if (fee) entries.push({ account: "platform_fee_revenue", side: "CREDIT", amount: fee, currency: payment.currency });
    return { id: randomToken("ltx"), referenceType: "PAYMENT", referenceId: payment.id, description: "Capture payment", balanced: true, entries: entries };
  }
  function refundLedger(refund, payment) { return { id: randomToken("ltx"), referenceType: "REFUND", referenceId: refund.id, description: "Reverse captured funds", balanced: true,
    entries: [{ account: "merchant_payable", side: "DEBIT", amount: refund.amount, currency: payment.currency }, { account: "provider_receivable", side: "CREDIT", amount: refund.amount, currency: payment.currency }] }; }

  function enqueueEvent(aggregateType, aggregateId, type) {
    var event = { id: randomToken("out"), aggregateType: aggregateType, aggregateId: aggregateId, eventType: type, status: "PENDING", attempts: 0, createdAt: now() };
    browserOutbox.unshift(event); renderOperations(browserLedger, browserOutbox, browserWebhooks);
    setTimeout(function () {
      event.status = "PUBLISHED"; event.attempts = 1; event.publishedAt = now();
      var delivery = { id: randomToken("wh"), eventId: event.id, endpoint: "mock://merchant/webhooks", status: "RETRYING", attempts: 1, lastStatusCode: 503, lastError: "mock endpoint unavailable" };
      browserWebhooks.unshift(delivery); renderOperations(browserLedger, browserOutbox, browserWebhooks);
      setTimeout(function () { delivery.attempts = 2; renderOperations(browserLedger, browserOutbox, browserWebhooks); }, 900);
      setTimeout(function () { delivery.attempts = 3; delivery.status = "SUCCEEDED"; delivery.lastStatusCode = 200; delivery.lastError = null; renderOperations(browserLedger, browserOutbox, browserWebhooks); }, 1900);
    }, 500);
  }

  function eventItem(type, detail) { return { id: randomToken("evt"), type: type, detail: detail, createdAt: now() }; }
  function throwPayload(code, message) { var error = new Error(message); error.payload = { code: code, message: message }; throw error; }

  function renderPayment(payment) {
    var status = payment ? payment.status : null; var pill = document.getElementById("statusPill");
    pill.textContent = status ? status.replaceAll("_", " ") : "未创建"; pill.className = "status-pill " + (status ? status.toLowerCase() : "idle");
    document.getElementById("paymentId").textContent = payment ? payment.id : "—";
    document.getElementById("providerRef").textContent = payment && payment.providerReference ? payment.providerReference : "—";
    document.getElementById("failureCode").textContent = payment && payment.failureCode ? payment.failureCode : "—";
    document.getElementById("refundedAmount").textContent = payment ? (payment.refundedAmount || 0) + " / " + payment.amount + " " + payment.currency : "0";
    document.querySelectorAll(".state").forEach(function (node) {
      node.classList.remove("active", "failed"); if (!status && node.dataset.state === "REQUIRES_CONFIRMATION") node.classList.add("active");
      if (status === node.dataset.state || (["PARTIALLY_REFUNDED", "REFUNDED"].indexOf(status) >= 0 && node.dataset.state === "SUCCEEDED")) node.classList.add(status === "FAILED" ? "failed" : "active");
    });
    confirmButton.disabled = !payment || payment.status !== "REQUIRES_CONFIRMATION";
    refundButton.disabled = !payment || ["SUCCEEDED", "PARTIALLY_REFUNDED"].indexOf(payment.status) < 0;
  }

  function renderEvents(events) {
    var timeline = document.getElementById("timeline");
    if (!events.length) { timeline.innerHTML = '<li class="empty">创建一笔支付后，领域事件会出现在这里。</li>'; return; }
    timeline.innerHTML = events.map(function (item) { return "<li><b>" + escapeHtml(item.type) + "</b><p>" + escapeHtml(item.detail) + "</p><time>" + new Date(item.createdAt).toLocaleTimeString() + "</time></li>"; }).join("");
  }

  function renderOperations(ledger, outbox, webhooks) {
    document.getElementById("ledgerView").innerHTML = ledger.length ? ledger.map(function (transaction) {
      return '<div class="data-card"><strong>' + escapeHtml(transaction.referenceType) + ' · BALANCED</strong>' + transaction.entries.map(function (entry) {
        return '<div class="entry"><span>' + escapeHtml(entry.side + " " + entry.account) + '</span><span>' + entry.amount + " " + entry.currency + '</span></div>';
      }).join("") + '</div>';
    }).join("") : '<p class="empty-copy">支付成功后生成平衡分录。</p>';
    document.getElementById("outboxView").innerHTML = outbox.length ? outbox.map(function (event) { return '<div class="data-card"><strong>' + escapeHtml(event.status) + '</strong>' + escapeHtml(event.eventType) + '<br>attempts: ' + event.attempts + '</div>'; }).join("") : '<p class="empty-copy">业务事务提交后等待发布。</p>';
    document.getElementById("webhookView").innerHTML = webhooks.length ? webhooks.map(function (delivery) { return '<div class="data-card ' + (delivery.status === "RETRYING" ? "failed" : "") + '"><strong>' + escapeHtml(delivery.status) + '</strong>HTTP ' + (delivery.lastStatusCode || "—") + ' · attempt ' + delivery.attempts + '<br>' + escapeHtml(delivery.endpoint) + '</div>'; }).join("") : '<p class="empty-copy">Mock 端点前两次返回 503，第三次成功。</p>';
  }

  function renderResponse(payload) { document.getElementById("apiResponse").textContent = JSON.stringify(payload, null, 2); }
  function setBusy(busy) {
    document.getElementById("createButton").disabled = busy;
    confirmButton.disabled = busy || !currentPayment || currentPayment.status !== "REQUIRES_CONFIRMATION";
    refundButton.disabled = busy || !currentPayment || ["SUCCEEDED", "PARTIALLY_REFUNDED"].indexOf(currentPayment.status) < 0;
  }
  function escapeHtml(value) { var element = document.createElement("div"); element.textContent = value == null ? "" : String(value); return element.innerHTML; }

  var commerceProducts = [
    { sku: "SKU-COFFEE", name: "Origin Coffee Beans", unitPrice: 1899, currency: "CNY", available: 100 },
    { sku: "SKU-MUG", name: "System Design Mug", unitPrice: 2499, currency: "CNY", available: 60 },
    { sku: "SKU-HOODIE", name: "Platform Engineering Hoodie", unitPrice: 6999, currency: "CNY", available: 30 }
  ];
  var commerceItems = {};
  var commerceCartId = null;
  var commerceCustomer = "demo-" + Date.now().toString().slice(-8);
  var commerceWallet = 5000;
  var commerceWalletCreated = false;

  function renderCommerceProducts() {
    document.getElementById("productGrid").innerHTML = commerceProducts.map(function (product) {
      return '<div class="product-card"><strong>' + escapeHtml(product.name) + '</strong><small>' + product.unitPrice + ' ' + product.currency + ' · 库存 ' + product.available + '</small><button type="button" data-commerce-sku="' + product.sku + '" aria-label="添加 ' + escapeHtml(product.name) + '">+</button></div>';
    }).join("");
    document.querySelectorAll("[data-commerce-sku]").forEach(function (button) {
      button.addEventListener("click", function () { addCommerceItem(button.dataset.commerceSku); });
    });
  }

  function commerceTotal() {
    return Object.keys(commerceItems).reduce(function (sum, sku) {
      var product = commerceProducts.find(function (item) { return item.sku === sku; });
      return sum + product.unitPrice * commerceItems[sku];
    }, 0);
  }

  function renderCommerceCart() {
    var count = Object.values(commerceItems).reduce(function (sum, quantity) { return sum + quantity; }, 0);
    document.getElementById("cartSummary").textContent = count + " 件 · " + commerceTotal() + " CNY";
    document.getElementById("walletBalance").textContent = commerceWallet.toLocaleString() + " CNY";
    document.getElementById("commerceCheckout").disabled = count === 0;
  }

  async function ensureCommerceBackend() {
    if (isPages || commerceCartId) return;
    var cart = await api("/v1/commerce/carts", { method: "POST", body: JSON.stringify({ customerId: commerceCustomer }) });
    commerceCartId = cart.id;
    if (!commerceWalletCreated) {
      await api("/v1/commerce/wallets/" + commerceCustomer + "/vouchers", { method: "POST", body: JSON.stringify({ amount: 5000, currency: "CNY", reference: "demo-welcome" }) });
      commerceWalletCreated = true;
    }
  }

  async function addCommerceItem(sku) {
    try {
      if (!isPages) { await ensureCommerceBackend(); await api("/v1/commerce/carts/" + commerceCartId + "/items", { method: "POST", body: JSON.stringify({ sku: sku, quantity: 1 }) }); }
      commerceItems[sku] = (commerceItems[sku] || 0) + 1; renderCommerceCart();
      document.getElementById("orderStatus").textContent = "CART OPEN";
    } catch (error) { alert(error.message); }
  }

  function markCommerceStep(index) {
    document.querySelectorAll("#checkoutSteps span").forEach(function (step, position) { step.classList.toggle("done", position <= index); });
  }

  async function checkoutCommerce() {
    var button = document.getElementById("commerceCheckout"); button.disabled = true;
    try {
      markCommerceStep(0); await new Promise(function (resolve) { setTimeout(resolve, 180); });
      var requested = Math.max(0, Number(document.getElementById("voucherSpend").value) || 0);
      var total = commerceTotal(); var voucher = Math.min(requested, commerceWallet, total); var external = total - voucher;
      var order;
      if (isPages) {
        order = { id: randomToken("order"), orderNumber: "ORD-" + Date.now().toString().slice(-6), status: external ? "PENDING_PAYMENT" : "PAID", totalAmount: total, voucherAmount: voucher, payableAmount: external, paymentId: external ? randomToken("pay") : null };
      } else {
        await ensureCommerceBackend();
        order = await api("/v1/commerce/checkout", { method: "POST", headers: { "Idempotency-Key": "commerce-" + Date.now() }, body: JSON.stringify({ cartId: commerceCartId, voucherAmount: requested }) });
      }
      markCommerceStep(1); document.getElementById("orderStatus").textContent = "STOCK RESERVED"; await new Promise(function (resolve) { setTimeout(resolve, 260); });
      markCommerceStep(2); document.getElementById("orderStatus").textContent = order.status.replaceAll("_", " ");
      if (!isPages && order.paymentId) order = await api("/v1/commerce/orders/" + order.id + "/confirm", { method: "POST" });
      else if (isPages && external) { await new Promise(function (resolve) { setTimeout(resolve, 380); }); order.status = "PAID"; }
      if (order.status === "PAYMENT_FAILED") {
        document.getElementById("orderStatus").textContent = "PAYMENT FAILED";
        document.getElementById("commerceOrder").textContent = order.orderNumber;
        document.getElementById("commerceVoucher").textContent = voucher + " CNY · REFUNDED";
        document.getElementById("commerceExternal").textContent = external + " CNY · FAILED";
        document.getElementById("commerceStock").textContent = "RESERVATION RELEASED";
        button.disabled = false; return;
      }
      if (order.status === "PENDING_PAYMENT") {
        commerceWallet -= voucher; renderCommerceCart();
        document.getElementById("orderStatus").textContent = "PAYMENT PROCESSING";
        document.getElementById("commerceOrder").textContent = order.orderNumber;
        document.getElementById("commerceVoucher").textContent = voucher + " CNY";
        document.getElementById("commerceExternal").textContent = external + " CNY · PROCESSING";
        document.getElementById("commerceStock").textContent = "RESERVED"; return;
      }
      markCommerceStep(3); commerceWallet -= voucher;
      Object.keys(commerceItems).forEach(function (sku) { var product = commerceProducts.find(function (item) { return item.sku === sku; }); product.available -= commerceItems[sku]; });
      document.getElementById("orderStatus").textContent = order.status.replaceAll("_", " ");
      document.getElementById("commerceOrder").textContent = order.orderNumber;
      document.getElementById("commerceVoucher").textContent = voucher + " CNY";
      document.getElementById("commerceExternal").textContent = external + " CNY" + (external ? " · SUCCEEDED" : " · NOT REQUIRED");
      document.getElementById("commerceStock").textContent = "RESERVED → COMMITTED";
      renderCommerceProducts(); renderCommerceCart(); renderResponse(order);
    } catch (error) {
      document.getElementById("orderStatus").textContent = "PAYMENT FAILED";
      document.getElementById("commerceStock").textContent = "RESERVATION RELEASED";
      alert(error.message);
    }
  }

  document.getElementById("commerceCheckout").addEventListener("click", checkoutCommerce);

  modeBadge.textContent = isPages ? "公开演示模式 · 数据仅保存在当前浏览器" : "API 模式 · Spring Boot + PostgreSQL";
  initializeFields(); renderPayment(null); renderOperations([], [], []); renderCommerceProducts(); renderCommerceCart();
  if (!isPages) setInterval(function () { if (currentPayment) refreshOperations().catch(function () {}); }, 3000);
})();
