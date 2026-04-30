package com.auditai.app.audit.api;

import com.auditai.app.audit.application.port.in.CreateAuditUseCase;
import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.application.service.AuditQueryService;
import com.auditai.app.audit.domain.Audit;
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
  private final AuditQueryService auditQueryService;

  public AuditController(CreateAuditUseCase createAuditUseCase, AuditQueryService auditQueryService) {
    this.createAuditUseCase = createAuditUseCase;
    this.auditQueryService = auditQueryService;
  }

  @PostMapping
  @Operation(summary = "Create a new audit and enqueue it for asynchronous processing")
  public ResponseEntity<CreateAuditResponse> create(@Valid @RequestBody CreateAuditRequest request) {
    Audit audit = createAuditUseCase.create(new CreateAuditCommand(request.timeLogContent()));
    CreateAuditResponse response = new CreateAuditResponse(
        audit.getId(),
        audit.getStatus(),
        audit.getCreatedAt()
    );
    return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a single audit by id")
  public ResponseEntity<AuditResponse> getById(@PathVariable UUID id) {
    Audit audit = auditQueryService.getById(id);
    return ResponseEntity.ok(toResponse(audit));
  }

  @GetMapping
  @Operation(summary = "List audits with optional status filter")
  public ResponseEntity<List<AuditResponse>> list(
      @Parameter(description = "Optional status filter")
      @RequestParam(required = false) AuditStatus status
  ) {
    List<AuditResponse> response = auditQueryService.list(status).stream()
        .map(this::toResponse)
        .toList();
    return ResponseEntity.ok(response);
  }

  private AuditResponse toResponse(Audit audit) {
    return new AuditResponse(
        audit.getId(),
        audit.getTimeLogContent(),
        audit.getStatus(),
        audit.getAiOpinion(),
        audit.getErrorReason(),
        audit.getProcessingAttempts(),
        audit.getCreatedAt(),
        audit.getCompletedAt()
    );
  }
}
