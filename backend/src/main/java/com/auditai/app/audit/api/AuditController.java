package com.auditai.app.audit.api;

import com.auditai.app.audit.application.port.in.CreateAuditUseCase;
import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.domain.Audit;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/audits")
public class AuditController {

  private final CreateAuditUseCase createAuditUseCase;

  public AuditController(CreateAuditUseCase createAuditUseCase) {
    this.createAuditUseCase = createAuditUseCase;
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
}
