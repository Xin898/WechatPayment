package com.xin.payment.commerce;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/v1/commerce")
public class CommerceController {
    private final CartService carts;private final CheckoutService checkout;private final VoucherService vouchers;private final InventoryService inventory;
    public CommerceController(CartService carts,CheckoutService checkout,VoucherService vouchers,InventoryService inventory){this.carts=carts;this.checkout=checkout;this.vouchers=vouchers;this.inventory=inventory;}

    @GetMapping("/products") List<ProductView> products(){return carts.products().stream().map(p->new ProductView(p.sku(),p.name(),p.unitPrice(),p.currency(),inventory.item(p.sku()).available())).toList();}
    @PostMapping("/carts") CartView createCart(@Valid @RequestBody CreateCart body){return CartView.from(carts.create(body.customerId()));}
    @GetMapping("/carts/{id}") CartView cart(@PathVariable UUID id){return CartView.from(carts.get(id));}
    @PostMapping("/carts/{id}/items") CartView add(@PathVariable UUID id,@Valid @RequestBody AddItem body){return CartView.from(carts.add(id,body.sku(),body.quantity()));}
    @PostMapping("/checkout") OrderView checkout(@RequestHeader("Idempotency-Key") @NotBlank String key,@Valid @RequestBody Checkout body){return OrderView.from(checkout.checkout(body.cartId(),key,body.voucherAmount()));}
    @PostMapping("/orders/{id}/confirm") OrderView confirm(@PathVariable UUID id){return OrderView.from(checkout.confirm(id));}
    @GetMapping("/orders/{id}") OrderView order(@PathVariable UUID id){return OrderView.from(checkout.get(id));}
    @PostMapping("/wallets/{customerId}/vouchers") WalletView issue(@PathVariable String customerId,@Valid @RequestBody IssueVoucher body){return WalletView.from(vouchers.issue(customerId,body.amount(),body.currency(),body.reference()));}
    @GetMapping("/wallets/{customerId}") WalletView wallet(@PathVariable String customerId){return WalletView.from(vouchers.get(customerId));}
    @GetMapping("/wallets/{customerId}/entries") List<VoucherEntryView> entries(@PathVariable String customerId){return vouchers.entries(customerId).stream().map(VoucherEntryView::from).toList();}

    public record CreateCart(@NotBlank String customerId){} public record AddItem(@NotBlank String sku,@Positive int quantity){}
    public record Checkout(@NotNull UUID cartId,@PositiveOrZero long voucherAmount){}
    public record IssueVoucher(@Positive long amount,@NotBlank @Size(min=3,max=3) String currency,@NotBlank String reference){}
    public record ProductView(String sku,String name,long unitPrice,String currency,long available){}
    public record ItemView(String sku,String name,long unitPrice,int quantity,long lineTotal){static ItemView from(CartItem i){return new ItemView(i.sku(),i.productName(),i.unitPrice(),i.quantity(),i.lineTotal());}}
    public record CartView(UUID id,String customerId,String status,long total,List<ItemView> items){static CartView from(ShoppingCart c){return new CartView(c.id(),c.customerId(),c.status().name(),c.total(),c.items().stream().map(ItemView::from).toList());}}
    public record LineView(String sku,String name,long unitPrice,int quantity,long lineTotal){static LineView from(OrderLine l){return new LineView(l.sku(),l.productName(),l.unitPrice(),l.quantity(),l.lineTotal());}}
    public record OrderView(UUID id,String orderNumber,String customerId,String status,long totalAmount,long voucherAmount,long payableAmount,String currency,UUID paymentId,Instant createdAt,List<LineView> lines){static OrderView from(SalesOrder o){return new OrderView(o.id(),o.orderNumber(),o.customerId(),o.status().name(),o.totalAmount(),o.voucherAmount(),o.payableAmount(),o.currency(),o.paymentId(),o.createdAt(),o.lines().stream().map(LineView::from).toList());}}
    public record WalletView(String customerId,long balance,String currency){static WalletView from(VoucherWallet w){return new WalletView(w.customerId(),w.balance(),w.currency());}}
    public record VoucherEntryView(UUID id,String type,long amount,String reference,Instant createdAt){static VoucherEntryView from(VoucherEntry e){return new VoucherEntryView(e.id(),e.type().name(),e.amount(),e.reference(),e.createdAt());}}
}
