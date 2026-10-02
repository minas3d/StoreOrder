package com.store.service;

import com.store.Repository.ProductRepository;
import com.store.exception.DuplicateProductException;
import com.store.exception.InvalidProductException;
import com.store.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {
    private final ProductRepository repo ;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }
    @Transactional
   public Product addProduct( String sku,String name,BigDecimal price,int stock ){
        if (repo.findBySku(sku).isPresent()){
            throw new DuplicateProductException(sku);
        }
         if (price == null || price.signum()<0 ){
             throw new IllegalArgumentException("price must be positive");
         }
        if(stock<=0){
            throw new IllegalArgumentException("price must be positive");
        }
        return repo.save(new Product(sku,name,price,stock));
    }

   @Transactional
   public void restock(Long productId, int quantity){
        Product p= repo.findById(productId);
        if(p==null){
            throw new InvalidProductException("in valid not found ");
        }
        p.setStock(p.getStock() + quantity);
   }

    @Transactional
    public void changePrice(Long productId,BigDecimal newPrice){
       Product p =repo.findById(productId);
        if (p ==null){
            throw new InvalidProductException("not found Product");
        }
        if (newPrice.signum()<0){
            throw new InvalidProductException("in valid must be posative ");
        }
        p.setPrice(newPrice);
    }


}
