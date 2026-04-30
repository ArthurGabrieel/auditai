package com.auditai.app.config.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.audit.processing")
public record AuditProcessingProperties(
    @NotBlank String exchange,
    @NotBlank String routingKey,
    @NotBlank String queue,
    @NotBlank String retryProcessingRoutingKey1,
    @NotBlank String retryProcessingQueue1,
    @NotBlank String dlq,
    @NotBlank String retryExchange,
    @NotBlank String retryRoutingKey1,
    @NotBlank String retryQueue1,
    @Min(1) long retryDelay1Ms
) {
}
