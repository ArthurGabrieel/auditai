package com.auditai.app.config.properties;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.ai.gemini")
public record GeminiProperties(
    boolean enabled,
    String apiKey,
    String model,
    @Min(100) int timeoutMs,
    @DecimalMin("0.0") @DecimalMax("1.0") double temperature
) {
}
