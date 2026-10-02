package com.store.service;

import com.store.dto.CategoryRevenue;
import com.store.dto.CustomerSpend;
import com.store.dto.MonthlySales;
import com.store.model.OrderStatus;
import com.store.dto.*;
import com.store.Repository.ReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final ReportRepository reports;

    public ReportService(ReportRepository reports) { this.reports = reports; }

    @Transactional(readOnly = true)
    public List<CategoryRevenue> revenueByCategory() { return reports.revenueByCategory(); }

    @Transactional(readOnly = true)
    public List<CustomerSpend> topCustomers(int limit) { return reports.topCustomers(limit); }

    @Transactional(readOnly = true)
    public Map<OrderStatus, Long> ordersPerStatus() { return reports.ordersPerStatus(); }

    @Transactional(readOnly = true)
    public List<ProductInfo> productsNeverOrdered() { return reports.productsNeverOrdered(); }

    @Transactional(readOnly = true)
    public List<MonthlySales> monthlySales(int year) { return reports.monthlySales(year); }

    @Transactional
    public int applyDiscount(String category, double percent) {
        return reports.applyDiscount(category, percent);
    }
}