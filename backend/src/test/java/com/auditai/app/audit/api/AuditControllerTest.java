package com.auditai.app.audit.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.auditai.app.audit.application.port.in.CreateAuditUseCase;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import com.auditai.app.shared.api.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
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

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    createAuditUseCase = Mockito.mock(CreateAuditUseCase.class);
    AuditController controller = new AuditController(createAuditUseCase);
    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void shouldReturn202WhenPayloadIsValid() throws Exception {
    Audit audit = Audit.builder()
        .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        .status(AuditStatus.PENDING)
        .createdAt(LocalDateTime.of(2026, 4, 29, 20, 0, 0))
        .build();
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
}
