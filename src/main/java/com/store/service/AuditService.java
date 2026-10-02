package com.store.service;

import com.store.model.AuditLog;
import com.store.Repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository logs;

    public AuditService(AuditLogRepository logs) { this.logs = logs; }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String details) {
        logs.save(new AuditLog(action, details));
    }
}