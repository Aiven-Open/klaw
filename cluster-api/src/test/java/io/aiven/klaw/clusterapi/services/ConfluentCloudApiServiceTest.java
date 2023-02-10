package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.UtilMethods;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ConfluentCloudApiServiceTest {
  ConfluentCloudApiService confluentCloudApiService;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    confluentCloudApiService = new ConfluentCloudApiService();
    utilMethods = new UtilMethods();
  }
}
