package com.auditai.app.config;

import com.auditai.app.config.properties.AuditProcessingProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  @Bean
  DirectExchange auditExchange(AuditProcessingProperties properties) {
    return new DirectExchange(properties.exchange(), true, false);
  }

  @Bean
  Queue auditQueue(AuditProcessingProperties properties) {
    return QueueBuilder.durable(properties.queue())
        .deadLetterExchange(properties.exchange())
        .deadLetterRoutingKey(properties.dlq())
        .build();
  }

  @Bean
  Queue auditDlq(AuditProcessingProperties properties) {
    return QueueBuilder.durable(properties.dlq()).build();
  }

  @Bean
  Binding auditBinding(Queue auditQueue, DirectExchange auditExchange, AuditProcessingProperties properties) {
    return BindingBuilder.bind(auditQueue).to(auditExchange).with(properties.routingKey());
  }

  @Bean
  Binding auditDlqBinding(Queue auditDlq, DirectExchange auditExchange, AuditProcessingProperties properties) {
    return BindingBuilder.bind(auditDlq).to(auditExchange).with(properties.dlq());
  }

  @Bean
  JacksonJsonMessageConverter jacksonJsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }
}
