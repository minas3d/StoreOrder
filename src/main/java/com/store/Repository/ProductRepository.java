package com.store.Repository;


import com.store.model.Category;
import com.store.model.Product;
import jakarta.persistence.EntityManager;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ProductRepository {
    @PersistenceContext
    private  EntityManager em ;
    public Product  save(Product p){
        if(p.getId()==null){
            em.persist(p);
        }
        return  em.merge(p);

    }
    public Product findById(Long id ){
        return em.find(Product.class,id);
    }
    public Optional<Product> findBySku(String sku){
      return em.createQuery("select p from Product p where p.sku = :sku", Product.class)
                .setParameter("sku",sku).
                getResultStream().findFirst();
    }
    public List<Product> findByCategory(String categoryName){
        return em.createQuery(
                "select p from Product p Join p.categories c where c.name = :categoryName",Product.class)
                .setParameter("categoryName", categoryName)
                .getResultList();
    }
    public List<Product> search(String keyword, BigDecimal minPrice, BigDecimal maxPrice, String category){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> cq= cb.createQuery(Product.class);
        Root<Product> p =cq.from(Product.class);
        List<Predicate> predicates= new ArrayList<>();
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(p.get("name")), pattern),
                    cb.like(cb.lower(p.get("sku")), pattern)));
        }
        if (minPrice!=null ){
            predicates.add(cb.greaterThanOrEqualTo(p.<BigDecimal>get("price"), minPrice));

        }
        if(maxPrice !=null){

            predicates.add(cb.lessThanOrEqualTo(p.<BigDecimal>get("price"),maxPrice));
        }
        if (category!= null && !category.isBlank())
        {
            Join<Product, Category> c = p.join("categories");
            predicates.add(cb.equal(c.get("name"), category.trim()));
        }
        cq.select(p)
                .where(predicates.toArray(new Predicate[0]))
                .distinct(true)
                .orderBy(cb.asc(p.get("name")));

        return em.createQuery(cq).getResultList();

    }
    public List<Product> findLowStock(int threshold){
        return em.createQuery("select p from Product p where p.stock<=:threshold order by p.stock asc ",Product.class)
                .setParameter("threshold", threshold)
                .getResultList();
    }
    public List<Product> findPage(int page, int size) {

        return em.createQuery("SELECT p FROM Product p", Product.class)
                .setFirstResult(page * size)
                .setMaxResults(size)
                .getResultList();
    }
    public  long countAll() {
        return em.createQuery("SELECT COUNT(p) FROM Product p", Long.class)
                .getSingleResult();
    }
}
