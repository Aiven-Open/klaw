package io.aiven.klaw.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.helpers.db.rdbms.HandleDbRequestsJdbc;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.KwTenantConfigModel;
import io.aiven.klaw.model.SchemaOverview;
import io.aiven.klaw.model.TopicOverview;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.ArrayList;
import java.util.Arrays;
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
public class TopicOverviewServiceTest {

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

  @Mock private Map<Integer, KwClusters> kwClustersHashMap;
  @Mock private KwClusters kwClusters;

  private TopicOverviewService topicOverviewService;

  private ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  public void setUp() throws Exception {
    utilMethods = new UtilMethods();
    this.topicOverviewService = new TopicOverviewService(mailService);

    Env env = new Env();
    env.setName("DEV");
    env.setId("1");
    ReflectionTestUtils.setField(topicOverviewService, "manageDatabase", manageDatabase);
    ReflectionTestUtils.setField(topicOverviewService, "commonUtilsService", commonUtilsService);
    ReflectionTestUtils.setField(topicOverviewService, "clusterApiService", clusterApiService);
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
  public void getAclsSyncFalse1() throws KlawException {
    String env1 = "1";
    stubUserInfo();

    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getKwPropertyValue(anyString(), anyInt())).thenReturn("true");
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(utilMethods.getTeams());
    when(handleDbRequests.getTopics(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(TESTTOPIC));
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt()))
        .thenReturn(getAclsSOT(TESTTOPIC));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(TESTTOPIC));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(utilMethods.getTopics(TESTTOPIC));
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(kwClustersHashMap);
    when(kwClustersHashMap.get(anyInt())).thenReturn(kwClusters);

    when(manageDatabase.getAllEnvList(anyInt()))
        .thenReturn(createListOfEnvs(KafkaClustersType.SCHEMA_REGISTRY, 5));
    when(mailService.getEnvProperty(eq(101), eq("REQUEST_TOPICS_OF_ENVS"))).thenReturn("1");
    mockTenantConfig();
    List<AclInfo> aclList = topicOverviewService.getTopicOverview(TESTTOPIC).getAclInfoList();

    assertThat(aclList).hasSize(1);

    assertThat(aclList.get(0).getTopicname()).isEqualTo(TESTTOPIC);
    assertThat(aclList.get(0).getConsumergroup()).isEqualTo("mygrp1");
    assertThat(aclList.get(0).getAcl_ip()).isEqualTo("2.1.2.1");
  }

  @Test
  @Order(2)
  public void getAclsSyncFalse2() {
    String topicNameSearch = "testnewtopic1";

    stubUserInfo();
    when(commonUtilsService.getEnvsFromUserId(anyString()))
        .thenReturn(new HashSet<>(Collections.singletonList("1")));
    when(manageDatabase.getKwPropertyValue(anyString(), anyInt())).thenReturn("true");
    when(manageDatabase.getKafkaEnvList(anyInt())).thenReturn(utilMethods.getEnvLists());
    when(handleDbRequests.selectAllTeamsOfUsers(anyString(), anyInt()))
        .thenReturn(utilMethods.getTeams());
    when(handleDbRequests.getTopics(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(topicNameSearch));
    when(handleDbRequests.getSyncAcls(anyString(), anyString(), anyInt()))
        .thenReturn(getAclsSOT(topicNameSearch));
    when(handleDbRequests.getTopicTeam(anyString(), anyInt()))
        .thenReturn(utilMethods.getTopics(topicNameSearch));
    when(commonUtilsService.getFilteredTopicsForTenant(any()))
        .thenReturn(utilMethods.getTopics(topicNameSearch));
    when(manageDatabase.getClusters(any(KafkaClustersType.class), anyInt()))
        .thenReturn(kwClustersHashMap);
    when(kwClustersHashMap.get(anyInt())).thenReturn(kwClusters);

    when(manageDatabase.getAllEnvList(anyInt()))
        .thenReturn(createListOfEnvs(KafkaClustersType.SCHEMA_REGISTRY, 5));
    when(mailService.getEnvProperty(eq(101), eq("REQUEST_TOPICS_OF_ENVS"))).thenReturn("1");
    mockTenantConfig();

    List<AclInfo> aclList = topicOverviewService.getTopicOverview(topicNameSearch).getAclInfoList();

    assertThat(aclList).hasSize(1);

    assertThat(aclList.get(0).getTopicname()).isEqualTo(topicNameSearch);
    assertThat(aclList.get(0).getConsumergroup()).isEqualTo("mygrp1");
    assertThat(aclList.get(0).getAcl_ip()).isEqualTo("2.1.2.1");
  }

  @Test
  @Order(3)
  public void givenASchemaWithOnlyOneSchemaEnv_ReturnNoPromotion() throws Exception {
    stubUserInfo();

    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.SCHEMA_REGISTRY, 1);

    when(mailService.getEnvProperty(eq(101), eq("ORDER_OF_SCHEMA_ENVS"))).thenReturn("1");

    SchemaOverview returnedValue = topicOverviewService.getSchemaOfTopic(TESTTOPIC, "1");

    assertThat(returnedValue.getSchemaPromotionDetails()).isNotNull();
    assertThat(returnedValue.getSchemaPromotionDetails().get("test-1").containsKey("status"))
        .isTrue();
    assertThat(returnedValue.getSchemaPromotionDetails().get("test-1").get("status"))
        .isEqualTo("NO_PROMOTION");
  }

  @Test
  @Order(4)
  public void getGivenASchemaWithManySchemaEnv_ReturnNextInPromotion() throws Exception {
    stubUserInfo();

    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.SCHEMA_REGISTRY, 15);

    when(mailService.getEnvProperty(eq(101), eq("ORDER_OF_SCHEMA_ENVS")))
        .thenReturn("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");

    SchemaOverview returnedValue = topicOverviewService.getSchemaOfTopic(TESTTOPIC, "1");

    assertThat(returnedValue.getSchemaPromotionDetails()).isNotNull();
    assertThat(returnedValue.getSchemaPromotionDetails().get("test-1").containsKey("status"))
        .isTrue();
    assertThat(returnedValue.getSchemaPromotionDetails().get("test-1").get("status"))
        .isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(returnedValue.getSchemaPromotionDetails().get("test-1").get("sourceEnv"))
        .isEqualTo("1");
    assertThat(returnedValue.getSchemaPromotionDetails().get("test-1").get("targetEnv"))
        .isEqualTo("test-2");
  }

  @Test
  @Order(5)
  public void givenATopicWithOnlyOneKafkaEnv_ReturnNoPromotion() throws Exception {
    stubUserInfo();
    stubKafkaPromotion(TESTTOPIC, 1);
    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.KAFKA, 15);
    when(handleDbRequests.getTopics(TESTTOPIC, 101))
        .thenReturn(Arrays.asList(createTopic(TESTTOPIC)));
    when(mailService.getEnvProperty(eq(101), eq("REQUEST_TOPICS_OF_ENVS"))).thenReturn("1");
    when(mailService.getEnvProperty(eq(101), eq("ORDER_OF_ENVS"))).thenReturn("1");

    TopicOverview returnedValue = topicOverviewService.getTopicOverview(TESTTOPIC);
    assertThat(returnedValue.getTopicPromotionDetails()).isNotNull();
    assertThat(returnedValue.getTopicPromotionDetails().containsKey("status")).isTrue();
    assertThat(returnedValue.getTopicPromotionDetails().get("status")).isEqualTo("NO_PROMOTION");
  }

  @Test
  @Order(6)
  public void getGivenATopicWithManyKafkaEnv_ReturnNextInPromotion() throws Exception {
    stubUserInfo();
    stubKafkaPromotion(TESTTOPIC, 15);
    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.KAFKA, 15);
    when(handleDbRequests.getTopics(TESTTOPIC, 101))
        .thenReturn(Arrays.asList(createTopic(TESTTOPIC)));
    when(mailService.getEnvProperty(eq(101), eq("REQUEST_TOPICS_OF_ENVS")))
        .thenReturn("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
    when(mailService.getEnvProperty(eq(101), eq("ORDER_OF_ENVS")))
        .thenReturn("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");

    TopicOverview returnedValue = topicOverviewService.getTopicOverview(TESTTOPIC);
    assertThat(returnedValue.getTopicPromotionDetails()).isNotNull();
    assertThat(returnedValue.getTopicPromotionDetails().containsKey("status")).isTrue();
    assertThat(returnedValue.getTopicPromotionDetails().get("status"))
        .isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(returnedValue.getTopicPromotionDetails().get("sourceEnv")).isEqualTo("1");
    assertThat(returnedValue.getTopicPromotionDetails().get("targetEnv")).isEqualTo("test-2");
  }

  @Test
  @Order(7)
  public void givenATopicThatDoesntExist_ReturnNoPromotion() throws Exception {
    stubUserInfo();
    stubKafkaPromotion(TESTTOPIC, 1);
    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.KAFKA, 15);

    when(mailService.getEnvProperty(eq(101), eq("ORDER_OF_ENVS"))).thenReturn("1");

    TopicOverview returnedValue = topicOverviewService.getTopicOverview(TESTTOPIC);
    assertThat(returnedValue.getTopicPromotionDetails()).isNullOrEmpty();
    assertThat(returnedValue.isTopicExists()).isFalse();
  }

  @Test
  @Order(8)
  public void givenASchemaThatDoesntExist_ReturnNoPromotion() throws Exception {
    stubUserInfo();
    stubSchemaPromotionInfo(TESTTOPIC, KafkaClustersType.SCHEMA_REGISTRY, 5);

    when(mailService.getEnvProperty(eq(101), eq("ORDER_OF_SCHEMA_ENVS"))).thenReturn("1");

    SchemaOverview returnedValue = topicOverviewService.getSchemaOfTopic(TESTTOPIC, "3");
    assertThat(returnedValue.getSchemaPromotionDetails()).isNullOrEmpty();
    assertThat(returnedValue.isSchemaExists()).isFalse();
  }

  private Topic createTopic(String topicName) {
    Topic t = new Topic();
    t.setTopicname(topicName);
    t.setTopicid(1);
    t.setEnvironment("1");
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
    when(manageDatabase.getAllEnvList(101))
        .thenReturn(createListOfEnvs(KafkaClustersType.SCHEMA_REGISTRY, numberOfEnvs));
    when(commonUtilsService.getTenantId(any())).thenReturn(101);
    when(handleDbRequests.selectAllSchemaRegEnvs(101))
        .thenReturn(createListOfEnvs(KafkaClustersType.SCHEMA_REGISTRY, numberOfEnvs));
    when(manageDatabase.getClusters(KafkaClustersType.SCHEMA_REGISTRY, 101))
        .thenReturn(createClusterMap(numberOfEnvs));
    when(clusterApiService.getAvroSchema(any(), any(), any(), eq(testtopic), eq(101)))
        .thenReturn(createSchemaList())
        .thenReturn(null);
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
        .thenReturn(List.of(createTopic(TESTTOPIC)));
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
    aclReq.setTopictype(AclType.CONSUMER.value);

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
