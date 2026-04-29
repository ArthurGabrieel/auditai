package com.auditai.app.audit.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepository extends JpaRepository<Audit, UUID> {
}
