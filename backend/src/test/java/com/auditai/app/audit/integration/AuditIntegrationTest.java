package com.auditai.app.audit.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auditai.app.audit.application.port.in.CreateAuditUseCase;
import com.auditai.app.audit.application.port.in.command.CreateAuditCommand;
import com.auditai.app.audit.application.port.out.AuditRepositoryPort;
import com.auditai.app.audit.domain.Audit;
import com.auditai.app.audit.domain.AuditStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@SuppressWarnings("resource")
class AuditIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17")
      .withDatabaseName("auditai")
      .withUsername("auditai")
      .withPassword("auditai");

  @Container
  static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("SPRING_DATASOURCE_URL", postgres::getJdbcUrl);
    registry.add("SPRING_DATASOURCE_USERNAME", postgres::getUsername);
    registry.add("SPRING_DATASOURCE_PASSWORD", postgres::getPassword);
    registry.add("SPRING_RABBITMQ_HOST", rabbit::getHost);
    registry.add("SPRING_RABBITMQ_PORT", () -> rabbit.getAmqpPort());
    registry.add("SPRING_RABBITMQ_USERNAME", () ->
        System.getProperty("it.rabbitmq.username", "guest"));
    registry.add("SPRING_RABBITMQ_PASSWORD", () ->
        System.getProperty("it.rabbitmq.password", "guest"));
  }

  @Autowired
  private CreateAuditUseCase createAuditUseCase;

  @Autowired
  private AuditRepositoryPort auditRepositoryPort;

  @Test
  void shouldCreateAndPersistAudit() {
    Audit created = createAuditUseCase.create(new CreateAuditCommand("time log sample"));
    UUID id = created.getId();

    assertTrue(auditRepositoryPort.findById(id).isPresent());
    assertEquals(AuditStatus.PENDING, auditRepositoryPort.findById(id).orElseThrow().getStatus());
  }
}
