package com.store.service;

import com.store.Repository.CustomerRepository;
import com.store.Repository.OrderRepository;
import com.store.Repository.ProductRepository;
import com.store.dto.OrderSummary;
import com.store.exception.InsufficientStockException;
import com.store.exception.InvalidOrderStateException;
import com.store.model.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final ProductRepository products;

    public OrderService(OrderRepository orders, CustomerRepository customers, ProductRepository products) {
        this.orders = orders;
        this.customers = customers;
        this.products = products;
    }
    @Transactional
    public Order placeOrder(Long customerId, Map<Long,Integer> productQuantities){
        if (customerId==null||productQuantities ==null ){
            throw new InsufficientStockException("Order must contain at least one product");
        }
        Customer customer =customers.findById(customerId);
        if (customer==null){
            throw new InsufficientStockException("Customer not found: " + customerId);
        }
        Order order = new Order();
        for (Map.Entry<Long,Integer> line :productQuantities.entrySet() ){
             Long productId = line.getKey();
             Integer quantity = line.getValue();
             if (quantity == null || quantity <= 0) {
                 throw new InsufficientStockException("Quantity must be greater than 0 for product " + productId);
             }

             Product product = products.findById(productId);
             if (product == null) {
                 throw new IllegalArgumentException("Product not found: " + productId);
             }
             if (product.getStock() < quantity) {
                 throw new InsufficientStockException("Insufficient stock for "
                         + product.getSku()
                         + ": requested "
                         + quantity
                         + ", available "
                         + product.getStock());
             }
             product.setStock(product.getStock() - quantity);
             order.addItem(new OrderItem(product, quantity));
        }
        customer.addOrder(order);
        return orders.save(order);
    }
    @Transactional
    public Payment pay(Long orderId, PaymentMethod method){
        if (orderId==null ||method ==null ){
            throw new InvalidOrderStateException(" ");
        }
        Order order = orders.findById(orderId);

        if(order==null){
            throw new InvalidOrderStateException("in valid ");
        }
        if(order.getStatus()!=OrderStatus.NEW){
            throw new InvalidOrderStateException("Only a NEW order can be paid, but order " + orderId + " is " + order.getStatus());
        }
        BigDecimal total = order.getItems().stream()
                           .map(OrderItem::getLineTotal)
                           .reduce(BigDecimal.ZERO,BigDecimal::add);


        Payment payment =new Payment(total,method);
        order.setPayment(payment);
        order.setStatus(OrderStatus.PAID);
        return payment;
    }
    @Transactional
    public void ship(Long orderId) {
         Order order = orders.findById(orderId);
        if(orderId==null || order==null){
            throw new InvalidOrderStateException("in valid ");
        }
        if ( order.getStatus()!= OrderStatus.PAID){
            throw new InvalidOrderStateException("in valid Becase order is not paid  ");
        }
        order.setStatus(OrderStatus.SHIPPED);
     }
    @Transactional
    public void cancel(Long orderId){
         Order order = orders.findById(orderId);
         if(orderId==null || order==null){
             throw new InvalidOrderStateException("in valid ");
         }
         if ( order.getStatus()!= OrderStatus.NEW|| order.getStatus()!= OrderStatus.PAID ){
             throw new InvalidOrderStateException("in valid order ");
         }
         for (OrderItem item : order.getItems()){
             Product p = item.getProduct();
             p.setStock(p.getStock()+item.getQuantity());
         }
         order.setStatus(OrderStatus.CANCELLED);

     }
    @Transactional(readOnly = true  )
    public OrderSummary getOrderSummary(Long orderId){
       Order order= orders.findByIdWithItems(orderId);
        if(orderId==null || order==null){
            throw new InvalidOrderStateException(" in valid ");
        }
        List<OrderSummary.Line> lines =order.getItems().stream()
                .map(i-> new OrderSummary.Line( i.getProduct().getSku(), i.getProduct().getName(),
                        i.getQuantity(), i.getUnitPrice(), i.getLineTotal()))
                .toList();
        BigDecimal total = lines.stream()
                .map(OrderSummary.Line::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderSummary(order.getId(), order.getCustomer().getName(),
                order.getStatus(), order.getOrderedAt(), lines, total);
    }


}
