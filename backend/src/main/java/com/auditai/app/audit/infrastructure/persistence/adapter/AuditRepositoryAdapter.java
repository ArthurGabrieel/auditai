package com.auditai.app.audit.infrastructure.persistence.adapter;

import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.infrastructure.persistence.entity.AuditJpaEntity;
import com.auditai.app.audit.infrastructure.persistence.mapper.AuditPersistenceMapper;
import com.auditai.app.audit.infrastructure.persistence.repository.SpringDataAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditRepositoryAdapter implements AuditRepositoryPort {

  private final SpringDataAuditRepository springDataAuditRepository;
  private final AuditPersistenceMapper mapper;

  @Override
  public Audit save(Audit audit) {
    AuditJpaEntity saved = springDataAuditRepository.save(mapper.toEntity(audit));
    return mapper.toDomain(saved);
  }
}
