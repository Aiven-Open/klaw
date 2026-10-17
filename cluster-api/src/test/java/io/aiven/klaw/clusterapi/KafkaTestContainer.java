package io.aiven.klaw.clusterapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration(proxyBeanMethods = false)
public class KafkaTestContainer {

  @Bean
  @ServiceConnection(name = "Kafka")
  public GenericContainer<?> kafkaContainer() {
    return new GenericContainer<>("apache/kafka-native:3.8.0");
  }
}
