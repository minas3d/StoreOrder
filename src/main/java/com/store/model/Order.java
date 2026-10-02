package com.store.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends BaseEntity{

   @ManyToOne(fetch = FetchType.LAZY,optional = false)
   @JoinColumn(name = "customer_id")
   private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
   private OrderStatus status = OrderStatus.NEW;

    @Column(nullable = false, updatable = false)
    private Instant orderedAt;
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL,orphanRemoval = true)
    List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order",cascade = CascadeType.ALL)
    private Payment payment;

    @PrePersist
    void onOrder() {
        if (orderedAt == null) orderedAt = Instant.now();
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }
    public void setPayment(Payment payment) {
        this.payment = payment;
        if (payment != null) payment.setOrder(this);
    }


    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }

    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Payment getPayment() {
        return payment;
    }


}
