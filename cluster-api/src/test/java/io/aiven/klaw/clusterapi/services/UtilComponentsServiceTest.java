package io.aiven.klaw.clusterapi.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.AclType;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterStatus;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateAclsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DescribeAclsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// TODO From my humble point of view more work is require dto fix the test setup.
// Maybe it is also worth considering redesigning some classes involved to better adhere to shallow
// depth of test principle.
@ExtendWith(MockitoExtension.class)
public class UtilComponentsServiceTest {

  @Mock private ClusterApiUtils getAdminClient;

  @Mock private Environment env;

  @Mock private AdminClient adminClient;

  @Mock private ListTopicsResult listTopicsResult;

  @Mock private KafkaFuture<Set<String>> kafkaFuture;

  @Mock private KafkaFuture<Map<String, TopicDescription>> kafkaFutureTopicdesc;

  @Mock private KafkaFuture<Collection<AclBinding>> kafkaFutureCollection;

  @Mock private DescribeTopicsResult describeTopicsResult;

  @Mock private DescribeAclsResult describeAclsResult;

  @Mock private AccessControlEntry accessControlEntry;

  @Mock private CreateTopicsResult createTopicsResult;

  @Mock private CreateAclsResult createAclsResult;

  @Mock private Map<String, KafkaFuture<Void>> futureTocpiCreateResult;

  @Mock private KafkaFuture<Void> kFutureVoid;

  @Mock private RestTemplate restTemplate;

  private UtilMethods utilMethods;

  private UtilComponentsService utilComponentsService;

  private ApacheKafkaAclService apacheKafkaAclService;

  private ApacheKafkaTopicService apacheKafkaTopicService;

  private SchemaService schemaService;

  @BeforeEach
  public void setUp() {
    utilComponentsService = new UtilComponentsService(env, getAdminClient);
    apacheKafkaAclService = new ApacheKafkaAclService(getAdminClient);
    apacheKafkaTopicService = new ApacheKafkaTopicService(env, getAdminClient);
    schemaService = new SchemaService(getAdminClient);
    utilMethods = new UtilMethods();
  }

  // If no cluster type is defined, return
  @Test
  public void getStatusOnline() throws Exception {
    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    ClusterStatus result =
        utilComponentsService.getStatus("localhost", KafkaSupportedProtocol.PLAINTEXT, "", "kafka");
    assertThat(result).isSameAs(ClusterStatus.ONLINE);
  }

  @Test
  public void getStatusOffline1() {

    ClusterStatus result =
        utilComponentsService.getStatus("localhost", KafkaSupportedProtocol.PLAINTEXT, "", "");
    assertThat(result).isSameAs(ClusterStatus.OFFLINE);
  }

  @Test
  public void getStatusOffline2() {

    ClusterStatus result =
        utilComponentsService.getStatus("localhost", KafkaSupportedProtocol.PLAINTEXT, "", "");
    assertThat(result).isSameAs(ClusterStatus.OFFLINE);
  }

