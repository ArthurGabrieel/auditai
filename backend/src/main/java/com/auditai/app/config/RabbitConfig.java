package com.auditai.app.config;

import com.auditai.app.audit.infrastructure.messaging.AuditRabbitErrorHandler;
import com.auditai.app.config.properties.AuditProcessingProperties;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
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
  DirectExchange auditRetryExchange(AuditProcessingProperties properties) {
    return new DirectExchange(properties.retryExchange(), true, false);
  }

  @Bean
  Queue auditQueue(AuditProcessingProperties properties) {
    return QueueBuilder.durable(properties.queue())
        .deadLetterExchange(properties.retryExchange())
        .deadLetterRoutingKey(properties.retryRoutingKey1())
        .build();
  }

  @Bean
  Queue auditRetryProcessingQueue1(AuditProcessingProperties properties) {
    return QueueBuilder.durable(properties.retryProcessingQueue1())
        .deadLetterExchange(properties.exchange())
        .deadLetterRoutingKey(properties.dlq())
        .build();
  }

  @Bean
  Queue auditDlq(AuditProcessingProperties properties) {
    return QueueBuilder.durable(properties.dlq()).build();
  }

  @Bean
  Queue auditRetryQueue1(AuditProcessingProperties properties) {
    return QueueBuilder.durable(properties.retryQueue1())
        .ttl((int) properties.retryDelay1Ms())
        .deadLetterExchange(properties.exchange())
        .deadLetterRoutingKey(properties.retryProcessingRoutingKey1())
        .build();
  }

  @Bean
  Binding auditBinding(Queue auditQueue, DirectExchange auditExchange, AuditProcessingProperties properties) {
    return BindingBuilder.bind(auditQueue).to(auditExchange).with(properties.routingKey());
  }

  @Bean
  Binding auditRetryProcessingBinding1(
      Queue auditRetryProcessingQueue1,
      DirectExchange auditExchange,
      AuditProcessingProperties properties
  ) {
    return BindingBuilder.bind(auditRetryProcessingQueue1).to(auditExchange).with(properties.retryProcessingRoutingKey1());
  }

  @Bean
  Binding auditDlqBinding(Queue auditDlq, DirectExchange auditExchange, AuditProcessingProperties properties) {
    return BindingBuilder.bind(auditDlq).to(auditExchange).with(properties.dlq());
  }

  @Bean
  Binding auditRetryBinding1(Queue auditRetryQueue1, DirectExchange auditRetryExchange, AuditProcessingProperties properties) {
    return BindingBuilder.bind(auditRetryQueue1).to(auditRetryExchange).with(properties.retryRoutingKey1());
  }

  @Bean
  JacksonJsonMessageConverter jacksonJsonMessageConverter() {
    return new JacksonJsonMessageConverter();
  }

  @Bean
  ConditionalRejectingErrorHandler rabbitListenerErrorHandler() {
    return new AuditRabbitErrorHandler();
  }

  @Bean
  SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      JacksonJsonMessageConverter messageConverter,
      ConditionalRejectingErrorHandler rabbitListenerErrorHandler
  ) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    factory.setDefaultRequeueRejected(false);
    factory.setErrorHandler(rabbitListenerErrorHandler);
    return factory;
  }
}
