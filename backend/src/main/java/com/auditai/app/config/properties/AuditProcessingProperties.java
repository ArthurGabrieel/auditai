package com.auditai.app.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.audit.processing")
public record AuditProcessingProperties(
    @NotBlank String exchange,
    @NotBlank String routingKey,
    @NotBlank String queue,
    @NotBlank String dlq
) {
}
