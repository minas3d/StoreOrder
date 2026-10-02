package com.store.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class Payment extends BaseEntity{
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(nullable = false)
    private Instant paidAt;

    protected Payment() {}

    public Payment(BigDecimal amount, PaymentMethod method) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        this.amount = amount;
        this.method = method;
        this.paidAt = Instant.now();
    }

    public Order getOrder() { return order; }
    void setOrder(Order order) { this.order = order; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public Instant getPaidAt() { return paidAt;}


    }
