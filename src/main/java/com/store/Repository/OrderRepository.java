package com.store.Repository;

import com.store.model.Order;
import com.store.model.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderRepository {
    @PersistenceContext
    private EntityManager em;

    public Order save(Order order) {
        if (order.getId() == null) {
            em.persist(order);
            return order;
        }
        return em.merge(order);
    }
    public Order findById(Long id) {
        return em.find(Order.class, id);
    }
    public Order findByIdWithItems(Long id){
        return em.createQuery("select o from Order o left join fetch o.items where o.id=:id ",Order.class)
                .setParameter("id",id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    public List<Order> findByCustomer(Long customerId){
        return em.createQuery("select o from Order o WHERE o.customer.id = :customerId ",Order.class)
                .setParameter("customerId", customerId).getResultList();
    }
    public List<Order> findByStatus(OrderStatus status){
        return em.createQuery("select o from Order o where o.status =:status ",Order.class).
                setParameter("status",status).getResultList();
    }
    public Order findByIdWithDetails(Long id) {
        return em.createQuery("select o from Order o "
                                + "join fetch o.customer "
                                + "left join fetch o.items i "
                                + "left join fetch i.product "
                                + "where o.id = :id", Order.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

}
