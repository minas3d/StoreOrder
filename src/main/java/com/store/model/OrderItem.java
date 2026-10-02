package com.store.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class OrderItem extends BaseEntity {
//    order ,product , quantity , unitPrice
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "order_id")
    private Order order ;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    protected OrderItem() {}
    public OrderItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = product.getPrice();   // نسخة من السعر وقت الطلب
    }
    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Order getOrder() { return order; }
    void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }

}
