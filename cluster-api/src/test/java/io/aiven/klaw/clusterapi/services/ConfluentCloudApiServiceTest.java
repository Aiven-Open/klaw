package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class ConfluentCloudApiServiceTest {
  ConfluentCloudApiService confluentCloudApiService;
  private Environment env;
  ClusterApiUtils clusterApiUtils;
  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    confluentCloudApiService = new ConfluentCloudApiService(env, clusterApiUtils);
    utilMethods = new UtilMethods();
  }
}
