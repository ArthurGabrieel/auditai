package com.auditai.app.audit.infrastructure.persistence.repository;

import com.auditai.app.audit.infrastructure.persistence.entity.AuditJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuditRepository extends JpaRepository<AuditJpaEntity, UUID> {
}
