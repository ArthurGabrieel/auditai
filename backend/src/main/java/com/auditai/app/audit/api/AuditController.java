package com.auditai.app.audit.api;

import com.auditai.app.audit.application.port.in.CreateAuditUseCase;
import com.auditai.app.audit.application.port.in.AuditQueryUseCase;
import com.auditai.app.audit.application.port.in.view.AuditView;
import com.auditai.app.audit.application.port.in.view.CreateAuditView;
import com.auditai.app.audit.domain.AuditStatus;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/audits")
public class AuditController {

  private final CreateAuditUseCase createAuditUseCase;
  private final AuditQueryUseCase auditQueryUseCase;

  public AuditController(
      CreateAuditUseCase createAuditUseCase,
      AuditQueryUseCase auditQueryUseCase
  ) {
    this.createAuditUseCase = createAuditUseCase;
    this.auditQueryUseCase = auditQueryUseCase;
  }

  @PostMapping
  @Operation(summary = "Create a new audit and enqueue it for asynchronous processing")
  public ResponseEntity<CreateAuditView> create(@Valid @RequestBody CreateAuditRequest request) {
    CreateAuditView response = createAuditUseCase.create(request.toCommand());
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single audit by id")
  public ResponseEntity<AuditView> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(auditQueryUseCase.getById(id));
  }

  @GetMapping
  @Operation(summary = "List audits with optional status filter")
  public ResponseEntity<List<AuditView>> list(
      @Parameter(description = "Optional status filter")
      @RequestParam(required = false) AuditStatus status
  ) {
    return ResponseEntity.ok(auditQueryUseCase.list(status));
  }
}
