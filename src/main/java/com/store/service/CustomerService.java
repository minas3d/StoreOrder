package com.store.service;

import com.store.Repository.CustomerRepository;
import com.store.exception.DuplicateCustomerException;
import com.store.model.Address;
import com.store.model.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {
    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }
    @Transactional
    public Customer register(String name,String email, Address address){
        if (repo.findByEmail(email).isPresent()){
            throw new DuplicateCustomerException("Duplicate Customer");
        }
      return   repo.save(new Customer(name,email ,address));

    }


}
