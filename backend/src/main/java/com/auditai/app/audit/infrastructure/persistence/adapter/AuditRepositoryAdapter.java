package com.auditai.app.audit.infrastructure.persistence.adapter;

import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import com.auditai.app.audit.infrastructure.persistence.entity.AuditJpaEntity;
import com.auditai.app.audit.infrastructure.persistence.mapper.AuditPersistenceMapper;
import com.auditai.app.audit.infrastructure.persistence.repository.SpringDataAuditRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

  @Override
  public Optional<Audit> findById(UUID id) {
    return springDataAuditRepository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<Audit> findAll() {
    return springDataAuditRepository.findAllByOrderByCreatedAtDesc()
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public List<Audit> findByStatus(AuditStatus status) {
    return springDataAuditRepository.findByStatusOrderByCreatedAtDesc(status)
        .stream()
        .map(mapper::toDomain)
        .toList();
  }
}
