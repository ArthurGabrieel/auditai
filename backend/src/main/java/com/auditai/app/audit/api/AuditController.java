package com.auditai.app.audit.api;

import com.auditai.app.audit.application.AuditService;
import com.auditai.app.audit.domain.Audit;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

  private final AuditService auditService;

  public AuditController(AuditService auditService) {
    this.auditService = auditService;
  }

  @PostMapping
  public ResponseEntity<CreateAuditResponse> create(@Valid @RequestBody CreateAuditRequest request) {
    Audit audit = auditService.create(request);
    CreateAuditResponse response = new CreateAuditResponse(
        audit.getId(),
        audit.getStatus(),
        audit.getCreatedAt()
    );
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }
}
