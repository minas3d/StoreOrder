package com.store;

import com.store.config.JpaConfig;
import com.store.model.Address;
import com.store.model.Customer;
import com.store.model.Order;
import com.store.model.PaymentMethod;
import com.store.model.Product;
import com.store.Repository.AuditLogRepository;
import com.store.Repository.ProductRepository;
import com.store.service.CustomerService;
import com.store.service.OrderService;
import com.store.service.ProductService;
import com.store.service.ReportService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.math.BigDecimal;
import java.time.Year;
import java.util.LinkedHashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        try (var ctx = new AnnotationConfigApplicationContext(JpaConfig.class)) {

            var customers = ctx.getBean(CustomerService.class);
            var products = ctx.getBean(ProductService.class);
            var orders = ctx.getBean(OrderService.class);
            var reports = ctx.getBean(ReportService.class);
            var productRepo = ctx.getBean(ProductRepository.class);
            var auditRepo = ctx.getBean(AuditLogRepository.class);

            // ---------- 1) بيانات أولية ----------
            title("1) Seed data");
            Customer mina = customers.register("Mina", "mina@test.com", address("1 Nile St", "Cairo", "Egypt"));
            Customer sara = customers.register("Sara", "sara@test.com", address("5 Tahrir Sq", "Giza", "Egypt"));

            Long phone  = products.addProduct("PH-1", "Phone",  new BigDecimal("500.00"),  10).getId();
            Long laptop = products.addProduct("LP-1", "Laptop", new BigDecimal("1000.00"), 5).getId();
            Long cable  = products.addProduct("CB-1", "Cable",  new BigDecimal("10.00"),   100).getId();
            Long mouse  = products.addProduct("MS-1", "Mouse",  new BigDecimal("20.00"),   3).getId();

//            products.assignCategory(phone, "Electronics");
//            products.assignCategory(laptop, "Electronics");
//            products.assignCategory(cable, "Accessories");
//            products.assignCategory(mouse, "Accessories");
//            System.out.println("Customers and products created");

            expectFailure("Duplicate email", () ->
                    customers.register("Mina2", "mina@test.com", address("x", "y", "z")));
            expectFailure("Duplicate SKU", () ->
                    products.addProduct("PH-1", "Phone2", new BigDecimal("1.00"), 1));

            // ---------- 2) أوردر ناجح + سعر منسوخ ----------
            title("2) placeOrder (stock decreases, price copied)");
            Order o1 = orders.placeOrder(mina.getId(), lines(phone, 2, cable, 3));
            System.out.println(orders.getOrderSummary(o1.getId()));
            System.out.println("Phone stock = " + stock(productRepo, phone) + " (expected 8)");
            System.out.println("Cable stock = " + stock(productRepo, cable) + " (expected 97)");

            // ---------- 3) pay / ship / حالات غلط ----------
            title("3) pay -> ship, and invalid transitions");
            orders.pay(o1.getId(), PaymentMethod.CARD);          // مفيش save: dirty checking
            System.out.println("After pay:  " + orders.getOrderSummary(o1.getId()).status());
            expectFailure("Pay twice", () -> orders.pay(o1.getId(), PaymentMethod.CASH));
            orders.ship(o1.getId());
            System.out.println("After ship: " + orders.getOrderSummary(o1.getId()).status());
            expectFailure("Cancel a SHIPPED order", () -> orders.cancel(o1.getId()));

            // ---------- 4) cancel بيرجّع المخزون ----------
            title("4) cancel restores stock");
            Order o2 = orders.placeOrder(mina.getId(), lines(laptop, 1));
            System.out.println("Laptop stock after order  = " + stock(productRepo, laptop) + " (expected 4)");
            orders.cancel(o2.getId());
            System.out.println("Laptop stock after cancel = " + stock(productRepo, laptop) + " (expected 5)");

            // ---------- 5) all-or-nothing + AuditLog ----------
            title("5) All-or-nothing + REQUIRES_NEW audit");
            expectFailure("Over-order", () ->
                    orders.placeOrder(mina.getId(), lines(phone, 1, laptop, 999)));
            System.out.println("Phone stock after failed order = " + stock(productRepo, phone) + " (expected 8, unchanged)");
            expectFailure("Zero quantity", () -> orders.placeOrder(mina.getId(), lines(phone, 0)));
            System.out.println("Audit log entries:");
            auditRepo.findAll().forEach(a -> System.out.println("  " + a.getAction() + " | " + a.getDetails()));

            // ---------- 6) أوردرات إضافية للتقارير ----------
            title("6) More orders for the reports");
            Order o3 = orders.placeOrder(mina.getId(), lines(laptop, 1));
            orders.pay(o3.getId(), PaymentMethod.WALLET);
            Order o4 = orders.placeOrder(sara.getId(), lines(cable, 4));
            orders.pay(o4.getId(), PaymentMethod.CASH);
            System.out.println("Orders 3 and 4 created and paid");

            // ---------- 7) ProductService + السعر المنسوخ ----------
            title("7) restock / changePrice (old orders keep their price)");
            products.restock(phone, 5);
            products.changePrice(cable, new BigDecimal("12.00"));
            System.out.println("Phone stock after restock = " + stock(productRepo, phone) + " (expected 13)");
            System.out.println("Order 1 still shows the old cable price:");
            orders.getOrderSummary(o1.getId()).lines().forEach(System.out::println);

            // ---------- 8) استعلامات الـ ProductRepository ----------
            title("8) ProductRepository queries");
            System.out.println("findByCategory(Accessories): "
                    + productRepo.findByCategory("Accessories").stream().map(Product::getName).toList());
            System.out.println("search(top, Electronics, 500..2000): "
                    + productRepo.search("top", new BigDecimal("500"), new BigDecimal("2000"), "Electronics")
                    .stream().map(Product::getName).toList());
            System.out.println("search(all nulls) count: "
                    + productRepo.search(null, null, null, null).size() + " (expected 4)");
            System.out.println("findLowStock(5): "
                    + productRepo.findLowStock(5).stream()
                    .map(p -> p.getName() + "=" + p.getStock()).toList());

            // ---------- 9) التقارير ----------
            title("9) Reports");
            System.out.println("-- revenueByCategory");
            reports.revenueByCategory().forEach(System.out::println);
            System.out.println("-- topCustomers(5)");
            reports.topCustomers(5).forEach(System.out::println);
            System.out.println("-- ordersPerStatus");
            System.out.println(reports.ordersPerStatus());
            System.out.println("-- productsNeverOrdered");
            reports.productsNeverOrdered().forEach(System.out::println);
            System.out.println("-- monthlySales(" + Year.now().getValue() + ")");
            reports.monthlySales(Year.now().getValue()).forEach(System.out::println);

            // ---------- 10) bulk update ----------
            title("10) Bulk discount (single UPDATE + clear)");
            int updated = reports.applyDiscount("Electronics", 10);
            System.out.println("Rows updated = " + updated + " (expected 2)");
            System.out.println("Phone price  = " + productRepo.findById(phone).getPrice() + " (expected 450.00)");
            System.out.println("Laptop price = " + productRepo.findById(laptop).getPrice() + " (expected 900.00)");
        }
    }

    // ---------- helpers ----------

    private static Address address(String street, String city, String country) {
        Address a = new Address();
        a.setStreet(street);
        a.setCity(city);
        a.setCountry(country);
        return a;
    }

    // LinkedHashMap عشان الترتيب ثابت (Map.of ترتيبه مش مضمون)
    private static Map<Long, Integer> lines(Long p1, int q1) {
        Map<Long, Integer> m = new LinkedHashMap<>();
        m.put(p1, q1);
        return m;
    }

    private static Map<Long, Integer> lines(Long p1, int q1, Long p2, int q2) {
        Map<Long, Integer> m = lines(p1, q1);
        m.put(p2, q2);
        return m;
    }

    private static int stock(ProductRepository repo, Long productId) {
        return repo.findById(productId).getStock();
    }

    private static void expectFailure(String label, Runnable action) {
        try {
            action.run();
            System.out.println(label + " -> NO EXCEPTION (unexpected!)");
        } catch (RuntimeException e) {
            System.out.println(label + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static void title(String text) {
        System.out.println("\n===== " + text + " =====");
    }
}