package com.store.dto;

import com.store.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderSummary(Long orderId, String customerName, OrderStatus status,
                           Instant orderedAt, List<Line> lines, BigDecimal total) {

    public record Line(String sku, String productName, int quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) {}
}