package com.store.Repository;

import com.store.model.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomerRepository {
    @PersistenceContext
    private EntityManager em;

    public Customer save(Customer c){
        if(c.getId()==null)
        {
            em.persist(c);
            return c;

        }
        else
            return em.merge(c);
    }
    public Customer findById(Long id ){
     return    em.find(Customer.class,id);
    }
    //c.email = :email
   // c.getEmail =email
    public Optional<Customer> findByEmail(String email ){
        return    em.createQuery("select c from Customer c where c.email = :email",Customer.class)
                .setParameter("email",email )
                .getResultStream().findFirst();
    }
    public List<Customer> findAll(){
        return em.createQuery("select c from Customer c",Customer.class).getResultList();
    }




}
