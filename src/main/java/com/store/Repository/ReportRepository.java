package com.store.Repository;

import com.store.dto.*;
import com.store.model.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Repository
public class ReportRepository {

    private static final List<OrderStatus> SOLD = List.of(OrderStatus.PAID, OrderStatus.SHIPPED);

    @PersistenceContext
    private EntityManager em;


    public List<CategoryRevenue> revenueByCategory() {
        return em.createQuery("""
                select new com.store.dto.CategoryRevenue(c.name, sum(i.unitPrice * i.quantity))
                from Order o
                join o.items i
                join i.product p
                join p.categories c
                where o.status in (:statuses)
                group by c.name
                order by sum(i.unitPrice * i.quantity) desc
                """, CategoryRevenue.class)
                .setParameter("statuses", SOLD)
                .getResultList();
    }


    public List<CustomerSpend> topCustomers(int limit) {
        return em.createQuery("""
                select new com.store.dto.CustomerSpend(cu.name, sum(i.unitPrice * i.quantity))
                from Order o
                join o.customer cu
                join o.items i
                where o.status in (:statuses)
                group by cu.id, cu.name
                order by sum(i.unitPrice * i.quantity) desc
                """, CustomerSpend.class)
                .setParameter("statuses", SOLD)
                .setMaxResults(limit)
                .getResultList();
    }


    public Map<OrderStatus, Long> ordersPerStatus() {
        Map<OrderStatus, Long> result = new EnumMap<>(OrderStatus.class);
        for (OrderStatus s : OrderStatus.values()) result.put(s, 0L);   // الحالات الفاضية بـ 0

        List<Object[]> rows = em.createQuery(
                        "select o.status, count(o) from Order o group by o.status", Object[].class)
                .getResultList();
        for (Object[] r : rows) result.put((OrderStatus) r[0], (Long) r[1]);
        return result;
    }


    public List<ProductInfo> productsNeverOrdered() {
        return em.createQuery("""
                select new com.store.dto.ProductInfo(p.sku, p.name, p.stock)
                from Product p
                where not exists (select i.id from OrderItem i where i.product = p)
                order by p.name
                """, ProductInfo.class)
                .getResultList();
    }


    public List<MonthlySales> monthlySales(int year) {
        return em.createQuery("""
                select new com.store.dto.MonthlySales(
                        extract(month from o.orderedAt), sum(i.unitPrice * i.quantity))
                from Order o
                join o.items i
                where extract(year from o.orderedAt) = :year
                  and o.status in (:statuses)
                group by extract(month from o.orderedAt)
                order by extract(month from o.orderedAt)
                """, MonthlySales.class)
                .setParameter("year", year)
                .setParameter("statuses", SOLD)
                .getResultList();
    }


    public int applyDiscount(String category, double percent) {
        if (percent <= 0 || percent >= 100) {
            throw new IllegalArgumentException("percent must be between 0 and 100 (exclusive)");
        }
        BigDecimal factor = BigDecimal.ONE.subtract(BigDecimal.valueOf(percent).movePointLeft(2));

        em.flush();
        int updated = em.createQuery("""
                update Product p
                set p.price = round(p.price * :factor, 2)
                where p.id in (select p2.id from Product p2
                               join p2.categories c
                               where c.name = :category)
                """)
                .setParameter("factor", factor)
                .setParameter("category", category)
                .executeUpdate();
        em.clear();
        return updated;
    }
}