package com.store.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
@Entity
public class Customer extends BaseEntity{

    private String name ;

    @Column(nullable = false,unique = true)
    private String email;
    @Embedded
    private Address shippingAddress;
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Order> orders = new HashSet<>();
    protected Customer() {}
    public Customer(String name, String email, Address shippingAddress) {
        setName( name);
        setEmail(email);
        setShippingAddress(shippingAddress);

    }
    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }
}