  @Test
  public void loadAcls1() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
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
  public void loadAcls2() throws Exception {
    List<AclBinding> listAclBindings = utilMethods.getListAclBindings(accessControlEntry);

    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class))).thenReturn(listAclBindings);
    when(accessControlEntry.host()).thenReturn("11.12.33.456");
    when(accessControlEntry.operation()).thenReturn(AclOperation.CREATE);
    when(accessControlEntry.permissionType()).thenReturn(AclPermissionType.ALLOW);

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).isEmpty();
  }

  @Test
  public void loadAcls3() throws Exception {
    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.describeAcls(any())).thenThrow(new RuntimeException("Describe Acls Error"));

    Set<Map<String, String>> result =
        apacheKafkaAclService.loadAcls("localhost", KafkaSupportedProtocol.PLAINTEXT, "");
    assertThat(result).isEmpty();
  }

  @Test
  public void loadTopics() throws Exception {
    Set<String> list = new HashSet<>();
    when(getAdminClient.getAdminClient(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.listTopics(any())).thenReturn(listTopicsResult);
    when(listTopicsResult.names()).thenReturn(kafkaFuture);
    when(kafkaFuture.get()).thenReturn(list);

    // Mockito seems to have trouble with stubbing default methods.
    when(adminClient.describeTopics(anyCollection())).thenReturn(describeTopicsResult);
    when(describeTopicsResult.all()).thenReturn(kafkaFutureTopicdesc);
    when(kafkaFutureTopicdesc.get(anyLong(), any(TimeUnit.class))).thenReturn(getTopicDescs());

    Set<HashMap<String, String>> result =
        apacheKafkaTopicService.loadTopics("localhost", KafkaSupportedProtocol.PLAINTEXT, "");

    HashMap<String, String> hashMap = new HashMap<>();
    hashMap.put("partitions", "2");
    hashMap.put("replicationFactor", "1");
    hashMap.put("topicName", "testtopic2");

    HashMap<String, String> hashMap1 = new HashMap<>();
    hashMap1.put("partitions", "2");
    hashMap1.put("replicationFactor", "1");
    hashMap1.put("topicName", "testtopic1");

    assertThat(result).hasSize(2);
    assertThat(hashMap).isEqualTo(new ArrayList<>(result).get(0));
    assertThat(hashMap1).isEqualTo(new ArrayList<>(result).get(1));
  }

  @Test
  public void createTopicSuccess() throws Exception {
    ClusterTopicRequest clusterTopicRequest =
        ClusterTopicRequest.builder()
            .env("localhost")
            .protocol(KafkaSupportedProtocol.PLAINTEXT)
            .topicName("testtopic")
            .partitions(1)
            .replicationFactor(Short.parseShort("1"))
            .clusterName("")
            .build();

    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createTopics(any())).thenReturn(createTopicsResult);
    when(createTopicsResult.values()).thenReturn(futureTocpiCreateResult);
    when(futureTocpiCreateResult.get(anyString())).thenReturn(kFutureVoid);

    ApiResponse result = apacheKafkaTopicService.createTopic(clusterTopicRequest);
    assertThat(result.getResult()).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  // TODO review test configuration, since an NPE is thrown, which is most likely not intended here.
  @Test
  public void createTopicFailure1() throws Exception {
    assertThatThrownBy(
            () -> {
              ClusterTopicRequest clusterTopicRequest =
                  ClusterTopicRequest.builder()
                      .env("localhost")
                      .protocol(KafkaSupportedProtocol.PLAINTEXT)
                      .topicName("testtopic")
                      .partitions(1)
                      .replicationFactor(Short.parseShort("1"))
                      .clusterName("")
                      .build();
              apacheKafkaTopicService.createTopic(clusterTopicRequest);
            })
        .isInstanceOf(Exception.class);
  }

  @Test
  public void createTopicFailure2() throws Exception {
    assertThatThrownBy(
            () -> {
              ClusterTopicRequest clusterTopicRequest =
                  ClusterTopicRequest.builder()
                      .env("localhost")
                      .protocol(KafkaSupportedProtocol.PLAINTEXT)
                      .topicName("testtopic")
                      .partitions(1)
                      .replicationFactor(Short.parseShort("1aa"))
                      .clusterName("")
                      .build();
              when(getAdminClient.getAdminClient(
                      any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
                  .thenReturn(adminClient);
              apacheKafkaTopicService.createTopic(clusterTopicRequest);
            })
        .isInstanceOf(NumberFormatException.class);
  }

  // TODO review test configuration, since an NPE is thrown, which is most likely not intended here.
  @Test
  public void createTopicFailure4() throws Exception {
    assertThatThrownBy(
            () -> {
              ClusterTopicRequest clusterTopicRequest =
                  ClusterTopicRequest.builder()
                      .env("localhost")
                      .protocol(KafkaSupportedProtocol.PLAINTEXT)
                      .topicName("testtopic1")
                      .partitions(1)
                      .replicationFactor(Short.parseShort("1aa"))
                      .clusterName("")
                      .build();
              apacheKafkaTopicService.createTopic(clusterTopicRequest);
            })
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  public void createProducerAcl1() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(getAdminClient.getAdminClient(
            anyString(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());
    String result = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);

    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createProducerAcl2() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());

    String result = apacheKafkaAclService.updateProducerAcl(clusterAclRequest);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createConsumerAcl1() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());
    String result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void createConsumerAcl2() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAclRequest(AclType.CONSUMER.value);
    when(getAdminClient.getAdminClient(any(), eq(KafkaSupportedProtocol.PLAINTEXT), anyString()))
        .thenReturn(adminClient);
    when(adminClient.createAcls(any())).thenReturn(createAclsResult);
    when(createAclsResult.all()).thenReturn(kFutureVoid);
    when(adminClient.describeAcls(any(AclBindingFilter.class))).thenReturn(describeAclsResult);
    when(describeAclsResult.values()).thenReturn(kafkaFutureCollection);
    when(kafkaFutureCollection.get(anyLong(), any(TimeUnit.class)))
        .thenReturn(Collections.emptyList());

    String result = apacheKafkaAclService.updateConsumerAcl(clusterAclRequest);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void postSchema1() {
    ClusterSchemaRequest clusterSchemaRequest = utilMethods.getSchema();
    ApiResponse apiResponse = ApiResponse.builder().result("Schema created id : 101").build();
    ResponseEntity<ApiResponse> response = new ResponseEntity<>(apiResponse, HttpStatus.OK);
    when(getAdminClient.getRequestDetails(any(), any(), any()))
        .thenReturn(Pair.of("", restTemplate));
    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(new ResponseEntity<>("Schema created id : 101", HttpStatus.OK));

    ApiResponse resultResp = schemaService.registerSchema(clusterSchemaRequest);
    assertThat(resultResp.getResult()).isEqualTo("Schema created id : 101");
  }

  @Test
  public void postSchema2() {
    ClusterSchemaRequest clusterSchemaRequest = utilMethods.getSchema();
    when(getAdminClient.getRequestDetails(any(), any(), any()))
        .thenReturn(Pair.of("", restTemplate));
    when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
        .thenReturn(
            new ResponseEntity<>(
                "Cannot retrieve SchemaRegistry Url", HttpStatus.INTERNAL_SERVER_ERROR));
    ApiResponse resultResp = schemaService.registerSchema(clusterSchemaRequest);
    assertThat(resultResp.getResult()).isEqualTo("Cannot retrieve SchemaRegistry Url");
  }

  private Map<String, TopicDescription> getTopicDescs() {
    Node node = new Node(1, "localhost", 1);

    TopicPartitionInfo topicPartitionInfo =
        new TopicPartitionInfo(2, node, List.of(node), List.of(node));
    TopicDescription tDesc =
        new TopicDescription(
            "testtopic", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    Map<String, TopicDescription> mapResults = new HashMap<>();
    mapResults.put("testtopic1", tDesc);

    tDesc =
        new TopicDescription(
            "testtopic2", true, Arrays.asList(topicPartitionInfo, topicPartitionInfo));
    mapResults.put("testtopic2", tDesc);

    return mapResults;
  }
}
