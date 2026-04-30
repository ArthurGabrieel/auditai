package com.auditai.app.audit.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auditai.app.audit.application.port.in.CreateAuditUseCase;
import com.auditai.app.audit.application.port.in.AuditQueryUseCase;
import com.auditai.app.audit.application.port.in.view.AuditView;
import com.auditai.app.audit.application.port.in.view.CreateAuditView;
import com.auditai.app.audit.domain.AuditNotFoundException;
import com.auditai.app.audit.domain.AuditStatus;
import com.auditai.app.shared.api.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuditControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private CreateAuditUseCase createAuditUseCase;
  private AuditQueryUseCase auditQueryUseCase;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    createAuditUseCase = Mockito.mock(CreateAuditUseCase.class);
    auditQueryUseCase = Mockito.mock(AuditQueryUseCase.class);
    AuditController controller = new AuditController(createAuditUseCase, auditQueryUseCase);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void shouldReturn202WhenPayloadIsValid() throws Exception {
    CreateAuditView audit = new CreateAuditView(
        UUID.fromString("11111111-1111-1111-1111-111111111111"),
        AuditStatus.PENDING,
        LocalDateTime.of(2026, 4, 29, 20, 0, 0)
    );
    when(createAuditUseCase.create(any())).thenReturn(audit);

    mockMvc.perform(post("/v1/audits")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CreateAuditRequest("log content"))))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
        .andExpect(jsonPath("$.status").value("PENDING"))
        .andExpect(jsonPath("$.createdAt").value("2026-04-29T20:00:00"));
  }

  @Test
  void shouldReturn400WhenPayloadIsInvalid() throws Exception {
    mockMvc.perform(post("/v1/audits")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new CreateAuditRequest(" "))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.fieldErrors.timeLogContent").exists());
  }

  @Test
  void shouldReturnAuditById() throws Exception {
    UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
    AuditView audit = new AuditView(
        id,
        "sample",
        AuditStatus.COMPLETED,
        null,
        null,
        1,
        LocalDateTime.of(2026, 4, 29, 20, 0, 0),
        LocalDateTime.of(2026, 4, 29, 20, 10, 0)
    );
    when(auditQueryUseCase.getById(id)).thenReturn(audit);

    mockMvc.perform(get("/v1/audits/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void shouldReturn404WhenAuditDoesNotExist() throws Exception {
    UUID id = UUID.fromString("22222222-2222-2222-2222-222222222222");
    when(auditQueryUseCase.getById(id)).thenThrow(new AuditNotFoundException(id));

    mockMvc.perform(get("/v1/audits/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Audit not found: " + id));
  }

  @Test
  void shouldListAudits() throws Exception {
    AuditView audit = new AuditView(
        UUID.fromString("33333333-3333-3333-3333-333333333333"),
        "sample",
        AuditStatus.PENDING,
        null,
        null,
        0,
        LocalDateTime.of(2026, 4, 29, 20, 0, 0),
        null
    );
    when(auditQueryUseCase.list(eq(null))).thenReturn(List.of(audit));

    mockMvc.perform(get("/v1/audits"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("33333333-3333-3333-3333-333333333333"))
        .andExpect(jsonPath("$[0].status").value("PENDING"));
  }
}
