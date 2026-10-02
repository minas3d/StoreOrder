package com.store.model;

import jakarta.persistence.*;

@Entity
public class AuditLog extends BaseEntity {

    @Column(nullable = false)
    private String action;

    @Column(nullable = false, length = 500)
    private String details;

    protected AuditLog() {}

    public AuditLog(String action, String details) {
        this.action = action;
        this.details = details;
    }

    public String getAction() { return action; }
    public String getDetails() { return details; }
}