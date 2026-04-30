package com.auditai.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AuditaiApplication {

  public static void main(String[] args) {
    SpringApplication.run(AuditaiApplication.class, args);
  }
}
