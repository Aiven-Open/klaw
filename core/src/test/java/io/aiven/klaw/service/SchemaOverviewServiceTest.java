package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.response.SchemaOverview;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SchemaOverviewServiceTest {

  public static final String TESTTOPIC = "testtopic";
  public static final String TEAM_1 = "Team-1";
  public static final String TEAM_ID = "kwusera";
  private UtilMethods utilMethods;
  @Mock private UserDetails userDetails;
  @Mock private HandleDbRequestsJdbc handleDbRequests;
  @Mock private ManageDatabase manageDatabase;
  @Mock private CommonUtilsService commonUtilsService;
  @Mock private MailUtils mailService;
  @Mock private UserInfo userInfo;

  @Mock private ClusterApiService clusterApiService;

  private SchemaOverviewService schemaOverviewService;

  private ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    this.schemaOverviewService = new SchemaOverviewService(mailService);

    ReflectionTestUtils.setField(schemaOverviewService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(schemaOverviewService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(schemaOverviewService, "clusterApiService", clusterApiService);
    when(manageDatabase.getHandleDbRequests()).thenReturn(handleDbRequests);
    loginMock();
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  @Order(1)
  public void givenASchemaWithOnlyOneSchemaEnv_ReturnNoPromotion() throws Exception {
    stubUserInfo();
    when(commonUtilsService.getTeamId(anyString())).thenReturn(10);
    when(handleDbRequests.getAllTopicsByTopicNameAndTeamIdAndTenantId(
            eq(TESTTOPIC), eq(10), eq(101)))
        .thenReturn(List.of(createTopic(TESTTOPIC, "1")));
    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.SCHEMA_REGISTRY, 1);

    when(commonUtilsService.getSchemaPromotionEnvsFromKafkaEnvs(eq(101))).thenReturn("3");

    SchemaOverview returnedValue =
        schemaOverviewService.getSchemaOfTopic(TESTTOPIC, "1", Collections.singletonList("1"));

    assertThat(returnedValue.getSchemaPromotionDetails()).isNotNull();
    assertThat(returnedValue.getSchemaPromotionDetails().get("DEV").containsKey("status")).isTrue();
    assertThat(returnedValue.getSchemaPromotionDetails().get("DEV").get("status"))
        .isEqualTo("NO_PROMOTION");
  }

  @Test
  @Order(2)
  public void getGivenASchemaWithManySchemaEnv_ReturnNextInPromotion() throws Exception {
    stubUserInfo();

    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.SCHEMA_REGISTRY, 5);

    when(commonUtilsService.getSchemaPromotionEnvsFromKafkaEnvs(eq(101))).thenReturn("3,4");
    when(commonUtilsService.getTeamId(anyString())).thenReturn(10);
    when(handleDbRequests.getAllTopicsByTopicNameAndTeamIdAndTenantId(
            eq(TESTTOPIC), eq(10), eq(101)))
        .thenReturn(List.of(createTopic(TESTTOPIC, "1")));
    SchemaOverview returnedValue =
        schemaOverviewService.getSchemaOfTopic(TESTTOPIC, "1", List.of("1", "2"));

    assertThat(returnedValue.getSchemaPromotionDetails()).isNotNull();
    assertThat(returnedValue.getSchemaPromotionDetails().get("DEV").containsKey("status")).isTrue();
    assertThat(returnedValue.getSchemaPromotionDetails().get("DEV").get("status"))
        .isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(returnedValue.getSchemaPromotionDetails().get("DEV").get("sourceEnv"))
        .isEqualTo("3");
    assertThat(returnedValue.getSchemaPromotionDetails().get("DEV").get("targetEnv"))
        .isEqualTo("test-4");
  }

  @Test
  @Order(3)
  public void givenASchemaThatDoesntExist_ReturnNoPromotion() throws Exception {
    stubUserInfo();
    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.SCHEMA_REGISTRY, 5);

    when(commonUtilsService.getSchemaPromotionEnvsFromKafkaEnvs(eq(101))).thenReturn("1");

    SchemaOverview returnedValue =
        schemaOverviewService.getSchemaOfTopic(TESTTOPIC, "3", Collections.singletonList("1"));
    assertThat(returnedValue.getSchemaPromotionDetails()).isNullOrEmpty();
    assertThat(returnedValue.isSchemaExists()).isFalse();
  }

  @Test
  @Order(4)
  public void
      getGivenASchemaWithManySchemaEnv_WithRequestorFromDifferentTeam_DoNotReturnNextInPromotion()
          throws Exception {
    stubUserInfo();

    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.SCHEMA_REGISTRY, 5);

    when(commonUtilsService.getSchemaPromotionEnvsFromKafkaEnvs(eq(101))).thenReturn("3,4");
    when(commonUtilsService.getTeamId(anyString())).thenReturn(8);
    when(handleDbRequests.getTopics(eq(TESTTOPIC), eq(101)))
        .thenReturn(List.of(createTopic(TESTTOPIC, "1")));
    SchemaOverview returnedValue =
        schemaOverviewService.getSchemaOfTopic(TESTTOPIC, "1", Collections.singletonList("1"));

    assertThat(returnedValue.getSchemaPromotionDetails()).isNull();
  }

  private Topic createTopic(String topicName, String envName) {
    Topic t = new Topic();
    t.setTopicname(topicName);
    t.setTopicid(1);
    t.setEnvironment(envName);
    t.setTeamId(10);
    return t;
  }

  private List<Env> createListOfEnvs(KafkaClustersType clusterType, int numberOfEnvs) {
    ArrayList<Env> envs = new ArrayList<>();
    for (int i = 1; i <= numberOfEnvs; i++) {
      Env e = new Env();
      e.setEnvExists("true");
      e.setName("test-" + i);
      e.setId(String.valueOf(i));
      e.setTenantId(101);
      e.setClusterId(i);
      e.setType(clusterType.value);
      envs.add(e);
    }

    return envs;
  }

  private KwClusters createCluster(KafkaClustersType clusterType) {
    KwClusters cluster = new KwClusters();
    cluster.setClusterId(1);
    cluster.setClusterType(clusterType.value);
    cluster.setBootstrapServers("server:8081");
    cluster.setTenantId(101);
    return cluster;
  }

  private TreeMap<Integer, Map<String, Object>> createSchemaList() throws JsonProcessingException {
    String schemav2 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"2\", \"id\":\"3\", \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvro\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";
    String schemav1 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"1\", \"id\":\"2\", \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvro\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";

    TreeMap<Integer, Map<String, Object>> allVersionSchemas =
        new TreeMap<>(Collections.reverseOrder());
    allVersionSchemas.put(1, mapper.readValue(schemav1, Map.class));
    allVersionSchemas.put(2, mapper.readValue(schemav2, Map.class));

    return allVersionSchemas;
  }

  private void stubSchemaPromotionInfo(
      String testtopic, KafkaClustersType clusterType, int numberOfEnvs) throws Exception {
    List<Env> listOfEnvs = createListOfEnvs(KafkaClustersType.SCHEMA_REGISTRY, numberOfEnvs);
    when(manageDatabase.getAllEnvList(101)).thenReturn(listOfEnvs);
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(handleDbRequests.selectAllSchemaRegEnvs(101)).thenReturn(listOfEnvs);
    when(manageDatabase.getClusters(eq(KafkaClustersType.SCHEMA_REGISTRY), eq(101)))
        .thenReturn(createClusterMap(numberOfEnvs));
    when(clusterApiService.getAvroSchema(any(), any(), any(), eq(testtopic), eq(101)))
        .thenReturn(createSchemaList())
        .thenReturn(null);

    Env kafkaEnv1 = new Env();
    kafkaEnv1.setName("DEV");
    kafkaEnv1.setId("1");
    kafkaEnv1.setClusterId(1);
    EnvTag envTag = new EnvTag();
    envTag.setId("3");
    envTag.setName("DEV");
    kafkaEnv1.setAssociatedEnv(envTag);

    Env kafkaEnv2 = new Env();
    kafkaEnv2.setName("TST");
    kafkaEnv2.setId("2");
    kafkaEnv2.setClusterId(2);
    EnvTag envTag2 = new EnvTag();
    envTag2.setId("4");
    envTag2.setName("TST");
    kafkaEnv2.setAssociatedEnv(envTag2);

    Env schemaEnv1 = new Env();
    schemaEnv1.setId("3");
    schemaEnv1.setName("DEV");
    schemaEnv1.setClusterId(1);
    schemaEnv1.setAssociatedEnv(new EnvTag("1", "DEV"));

    Env schemaEnv2 = new Env();
    schemaEnv2.setId("4");
    schemaEnv2.setName("TST");
    schemaEnv2.setClusterId(1);
    schemaEnv2.setAssociatedEnv(new EnvTag("2", "TST"));

    when(handleDbRequests.selectEnvDetails("1", 101)).thenReturn(kafkaEnv1);
    when(handleDbRequests.selectEnvDetails("2", 101)).thenReturn(kafkaEnv2);
    when(handleDbRequests.selectEnvDetails("3", 101)).thenReturn(schemaEnv1);
    when(handleDbRequests.selectEnvDetails("4", 101)).thenReturn(schemaEnv2);
  }

  private void stubKafkaPromotion(String testtopic, int numberOfEnvs) throws Exception {

    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(
            new HashSet<>() {
              {
                add("1");
                add("2");
                add("3");
                add("4");
                add("5");
                add("6");
              }
            });

    mockTenantConfig();

    when(manageDatabase.getTeamNameFromTeamId(101, 10)).thenReturn(TEAM_1);
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(List.of(createTopic(TESTTOPIC, "DEV")));
    when(manageDatabase.getClusters(KafkaClustersType.SCHEMA_REGISTRY, 101))
        .thenReturn(createClusterMap(numberOfEnvs));
  }

  private Map<Integer, KwClusters> createClusterMap(int numberOfClusters) {
    Map<Integer, KwClusters> map = new HashMap<>();

    for (int i = 0; i < numberOfClusters; i++) {
      map.put(i + 1, createCluster(KafkaClustersType.SCHEMA_REGISTRY));
    }
    return map;
  }

  private void mockTenantConfig() {
    when(manageDatabase.getTenantConfig())
        .thenReturn(
            new HashMap<>() {
              {
                put(101, createModel());
              }
            });
  }

  private KwTenantConfigModel createModel() {
    KwTenantConfigModel model = new KwTenantConfigModel();
    model.setBaseSyncEnvironment("1");
    model.setOrderOfTopicPromotionEnvsList(List.of("1", "2", "3", "4", "5", "6", "7"));
    return model;
  }

  private List<Acl> getAclsSOT(String topicName) {
    List<Acl> aclList = new ArrayList<>();

    Acl aclReq = new Acl();
    aclReq.setReq_no(1001);
    aclReq.setTopicname(topicName);
    aclReq.setTeamId(1);
    aclReq.setAclip("2.1.2.1");
    aclReq.setAclssl(null);
    aclReq.setEnvironment("1");
    aclReq.setConsumergroup("mygrp1");
    aclReq.setAclType(AclType.CONSUMER.value);

    aclList.add(aclReq);
    return aclList;
  }

  private void stubUserInfo() {
    when(handleDbRequests.getUsersInfo(anyString())).thenReturn(userInfo);
    when(userInfo.getTeamId()).thenReturn(10);
    when(mailService.getUserName(any())).thenReturn(TEAM_ID);
    when(commonUtilsService.getTeamId(eq(TEAM_ID))).thenReturn(10);
  }
}
