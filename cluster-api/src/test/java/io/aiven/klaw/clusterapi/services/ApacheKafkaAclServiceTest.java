package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.enums.*;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.DeleteAclsResult;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.acl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApacheKafkaAclServiceTest {
  @Mock private ClusterApiUtils clusterApiUtils;

  @Mock private AdminClient adminClient;

  @Mock private KafkaFuture<Collection<AclBinding>> kafkaFutureCollection;

  @Mock private DescribeAclsResult describeAclsResult;

  @Mock private DeleteAclsResult deleteAclsResult;

  @Mock private AccessControlEntry accessControlEntry;

  @Mock private CreateAclsResult createAclsResult;

  @Mock private KafkaFuture<Void> kFutureVoid;

  private UtilMethods utilMethods;

  private ApacheKafkaAclService apacheKafkaAclService;

  @BeforeEach
  public void setUp() {
    apacheKafkaAclService = new ApacheKafkaAclService(clusterApiUtils);
    utilMethods = new UtilMethods();
  }

  @Test
  public void loadAcls_OperationRead() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class))).thenReturn(listAclBindings);
    when(accessControlEntry.host()).thenReturn("11.12.33.456");
    when(accessControlEntry.operation()).thenReturn(AclOperation.READ);
    when(accessControlEntry.permissionType()).thenReturn(AclPermissionType.ALLOW);

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).hasSize(1);
  }

  @Test
  public void loadAcls_OperationCreate() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    mockDescribeAclsRequest();
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class))).thenReturn(listAclBindings);
    when(accessControlEntry.host()).thenReturn("11.12.33.456");
    when(accessControlEntry.operation()).thenReturn(AclOperation.CREATE);
    when(accessControlEntry.permissionType()).thenReturn(AclPermissionType.ALLOW);

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).isEmpty();
  }

  @Test
  public void loadAclsFailure() throws Exception {
    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any())).thenThrow(new RuntimeException("Describe Acls Error"));

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).isEmpty();
  }

  @Test
  public void createProducer() throws Exception {
    ClusterAclRequest clusterAclRequest =
        getAclRequest(
            AclType.PRODUCER.value,
            null,
            AclIPPrincipleType.IP_ADDRESS.name(),
            RequestOperationType.CREATE);

    when(clusterApiUtils.getAdminClient(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    mockCreateAclsRequest();
    mockDescribeAclsRequest();
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());

    String result = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);

    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"aclSsl"})
  public void createProducerAcl_AclAlreadyExists(String aclSsl) throws Exception {
    ClusterAclRequest clusterAclRequest =
        getAclRequest(
            AclType.PRODUCER.value,
            aclSsl,
            AclIPPrincipleType.PRINCIPAL.name(),
            RequestOperationType.CREATE);

    when(clusterApiUtils.getAdminClient(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    mockDescribeAclsRequest();
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(List.of(mock(AclBinding.class)));

    String actual = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);

    String expected = "Acl already exists. success";
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"aclSsl"})
  public void updateProducerAcl(String aclSsl) throws Exception {
    ClusterAclRequest clusterAclRequest =
        getAclRequest(
            AclType.PRODUCER.value,
            aclSsl,
            AclIPPrincipleType.PRINCIPAL.name(),
            RequestOperationType.UPDATE);

    when(clusterApiUtils.getAdminClient(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    mockDeleteAclsRequest();
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(List.of(mock(AclBinding.class)));

    String actual = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);

    String expected = ApiResultStatus.SUCCESS.value;
    assertThat(actual).isEqualTo(expected);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"aclSsl"})
  public void createConsumerAcl(String aclSsl) throws Exception {
    ClusterAclRequest clusterAclRequest =
        getAclRequest(
            AclType.CONSUMER.value,
            aclSsl,
            AclIPPrincipleType.PRINCIPAL.name(),
            RequestOperationType.CREATE);

    when(clusterApiUtils.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    mockCreateAclsRequest();
    mockDescribeAclsRequest();
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());

    String result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);

    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"aclSsl"})
  public void updateConsumerAcl(String aclSsl) throws Exception {
    ClusterAclRequest clusterAclRequest =
        getAclRequest(
            AclType.CONSUMER.value,
            aclSsl,
            AclIPPrincipleType.PRINCIPAL.name(),
            RequestOperationType.UPDATE);

    when(clusterApiUtils.getAdminClient(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    mockDeleteAclsRequest();
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(List.of(mock(AclBinding.class)));

    String result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);

    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  private void mockCreateAclsRequest() {
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
  }

  private void mockDescribeAclsRequest() {
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
  }

  private void mockDeleteAclsRequest() {
    when(adminClient.deleteAcls(anyList())).thenReturn(deleteAclsResult);
    when(deleteAclsResult.all()).thenReturn(kafkaFutureCollection);
  }

  private ClusterAclRequest getAclRequest(
      String aclType,
      String aclSsl,
      String principleType,
      RequestOperationType requestOperationType) {
    return ClusterAclRequest.builder()
        .env("localhost")
        .topicName("testtopic")
        .protocol(KafkaSupportedProtocol.PLAINTEXT)
        .consumerGroup("congroup1")
        .clusterName("clusterName")
        .aclType(aclType)
        .aclIp("11.12.33.122")
        .aclSsl(aclSsl)
        .requestOperationType(requestOperationType)
        .aclNativeType(AclsNativeType.NATIVE.name())
        .aclIpPrincipleType(principleType)
        .transactionalId("transactionId")
        .build();
  }
}
