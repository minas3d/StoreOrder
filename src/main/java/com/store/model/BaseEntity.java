package com.store.model;

import jakarta.persistence.*;

import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id ;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Version
    private Long version;
    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public Long getVersion() {
        return version;
    }

}
