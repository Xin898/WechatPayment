(function () {
  "use strict";

  var isPages = location.hostname.endsWith("github.io") || location.protocol === "file:";
  var currentPayment = null;
  var browserPayments = {};
  var browserKeys = {};

  var form = document.getElementById("paymentForm");
  var orderInput = document.getElementById("orderId");
  var amountInput = document.getElementById("amount");
  var currencyInput = document.getElementById("currency");
  var keyInput = document.getElementById("idempotencyKey");
  var confirmButton = document.getElementById("confirmButton");
  var modeBadge = document.getElementById("modeBadge");

  function randomToken(prefix) {
    return prefix + "-" + Math.random().toString(16).slice(2, 10);
  }

  function initializeFields() {
    var token = Date.now().toString().slice(-8);
    orderInput.value = "ORDER-" + token;
    keyInput.value = "idem-" + token;
  }

  function applySuffix(suffix) {
    var raw = Math.max(100, Number(amountInput.value) || 100);
    amountInput.value = Math.floor(raw / 100) * 100 + Number(suffix);
  }

  document.querySelectorAll(".scenario").forEach(function (button) {
    button.addEventListener("click", function () {
      document.querySelectorAll(".scenario").forEach(function (item) {
        item.classList.remove("active");
      });
      button.classList.add("active");
      applySuffix(button.dataset.suffix);
    });
  });

  form.addEventListener("submit", async function (event) {
    event.preventDefault();
    await runAction(createPayment);
  });

  confirmButton.addEventListener("click", async function () {
    await runAction(confirmPayment);
  });

  document.getElementById("resetButton").addEventListener("click", function () {
    currentPayment = null;
    initializeFields();
    amountInput.value = "129900";
    document.querySelectorAll(".scenario").forEach(function (item, index) {
      item.classList.toggle("active", index === 0);
    });
    renderPayment(null);
    renderEvents([]);
    renderResponse({});
  });

  async function runAction(action) {
    setBusy(true);
    try {
      await action();
    } catch (error) {
      renderResponse(error.payload || { code: "client_error", message: error.message });
      alert(error.message);
    } finally {
      setBusy(false);
    }
  }

  async function createPayment() {
    var request = {
      merchantOrderId: orderInput.value,
      amount: Number(amountInput.value),
      currency: currencyInput.value
    };
    var key = keyInput.value;

    currentPayment = isPages
      ? browserCreate(key, request)
      : await api("/v1/payment-intents", {
          method: "POST",
          headers: { "Idempotency-Key": key },
          body: JSON.stringify(request)
        });

    renderPayment(currentPayment);
    await refreshEvents();
  }

  async function confirmPayment() {
    if (!currentPayment) return;
    currentPayment = isPages
      ? browserConfirm(currentPayment.id)
      : await api("/v1/payment-intents/" + currentPayment.id + "/confirm", {
          method: "POST"
        });

    renderPayment(currentPayment);
    await refreshEvents();
  }

  async function refreshEvents() {
    if (!currentPayment) return renderEvents([]);
    var events = isPages
      ? browserPayments[currentPayment.id].events
      : await api("/v1/payment-intents/" + currentPayment.id + "/events");
    renderEvents(events);
  }

  async function api(path, options) {
    var response = await fetch(path, Object.assign({
      headers: { "Content-Type": "application/json" }
    }, options || {}));
    var payload = await response.json();
    renderResponse(payload);
    if (!response.ok) {
      var error = new Error(payload.message || "API request failed");
      error.payload = payload;
      throw error;
    }
    return payload;
  }

  function browserCreate(key, request) {
    var fingerprint = JSON.stringify(request);
    if (browserKeys[key]) {
      if (browserKeys[key].fingerprint !== fingerprint) {
        throwPayload("idempotency_conflict", "Idempotency key was already used with different parameters");
      }
      renderResponse(browserKeys[key].payment);
      return browserKeys[key].payment;
    }

    var now = new Date().toISOString();
    var payment = {
      id: crypto.randomUUID ? crypto.randomUUID() : randomToken("pay"),
      merchantOrderId: request.merchantOrderId,
      amount: request.amount,
      currency: request.currency,
      status: "REQUIRES_CONFIRMATION",
      providerReference: null,
      failureCode: null,
      createdAt: now,
      updatedAt: now
    };
    browserPayments[payment.id] = {
      payment: payment,
      events: [eventItem("payment.created", "Payment intent created and awaiting confirmation")]
    };
    browserKeys[key] = { fingerprint: fingerprint, payment: payment };
    renderResponse(payment);
    return payment;
  }

  function browserConfirm(id) {
    var record = browserPayments[id];
    if (!record) throwPayload("payment_not_found", "Payment intent not found");
    var payment = record.payment;
    if (payment.status !== "REQUIRES_CONFIRMATION") {
      renderResponse(payment);
      return payment;
    }

    payment.status = "PROCESSING";
    payment.updatedAt = new Date().toISOString();
    record.events.push(eventItem("payment.processing", "Request sent to mock payment provider"));

    var suffix = payment.amount % 100;
    if (suffix === 2) {
      payment.status = "FAILED";
      payment.failureCode = "mock_payment_declined";
      record.events.push(eventItem("payment.failed", "Provider declined the payment: mock_payment_declined"));
    } else if (suffix === 77) {
      payment.providerReference = randomToken("mock");
      record.events.push(eventItem("payment.pending", "Provider result is unknown; callback or reconciliation must resolve it"));
    } else {
      payment.status = "SUCCEEDED";
      payment.providerReference = randomToken("mock");
      record.events.push(eventItem("payment.succeeded", "Provider confirmed the payment"));
    }
    payment.updatedAt = new Date().toISOString();
    renderResponse(payment);
    return payment;
  }

  function eventItem(type, detail) {
    return {
      id: randomToken("evt"),
      type: type,
      detail: detail,
      createdAt: new Date().toISOString()
    };
  }

  function throwPayload(code, message) {
    var error = new Error(message);
    error.payload = { code: code, message: message };
    throw error;
  }

  function renderPayment(payment) {
    var status = payment ? payment.status : null;
    var pill = document.getElementById("statusPill");
    pill.textContent = status ? status.replaceAll("_", " ") : "未创建";
    pill.className = "status-pill " + (status ? status.toLowerCase() : "idle");

    document.getElementById("paymentId").textContent = payment ? payment.id : "—";
    document.getElementById("providerRef").textContent =
      payment && payment.providerReference ? payment.providerReference : "—";
    document.getElementById("failureCode").textContent =
      payment && payment.failureCode ? payment.failureCode : "—";

    document.querySelectorAll(".state").forEach(function (node) {
      node.classList.remove("active", "failed");
      if (!status && node.dataset.state === "REQUIRES_CONFIRMATION") node.classList.add("active");
      if (status === node.dataset.state) {
        node.classList.add(status === "FAILED" ? "failed" : "active");
      }
    });

    confirmButton.disabled = !payment || payment.status !== "REQUIRES_CONFIRMATION";
  }

  function renderEvents(events) {
    var timeline = document.getElementById("timeline");
    if (!events.length) {
      timeline.innerHTML = '<li class="empty">创建一笔支付后，领域事件会出现在这里。</li>';
      return;
    }
    timeline.innerHTML = events.map(function (item) {
      return "<li><b>" + escapeHtml(item.type) + "</b><p>" +
        escapeHtml(item.detail) + "</p><time>" +
        new Date(item.createdAt).toLocaleTimeString() + "</time></li>";
    }).join("");
  }

  function renderResponse(payload) {
    document.getElementById("apiResponse").textContent = JSON.stringify(payload, null, 2);
  }

  function setBusy(busy) {
    document.getElementById("createButton").disabled = busy;
    confirmButton.disabled = busy || !currentPayment ||
      currentPayment.status !== "REQUIRES_CONFIRMATION";
  }

  function escapeHtml(value) {
    var element = document.createElement("div");
    element.textContent = value;
    return element.innerHTML;
  }

  modeBadge.textContent = isPages
    ? "公开演示模式 · 数据仅保存在当前浏览器"
    : "API 模式 · Spring Boot + PostgreSQL";
  initializeFields();
  renderPayment(null);
})();
