package com.auditai.app.audit.infrastructure.ai;

import com.auditai.app.audit.application.exception.AiProviderUnavailableException;
import com.auditai.app.audit.application.port.out.AuditAiAnalyzerPort;
import com.auditai.app.config.properties.GeminiProperties;
import dev.langchain4j.exception.InternalServerException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.ai.gemini", name = "enabled", havingValue = "true")
public class LangChain4jGeminiAuditAiAnalyzer implements AuditAiAnalyzerPort {

  private static final String PROMPT_TEMPLATE = """
      Você é um auditor de compliance trabalhista altamente rigoroso.
      Analise os registros de ponto e identifique:
      1) Possíveis horas extras acima do limite legal de 2h/dia.
      2) Falta de intervalo intrajornada.
      Retorne em tópicos curtos, claros e objetivos.
      Registros:
      %s
      """;

  private final GeminiProperties properties;
  private final ChatModel chatModel;

  public LangChain4jGeminiAuditAiAnalyzer(GeminiProperties properties) {
    this.properties = properties;
    validateProperties();
    this.chatModel = GoogleAiGeminiChatModel.builder()
        .apiKey(properties.apiKey())
        .modelName(properties.model())
        .temperature(properties.temperature())
        .build();
  }

  @Override
  public String analyze(String timeLogContent) {
    String prompt = PROMPT_TEMPLATE.formatted(timeLogContent);
    CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> chatModel.chat(prompt));
    try {
      return future.get(properties.timeoutMs(), TimeUnit.MILLISECONDS);
    } catch (TimeoutException ex) {
      future.cancel(true);
      throw new IllegalStateException("Gemini timeout after " + properties.timeoutMs() + "ms", ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Gemini call interrupted", ex);
    } catch (ExecutionException ex) {
      Throwable cause = ex.getCause();
      if (isTemporaryUnavailable(cause)) {
        throw new AiProviderUnavailableException(
            "Gemini is temporarily unavailable (HTTP 503). Message was requeued for retry.",
            cause
        );
      }
      throw new IllegalStateException("Gemini call failed", cause);
    }
  }

  private boolean isTemporaryUnavailable(Throwable throwable) {
    if (throwable instanceof InternalServerException) {
      String message = throwable.getMessage();
      return message != null && message.contains("\"code\": 503");
    }
    return false;
  }

  private void validateProperties() {
    if (isBlank(properties.apiKey()) || isBlank(properties.model())) {
      throw new IllegalStateException("Gemini is enabled but configuration is incomplete");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
