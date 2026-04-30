package com.auditai.app.audit.infrastructure.persistence.repository;

import com.auditai.app.audit.infrastructure.persistence.entity.AuditJpaEntity;
import com.auditai.app.audit.domain.AuditStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataAuditRepository extends JpaRepository<AuditJpaEntity, UUID> {
  List<AuditJpaEntity> findAllByOrderByCreatedAtDesc();
  List<AuditJpaEntity> findByStatusOrderByCreatedAtDesc(AuditStatus status);
}
