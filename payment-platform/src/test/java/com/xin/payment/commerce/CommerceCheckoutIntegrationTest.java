package com.xin.payment.commerce;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommerceCheckoutIntegrationTest {
    @Autowired CartService carts;
    @Autowired CheckoutService checkout;
    @Autowired VoucherService vouchers;
    @Autowired InventoryService inventory;

    @Test
    void checksOutWithVoucherAndExternalPaymentThenCommitsStock() {
        vouchers.issue("customer-1", 5000, "CNY", "welcome-voucher");
        var cart = carts.create("customer-1");
        carts.add(cart.id(), "SKU-COFFEE", 2);

        var order = checkout.checkout(cart.id(), "checkout-001", 1500);

        assertThat(order.status()).isEqualTo(SalesOrder.Status.PENDING_PAYMENT);
        assertThat(order.totalAmount()).isEqualTo(3798);
        assertThat(order.voucherAmount()).isEqualTo(1500);
        assertThat(order.payableAmount()).isEqualTo(2298);
        assertThat(vouchers.get("customer-1").balance()).isEqualTo(3500);
        assertThat(inventory.item("SKU-COFFEE").reserved()).isEqualTo(2);

        var paid = checkout.confirm(order.id());
        assertThat(paid.status()).isEqualTo(SalesOrder.Status.PAID);
        assertThat(inventory.item("SKU-COFFEE").reserved()).isZero();
        assertThat(inventory.item("SKU-COFFEE").onHand()).isEqualTo(98);
        assertThat(carts.get(cart.id()).status()).isEqualTo(ShoppingCart.Status.CHECKED_OUT);
    }

    @Test
    void voucherCanPayTheWholeOrderWithoutCreatingExternalPayment() {
        vouchers.issue("customer-2", 3000, "CNY", "gift-card");
        var cart = carts.create("customer-2");
        carts.add(cart.id(), "SKU-MUG", 1);

        var order = checkout.checkout(cart.id(), "checkout-002", 3000);

        assertThat(order.status()).isEqualTo(SalesOrder.Status.PAID);
        assertThat(order.paymentId()).isNull();
        assertThat(order.voucherAmount()).isEqualTo(2499);
        assertThat(vouchers.get("customer-2").balance()).isEqualTo(501);
        assertThat(inventory.item("SKU-MUG").onHand()).isEqualTo(59);
    }

    @Test
    void checkoutIsIdempotent() {
        var cart = carts.create("customer-3");
        carts.add(cart.id(), "SKU-HOODIE", 1);
        var first = checkout.checkout(cart.id(), "checkout-003", 0);
        var second = checkout.checkout(cart.id(), "checkout-003", 0);
        assertThat(second.id()).isEqualTo(first.id());
        assertThat(inventory.item("SKU-HOODIE").reserved()).isEqualTo(1);
    }
}
