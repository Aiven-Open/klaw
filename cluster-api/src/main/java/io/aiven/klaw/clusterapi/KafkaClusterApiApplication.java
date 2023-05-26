package io.aiven.klaw.clusterapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaClusterApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(KafkaClusterApiApplication.class, args);
  }
}
