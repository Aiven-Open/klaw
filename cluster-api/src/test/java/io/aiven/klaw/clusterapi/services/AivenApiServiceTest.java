package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AivenApiServiceTest {

  AivenApiService aivenApiService;

  @BeforeEach
  public void setUp() {
    aivenApiService = new AivenApiService();
  }

  @Test
  @Disabled
  public void getAclsListTest() throws Exception {
    // TODO when, asserts
    aivenApiService.listAcls("dev-sandbox", "kafka-acls-kw");
  }

  @Disabled
  @Test
  public void createAclsTest() throws Exception {
    // TODO when, asserts
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .permission("read")
            .topicName("testtopic")
            .username("avnadmin")
            .projectName("testproject")
            .serviceName("serviceName")
            .build();

    aivenApiService.createAcls(clusterAclRequest);
  }

  @Disabled
  @Test
  public void deleteAclsTest() throws Exception {
    // TODO when, asserts
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .aivenAclKey("4322342")
            .projectName("testproject")
            .serviceName("serviceName")
            .build();

    aivenApiService.deleteAcls(clusterAclRequest);
  }
}
