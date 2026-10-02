package com.store.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
@Entity
public class Product extends BaseEntity{
    //sku (unique),name ,
    // price ( BigDecimal ),stock
    @Column(nullable = false,unique = true)
    private String sku;
    private String name ;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    private int stock ;
    @ManyToMany
    @JoinTable(
            name = "Product_Category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )

    private   Set<Category> categories = new HashSet<>();
    protected Product() {}

    public Product(String sku, String name, BigDecimal price, int stock) {
        setSku(sku);
        setName(name);
        setPrice(price);
        setStock(stock);
    }
    public void addCategory(Category c) {
        categories.add(c);
        c.getProducts().add(this);
    }

    public void removeCategory(Category c) {
        categories.remove(c);
        c.getProducts().remove(this);
    }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public Set<Category> getCategories() { return categories; }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setName(String name) { this.name = name; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
}
