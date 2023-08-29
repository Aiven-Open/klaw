package io.aiven.klaw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.constants.TestConstants;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.ServiceAccounts;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.*;
import io.aiven.klaw.model.charts.ChartsJsOverview;
import io.aiven.klaw.model.charts.Options;
import io.aiven.klaw.model.charts.TeamOverview;
import io.aiven.klaw.model.charts.Title;
import io.aiven.klaw.model.cluster.SchemaInfoOfTopic;
import io.aiven.klaw.model.cluster.SchemasInfoOfClusterResponse;
import io.aiven.klaw.model.cluster.consumergroup.OffsetResetType;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclPermissionType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import io.aiven.klaw.model.enums.MetadataOperationType;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.requests.ConsumerOffsetResetRequestModel;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.ResetEntityCache;
import io.aiven.klaw.model.requests.SchemaPromotion;
import io.aiven.klaw.model.requests.SchemaRequestModel;
import io.aiven.klaw.model.requests.TopicCreateRequestModel;
import io.aiven.klaw.model.requests.TopicUpdateRequestModel;
import io.aiven.klaw.model.requests.UserInfoModel;
import io.aiven.klaw.model.response.AclOverviewInfo;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.EnvIdInfo;
import io.aiven.klaw.model.response.EnvModelResponse;
import io.aiven.klaw.model.response.RequestEntityStatusCount;
import io.aiven.klaw.model.response.RequestStatusCount;
import io.aiven.klaw.model.response.RequestsCountOverview;
import io.aiven.klaw.model.response.RequestsOperationTypeCount;
import io.aiven.klaw.model.response.SchemaRequestsResponseModel;
import io.aiven.klaw.model.response.SchemaSubjectInfoResponse;
import io.aiven.klaw.model.response.SyncSchemasList;
import io.aiven.klaw.model.response.TeamModelResponse;
import io.aiven.klaw.model.response.TopicConfig;
import io.aiven.klaw.model.response.TopicOverview;
import io.aiven.klaw.model.response.TopicRequestsResponseModel;
import io.aiven.klaw.model.response.UserInfoModelResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.HttpHeaders;

@Slf4j
public class UtilMethods {

  public HttpHeaders createHeaders(String username, String password) {
    return new HttpHeaders() {
      {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.US_ASCII), false);
        String authHeader = "Basic " + new String(encodedAuth);
        set("Authorization", authHeader);
      }
    };
  }

  public List<MessageSchema> getMSchemas() {
    List<MessageSchema> listMSchemas = new ArrayList<>();
    MessageSchema mSchema = new MessageSchema();
    mSchema.setEnvironment("DEV");
    mSchema.setSchemaversion("1.0");
    mSchema.setTopicname("testtopic");
    mSchema.setSchemafull("schema");
    mSchema.setTenantId(101);
    listMSchemas.add(mSchema);

    return listMSchemas;
  }

  public UserInfoModel getUserInfoMock() {
    UserInfoModel userInfo = new UserInfoModel();
    userInfo.setUsername("kwusera");
    userInfo.setTeamId(101);
    userInfo.setTenantId(101);
    userInfo.setRole("USER");
    userInfo.setMailid("test@test.com");
    userInfo.setFullname("My full name");
    userInfo.setUserPassword("mypwadasdas");

    return userInfo;
  }

  public UserInfoModelResponse getUserInfoMockResponse() {
    UserInfoModelResponse userInfo = new UserInfoModelResponse();
    userInfo.setTeam("Seahorses");
    userInfo.setUsername("kwusera");
    userInfo.setTeamId(101);
    userInfo.setTenantId(101);
    userInfo.setRole("USER");
    userInfo.setMailid("test@test.com");
    userInfo.setFullname("My full name");
    userInfo.setUserPassword("mypwadasdas");

    return userInfo;
  }

  public UserInfo getUserInfoMockDao() {
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(3);
    userInfo.setUsername("kwusera");
    userInfo.setTenantId(101);
    userInfo.setRole("USER");

    return userInfo;
  }

  public List<UserInfo> getUserInfoList(String username, String role) {
    List<UserInfo> userInfoList = new ArrayList<>();
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(3);
    userInfo.setUsername(username);
    userInfo.setRole(role);
    userInfoList.add(userInfo);

    return userInfoList;
  }

  public List<UserInfoModelResponse> getUserInfoListModel(String username, String role) {
    List<UserInfoModelResponse> userInfoList = new ArrayList<>();
    UserInfoModelResponse userInfo = new UserInfoModelResponse();
    userInfo.setTeam("Seahorses");
    userInfo.setUsername(username);
    userInfo.setRole(role);
    userInfoList.add(userInfo);

    return userInfoList;
  }

  public List<ActivityLog> getLogs() {
    List<ActivityLog> activityLogs = new ArrayList<>();
    ActivityLog activityLog = new ActivityLog();
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLogs.add(activityLog);
    return activityLogs;
  }

  public Topic getTopic(String topicName) {
    Topic topic = new Topic();
    topic.setTeamId(3);
    topic.setTopicname(topicName);

    return topic;
  }

  public int getSchemaRequestPk() {

    return 1001;
  }

  public List<Topic> getTopics() {
    List<Topic> allTopicReqs = new ArrayList<>();
    Topic topicRequest = new Topic();
    topicRequest.setEnvironment("1");
    topicRequest.setTopicname("testtopic");
    topicRequest.setTeamId(3);
    topicRequest.setNoOfPartitions(1);
    topicRequest.setTenantId(101);
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public List<Topic> getTopics(String topicName) {
    List<Topic> allTopicReqs = new ArrayList<>();
    Topic topicRequest = new Topic();
    topicRequest.setEnvironment("1");
    topicRequest.setTopicname(topicName);

    topicRequest.setTeamId(3);
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public List<Topic> getTopicInMultipleEnvs(String topicName, int teamId, int sizeOfTopics) {
    List<Topic> topicList = new ArrayList<>();
    for (int i = 0; i < sizeOfTopics; i++) {
      Topic topic1 = new Topic();
      topic1.setEnvironment(i + 1 + "");
      topic1.setTopicname(topicName);

      topic1.setTeamId(teamId);
      topicList.add(topic1);
    }

    return topicList;
  }

  public List<Topic> getMultipleTopics(String topicPrefix, int size, String env, int teamId) {
    List<Topic> listTopics = new ArrayList<>();
    Topic t;
    if (env == null) {
      env = "1";
    }

    if (teamId == 0) {
      teamId = 101;
    }

    for (int i = 0; i < size; i++) {
      t = new Topic();

      t.setTopicname(topicPrefix + i);
      t.setTopicid(i);
      t.setEnvironment(env);
      t.setTeamId(teamId);
      t.setEnvironmentsList(new ArrayList<>());

      listTopics.add(t);
    }
    return listTopics;
  }

  public List<Acl> getAcls() {
    List<Acl> allTopicReqs = new ArrayList<>();
    Acl topicRequest = new Acl();
    topicRequest.setTeamId(3);
    topicRequest.setAclType(AclType.PRODUCER.value);
    topicRequest.setTenantId(101);
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public List<AclInfo> getAclInfoList() {
    List<AclInfo> allTopicReqs = new ArrayList<>();
    AclInfo topicRequest = new AclInfo();
    topicRequest.setTeamname("Seahorses");
    topicRequest.setTopictype(AclType.PRODUCER.value);
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public List<AclOverviewInfo> getAclOverviewInfoList() {
    List<AclOverviewInfo> allTopicReqs = new ArrayList<>();
    AclOverviewInfo topicRequest = new AclOverviewInfo();
    topicRequest.setTeamname("Seahorses");
    topicRequest.setTopictype(AclType.PRODUCER.value);
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public List<Map<String, String>> getClusterSyncAcls() {
    Set<Map<String, String>> acls = new HashSet<>();

    HashMap<String, String> aclbindingMap;

    aclbindingMap = new HashMap<>();
    aclbindingMap.put("host", "2.1.2.2");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "TOPIC");
    aclbindingMap.put("resourceName", "testtopic1001");
    acls.add(aclbindingMap);

    aclbindingMap = new HashMap<>();
    aclbindingMap.put("host", "2.1.2.2");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "GROUP");
    aclbindingMap.put("resourceName", "mygrp2");
    acls.add(aclbindingMap);

    return new ArrayList<>(acls);
  }

  public List<Map<String, String>> getClusterAcls() {
    Set<Map<String, String>> acls = new HashSet<>();

    HashMap<String, String> aclbindingMap = new HashMap<>();

    aclbindingMap.put("host", "1.1.1.1");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "GROUP");
    aclbindingMap.put("resourceName", "myconsumergroup1");
    acls.add(aclbindingMap);

    aclbindingMap = new HashMap<>();
    aclbindingMap.put("host", "2.1.2.1");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "TOPIC");
    aclbindingMap.put("resourceName", "testtopic1");
    acls.add(aclbindingMap);

    aclbindingMap = new HashMap<>();
    aclbindingMap.put("host", "2.1.2.1");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "GROUP");
    aclbindingMap.put("resourceName", "mygrp1");
    acls.add(aclbindingMap);

    return new ArrayList<>(acls);
  }

  public List<Map<String, String>> getClusterAclsNonApacheKafka() {
    Set<Map<String, String>> acls = new HashSet<>();

    HashMap<String, String> aclbindingMap;

    aclbindingMap = new HashMap<>();
    aclbindingMap.put("host", "*");
    aclbindingMap.put("principle", "testuser1234");
    aclbindingMap.put("consumerGroup", "-na-");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "TOPIC");
    aclbindingMap.put("resourceName", "testtopic1");
    aclbindingMap.put("aivenaclid", "aclid12345");

    acls.add(aclbindingMap);

    return new ArrayList<>(acls);
  }

  public List<Map<String, String>> getClusterAcls2() {
    Set<Map<String, String>> acls = new HashSet<>();

    HashMap<String, String> aclbindingMap = new HashMap<>();

    aclbindingMap.put("host", "1.1.1.1");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "GROUP");
    aclbindingMap.put("resourceName", "myconsumergroup1");
    acls.add(aclbindingMap);

    aclbindingMap = new HashMap<>();
    aclbindingMap.put("host", "2.1.2.1");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "TOPIC");
    aclbindingMap.put("resourceName", "testtopic");
    acls.add(aclbindingMap);

    aclbindingMap = new HashMap<>();
    aclbindingMap.put("host", "2.1.2.1");
    aclbindingMap.put("principle", "User:*");
    aclbindingMap.put("operation", AclPermissionType.READ.value);
    aclbindingMap.put("permissionType", "ALLOW");
    aclbindingMap.put("resourceType", "GROUP");
    aclbindingMap.put("resourceName", "mygrp1");
    acls.add(aclbindingMap);

    return new ArrayList<>(acls);
  }

  public Set<Map<String, String>> getAclsMock() {
    Set<Map<String, String>> listAcls = new HashSet<>();
    HashMap<String, String> hsMp = new HashMap<>();
    hsMp.put("key", "val");
    listAcls.add(hsMp);

    hsMp = new HashMap<>();
    hsMp.put("key", "val");
    listAcls.add(hsMp);
    return listAcls;
  }

  public List<Acl> getAclsForDelete() {
    List<Acl> allTopicReqs = new ArrayList<>();
    Acl topicRequest = new Acl();
    topicRequest.setTopicname("testtopic");
    topicRequest.setConsumergroup("congrp");
    topicRequest.setEnvironment("1");
    topicRequest.setAclip("12.22.126.21");
    topicRequest.setAclType(AclType.PRODUCER.value);
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public List<Acl> getAllAcls() {
    List<Acl> allTopicReqs = new ArrayList<>();
    Acl acl = new Acl();
    acl.setTeamId(3);
    acl.setAclType(AclType.PRODUCER.value);
    allTopicReqs.add(acl);

    acl = new Acl();
    acl.setTopicname("testtopic");
    acl.setConsumergroup("congrp");
    acl.setEnvironment("1");
    acl.setAclip("12.22.126.21");
    acl.setAclType(AclType.PRODUCER.value);
    acl.setOtherParams("101");
    acl.setTeamId(101);
    allTopicReqs.add(acl);

    return allTopicReqs;
  }

  public List<Team> getTeams() {
    List<Team> allTopicReqs = new ArrayList<>();
    Team team = new Team();
    team.setTeamname("Seahorses");
    team.setTeamId(101);
    team.setContactperson("Contact Person");
    team.setTenantId(101);
    team.setTeamphone("3142342343242");
    team.setTeammail("test@test.com");

    ServiceAccounts serviceAccounts = new ServiceAccounts();
    Set<String> serviceAccountsList = new HashSet<>();
    serviceAccountsList.add("user1");
    serviceAccountsList.add("user2");
    serviceAccounts.setNumberOfAllowedAccounts(25);
    serviceAccounts.setServiceAccountsList(serviceAccountsList);
    team.setServiceAccounts(serviceAccounts);
    allTopicReqs.add(team);

    return allTopicReqs;
  }

  public List<TeamModelResponse> getTeamsModel() {
    List<TeamModelResponse> allTopicReqs = new ArrayList<>();
    TeamModelResponse team = new TeamModelResponse();
    team.setTeamname("Seahorses");
    allTopicReqs.add(team);
    return allTopicReqs;
  }

  public List<TopicRequest> getTopicRequests() {
    List<TopicRequest> allTopicReqs = new ArrayList<>();
    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setTeamId(3);
    topicRequest.setRequestStatus(RequestStatus.CREATED.value);
    topicRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    topicRequest.setRequestor("Jackie");
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public List<TopicRequestsResponseModel> getTopicRequestsModel() {
    List<TopicRequestsResponseModel> allTopicReqs = new ArrayList<>();
    TopicRequestsResponseModel topicRequest = new TopicRequestsResponseModel();
    topicRequest.setTeamname("Seahorses");
    allTopicReqs.add(topicRequest);
    return allTopicReqs;
  }

  public TopicRequest getTopicRequest(int topicId) {
    TopicRequest topicRequest = new TopicRequest();
    topicRequest.setTopicid(topicId);
    topicRequest.setTeamId(1001); // INFRATEAM
    topicRequest.setRequestor("kwusera");
    topicRequest.setTopicname("testtopic" + topicId);
    topicRequest.setTopicpartitions(2);
    topicRequest.setReplicationfactor("1");
    topicRequest.setEnvironment("1");
    topicRequest.setRequestOperationType(RequestOperationType.CREATE.value);
    topicRequest.setDescription("Test desc");
    topicRequest.setTenantId(101);
    return topicRequest;
  }

  public TopicCreateRequestModel getTopicCreateRequestModel(int topicId) {
    TopicCreateRequestModel topicRequest = new TopicCreateRequestModel();
    topicRequest.setRequestor("kwusera");
    topicRequest.setTopicname("testtopic" + topicId);
    topicRequest.setTopicpartitions(2);
    topicRequest.setReplicationfactor("1");
    topicRequest.setEnvironment("1");
    topicRequest.setRequestOperationType(RequestOperationType.CREATE);
    topicRequest.setDescription("Test desc");
    topicRequest.setRequestor("kwusera");
    return topicRequest;
  }

  public ConsumerOffsetResetRequestModel getConsumerOffsetResetRequest() {
    ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel =
        new ConsumerOffsetResetRequestModel();
    consumerOffsetResetRequestModel.setRequestor("kwusera");
    consumerOffsetResetRequestModel.setTopicname("testtopic");
    consumerOffsetResetRequestModel.setEnvironment("1");
    consumerOffsetResetRequestModel.setRequestor("kwusera");
    consumerOffsetResetRequestModel.setOffsetResetType(OffsetResetType.LATEST);
    consumerOffsetResetRequestModel.setConsumerGroup("testconsumergroup");
    consumerOffsetResetRequestModel.setOperationalRequestType(
        OperationalRequestType.RESET_CONSUMER_OFFSETS);
    return consumerOffsetResetRequestModel;
  }

  public TopicUpdateRequestModel getTopicUpdateRequestModel(int topicId) {
    TopicUpdateRequestModel topicRequest = new TopicUpdateRequestModel();
    topicRequest.setRequestor("kwusera");
    topicRequest.setTopicname("testtopic" + topicId);
    topicRequest.setTopicpartitions(2);
    topicRequest.setReplicationfactor("1");
    topicRequest.setEnvironment("1");
    topicRequest.setRequestOperationType(RequestOperationType.UPDATE);
    topicRequest.setDescription("Test desc");
    return topicRequest;
  }

  public AclRequests getAclRequest(String topicName) {
    AclRequests aclRequest = new AclRequests();
    aclRequest.setTeamId(3);
    aclRequest.setEnvironment("1");
    aclRequest.setTopicname(topicName);
    aclRequest.setRequestor("kwusera");
    aclRequest.setAclType(AclType.CONSUMER.value);
    aclRequest.setRequestOperationType(RequestOperationType.DELETE.value);
    aclRequest.setConsumergroup("congroup1");
    aclRequest.setAcl_ip("10.11.112.113");
    aclRequest.setAclPatternType(AclPatternType.LITERAL.value);
    aclRequest.setOtherParams("101");
    aclRequest.setTenantId(101);
    return aclRequest;
  }

  public AclRequests getAclRequestCreate(String topicName) {
    AclRequests aclRequest = new AclRequests();
    aclRequest.setTeamId(3);
    aclRequest.setEnvironment("1");
    aclRequest.setTopicname(topicName);
    aclRequest.setRequestor("kwusera");
    aclRequest.setAclType(AclType.CONSUMER.value);
    aclRequest.setRequestOperationType(RequestOperationType.CREATE.value);
    aclRequest.setConsumergroup("congroup1");
    aclRequest.setAcl_ip("10.11.112.113");
    aclRequest.setAclPatternType(AclPatternType.LITERAL.value);
    return aclRequest;
  }

  public List<SchemaRequestModel> getSchemaRequests() {
    List<SchemaRequestModel> schemaList = new ArrayList<>();
    SchemaRequestModel schemaRequest = new SchemaRequestModel();
    schemaRequest.setEnvironment("1");
    schemaRequest.setRequestor("kwusera");
    schemaRequest.setSchemafull("schemafdsfsd");
    schemaRequest.setTeamId(1001);
    schemaRequest.setRemarks("pls approve");
    schemaRequest.setTopicname("testtopic");
    schemaRequest.setRequestOperationType(RequestOperationType.CREATE);
    schemaList.add(schemaRequest);

    return schemaList;
  }

  public List<SchemaRequestsResponseModel> getSchemaRequestsResponse() {
    List<SchemaRequestsResponseModel> schemaList = new ArrayList<>();
    SchemaRequestsResponseModel schemaRequest = new SchemaRequestsResponseModel();
    schemaRequest.setEnvironment("1");
    schemaRequest.setRequestor("kwusera");
    schemaRequest.setSchemafull("schemafdsfsd");
    schemaRequest.setTeamId(1001);
    schemaRequest.setRemarks("pls approve");
    schemaRequest.setTopicname("testtopic");
    schemaList.add(schemaRequest);
    return schemaList;
  }

  public List<SchemaPromotion> getSchemaPromotion() {
    SchemaPromotion promo = new SchemaPromotion();
    promo.setRemarks("Promotion Schema Request");
    promo.setAppName("App");
    promo.setSchemaVersion("1");
    promo.setTopicName("topic-1");
    promo.setForceRegister(false);
    return List.of(promo);
  }

  public List<SchemaRequest> getSchemaRequestsDao() {
    List<SchemaRequest> schemaList = new ArrayList<>();
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setEnvironment("1");
    schemaRequest.setTeamId(3);
    schemaRequest.setRequestor("kwusera");
    schemaRequest.setSchemafull("schema");
    schemaRequest.setTenantId(101);
    schemaList.add(schemaRequest);
    return schemaList;
  }

  public KwKafkaConnector getKwKafkaConnector() {
    KwKafkaConnector kwKafkaConnector = new KwKafkaConnector();
    kwKafkaConnector.setConnectorConfig("config");
    kwKafkaConnector.setConnectorId(101);
    kwKafkaConnector.setConnectorName("testconn");
    kwKafkaConnector.setTenantId(101);
    kwKafkaConnector.setTeamId(1003);
    return kwKafkaConnector;
  }

  public List<AclRequestsResponseModel> getAclRequestsModel() {
    List<AclRequestsResponseModel> aclRequests = new ArrayList<>();
    AclRequestsResponseModel aclRequests1 = new AclRequestsResponseModel();
    aclRequests1.setTeamname("Seahorses");
    aclRequests1.setRequestingteam(2);
    aclRequests1.setRequestOperationType(RequestOperationType.CREATE);
    aclRequests1.setAclPatternType(AclPatternType.LITERAL.value);
    aclRequests.add(aclRequests1);
    return aclRequests;
  }

  public List<AclRequests> getAclRequests() {
    List<AclRequests> aclRequests = new ArrayList<>();
    AclRequests aclRequests1 = new AclRequests();
    aclRequests1.setTeamId(3);
    aclRequests1.setRequestingteam(3);
    aclRequests1.setRequestOperationType(RequestOperationType.CREATE.value);
    aclRequests1.setAclPatternType(AclPatternType.LITERAL.value);
    aclRequests.add(aclRequests1);
    return aclRequests;
  }

  public List<AclRequestsResponseModel> getAclRequestsList() {
    List<AclRequestsResponseModel> aclRequests = new ArrayList<>();
    AclRequestsResponseModel aclRequests1 = new AclRequestsResponseModel();
    aclRequests1.setTeamname("Seahorses");
    aclRequests1.setRequestingteam(3);
    aclRequests.add(aclRequests1);
    return aclRequests;
  }

  public List<TopicRequestsResponseModel> getTopicRequestsList() {
    List<TopicRequestsResponseModel> allTopicReqs = new ArrayList<>();
    TopicRequestsResponseModel topicRequest = new TopicRequestsResponseModel();
    topicRequest.setTeamname("Seahorses");
    allTopicReqs.add(topicRequest);

    return allTopicReqs;
  }

  public List<List<TopicInfo>> getTopicInfoList() {
    List<List<TopicInfo>> topicReqs = new ArrayList<>();

    List<TopicInfo> allTopicReqs = new ArrayList<>();
    TopicInfo topicRequest = new TopicInfo();
    topicRequest.setTeamname("Seahorses");
    allTopicReqs.add(topicRequest);
    topicReqs.add(allTopicReqs);

    return topicReqs;
  }

  public List<Env> getEnvLists() {
    List<Env> envList = new ArrayList<>();
    Env env = new Env();
    env.setId("1");
    env.setName("DEV");
    envList.add(env);
    env.setClusterId(1);
    env.setTenantId(101);
    return envList;
  }

  public List<Env> getEnvListsIncorrect1() {
    List<Env> envList = new ArrayList<>();
    Env env = new Env();
    env.setId("1");
    env.setName("DEV");
    env.setOtherParams("");
    envList.add(env);
    return envList;
  }

  public List<EnvModelResponse> getEnvList() {
    List<EnvModelResponse> envList = new ArrayList<>();
    EnvModelResponse env = new EnvModelResponse();
    env.setId("1");
    env.setName("DEV");
    env.setClusterType(KafkaClustersType.KAFKA);
    env.setClusterId(101);
    envList.add(env);
    return envList;
  }

  public List<EnvModel> getEnvListToAdd() {
    List<EnvModel> envList = new ArrayList<>();
    EnvModel env = new EnvModel();
    env.setId("1");
    env.setName("DEV");
    env.setType(KafkaClustersType.KAFKA.value);
    env.setClusterId(101);
    envList.add(env);
    return envList;
  }

  public List<EnvIdInfo> getSyncEnv() {
    List<EnvIdInfo> envList = new ArrayList<>();

    EnvIdInfo envIdInfo = new EnvIdInfo();
    envIdInfo.setId("1");
    envIdInfo.setName("DEV");
    envList.add(envIdInfo);

    EnvIdInfo envIdInfo1 = new EnvIdInfo();
    envIdInfo1.setId("2");
    envIdInfo1.setName("TST");
    envList.add(envIdInfo1);

    return envList;
  }

  public AclRequestsModel getAclRequestModel(String topic) {
    AclRequestsModel aclRequest = new AclRequestsModel();
    aclRequest.setTeamId(1001);
    aclRequest.setEnvironment("1");
    aclRequest.setTopicname(topic);
    aclRequest.setRequestor("kwusera");
    aclRequest.setAclType(AclType.CONSUMER);
    aclRequest.setConsumergroup("mygrp1");
    ArrayList<String> ipList = new ArrayList<>();
    ipList.add("2.1.2.1");
    aclRequest.setAcl_ip(ipList);
    aclRequest.setAcl_ssl(null);
    aclRequest.setAclPatternType(AclPatternType.LITERAL.value);
    aclRequest.setRequestingteam(1);
    aclRequest.setAclIpPrincipleType(AclIPPrincipleType.IP_ADDRESS);
    aclRequest.setRequestOperationType(RequestOperationType.CREATE);

    return aclRequest;
  }

  public AclRequestsModel getAivenAclRequestModel(String topic) {
    AclRequestsModel aclRequest = new AclRequestsModel();
    aclRequest.setTeamId(1001);
    aclRequest.setEnvironment("2");
    aclRequest.setTopicname(topic);
    aclRequest.setRequestor("kwusera");
    aclRequest.setAclType(AclType.CONSUMER);
    ArrayList<String> sslList = new ArrayList<>();
    sslList.add("user1");
    aclRequest.setAcl_ip(null);
    aclRequest.setAcl_ssl(sslList);
    aclRequest.setAclPatternType(AclPatternType.LITERAL.value);
    aclRequest.setRequestingteam(1);
    aclRequest.setAclIpPrincipleType(AclIPPrincipleType.PRINCIPAL);
    aclRequest.setRequestOperationType(RequestOperationType.CREATE);

    return aclRequest;
  }

  public List<TopicConfig> getClusterApiTopics(String topicPrefix, int size) {
    List<TopicConfig> listTopics = new ArrayList<>();
    TopicConfig hashMap;
    for (int i = 0; i < size; i++) {
      hashMap = new TopicConfig();
      hashMap.setTopicName(topicPrefix + i);
      hashMap.setReplicationFactor("1");
      hashMap.setPartitions("2");
      listTopics.add(hashMap);
    }
    return listTopics;
  }

  public List<SyncTopicUpdates> getSyncTopicUpdates() {
    List<SyncTopicUpdates> syncUpdatesList = new ArrayList<>();
    SyncTopicUpdates syncTopicUpdates = new SyncTopicUpdates();
    syncTopicUpdates.setEnvSelected("1");
    syncTopicUpdates.setPartitions(2);
    syncTopicUpdates.setReplicationFactor("1");
    syncTopicUpdates.setTopicName("testtopic");
    syncTopicUpdates.setTeamSelected("Seahorses");
    syncTopicUpdates.setReq_no("fsadFDS");
    syncUpdatesList.add(syncTopicUpdates);

    return syncUpdatesList;
  }

  public List<SyncConnectorUpdates> getSyncConnectorUpdates() {
    List<SyncConnectorUpdates> syncUpdatesList = new ArrayList<>();
    SyncConnectorUpdates syncConnectorUpdates = new SyncConnectorUpdates();
    syncConnectorUpdates.setEnvSelected("1");
    syncConnectorUpdates.setConnectorName("testconnector");
    syncConnectorUpdates.setTeamSelected("Seahorses");
    syncConnectorUpdates.setReq_no("fsadFDS");
    syncUpdatesList.add(syncConnectorUpdates);

    return syncUpdatesList;
  }

  public List<SyncAclUpdates> getSyncAclsUpdates() {
    List<SyncAclUpdates> syncUpdatesList = new ArrayList<>();
    SyncAclUpdates syncAclUpdates = new SyncAclUpdates();
    syncAclUpdates.setTopicName("testtopic");
    syncAclUpdates.setReq_no("101");
    syncAclUpdates.setAclType(AclType.PRODUCER.value);
    syncAclUpdates.setAclIp("12.2.4.55");
    syncAclUpdates.setTeamSelected("Team2");
    syncAclUpdates.setEnvSelected("DEV");
    syncAclUpdates.setAclId("aclid12345");

    SyncAclUpdates syncAclUpdates1 = new SyncAclUpdates();
    syncAclUpdates1.setTopicName("testtopic1");
    syncAclUpdates1.setReq_no("102");
    syncAclUpdates1.setAclType(AclType.CONSUMER.value);
    syncAclUpdates1.setAclIp("12.2.4.55");
    syncAclUpdates1.setTeamSelected("Team2");
    syncAclUpdates1.setEnvSelected("1");
    syncAclUpdates1.setAclId("aclid12346");

    syncUpdatesList.add(syncAclUpdates);
    syncUpdatesList.add(syncAclUpdates1);

    return syncUpdatesList;
  }

  public TopicOverview getTopicOverview() {
    TopicOverview topicOverview = new TopicOverview();

    List<TopicOverviewInfo> allTopicReqs = new ArrayList<>();
    TopicOverviewInfo topicRequest = new TopicOverviewInfo();
    topicRequest.setTeamname("Seahorses");
    allTopicReqs.add(topicRequest);

    topicOverview.setAclInfoList(getAclOverviewInfoList());
    topicOverview.setTopicInfoList(allTopicReqs);
    return topicOverview;
  }

  public List<String> getAllTeamsSUOnly() {
    List<String> teamsList = new ArrayList<>();
    teamsList.add("Seahorses");
    teamsList.add("Team2");

    return teamsList;
  }

  public List<ServerConfigProperties> getServerConfig() {
    List<ServerConfigProperties> serverConfigPropertiesList = new ArrayList<>();
    ServerConfigProperties serverConfigProperties = new ServerConfigProperties();
    serverConfigProperties.setKey("JDK");
    serverConfigProperties.setValue("OpenJDK");

    serverConfigPropertiesList.add(serverConfigProperties);

    return serverConfigPropertiesList;
  }

  public TopicInfo getTopicInfo() {
    TopicInfo topicInfo = new TopicInfo();
    topicInfo.setTeamname("testteam");
    topicInfo.setTopicName("testtopic");
    topicInfo.setTopicid(1);
    topicInfo.setEnvName("DEV");
    topicInfo.setEnvId("1");
    topicInfo.setDocumentation("this is content for documentation");

    return topicInfo;
  }

  public KwClusters getKwClusters() {
    KwClusters kwClusters = new KwClusters();
    kwClusters.setKafkaFlavor(KafkaFlavors.APACHE_KAFKA.value);
    kwClusters.setBootstrapServers("");
    kwClusters.setProtocol(KafkaSupportedProtocol.PLAINTEXT);
    kwClusters.setClusterName("");
    kwClusters.setClusterId(1);

    return kwClusters;
  }

  public Map<String, List<String>> getRolesPermsMap() {
    Map<String, List<String>> rolesPermsMap = new HashMap<>();
    List<String> permsList =
        List.of(
            PermissionType.ADD_EDIT_DELETE_ENVS.name(),
            PermissionType.FULL_ACCESS_USERS_TEAMS_ROLES.name());
    rolesPermsMap.put("USER", permsList);
    return rolesPermsMap;
  }

  public RequestsCountOverview getRequestStatisticsOverview() {
    RequestsCountOverview requestsCountOverview = new RequestsCountOverview();
    Set<RequestEntityStatusCount> requestEntityStatusCountSet = new HashSet<>();

    Map<String, Long> opCounts = new HashMap<>();
    Map<String, Long> stCounts = new HashMap<>();

    opCounts.put("CREATE", 2L);
    opCounts.put("UPDATE", 3L);
    stCounts.put("CREATED", 2L);
    stCounts.put("APPROVED", 4L);

    Set<RequestStatusCount> requestStatusCountSet = new HashSet<>();
    Set<RequestsOperationTypeCount> requestsOperationTypeCountsSet = new HashSet<>();

    for (String key : stCounts.keySet()) {
      RequestStatusCount requestStatusCount =
          RequestStatusCount.builder()
              .requestStatus(RequestStatus.valueOf(key))
              .count(stCounts.get(key))
              .build();
      requestStatusCountSet.add(requestStatusCount);
    }

    for (String key : opCounts.keySet()) {
      RequestsOperationTypeCount requestsOperationTypeCount =
          RequestsOperationTypeCount.builder()
              .requestOperationType(RequestOperationType.valueOf(key))
              .count(opCounts.get(key))
              .build();
      requestsOperationTypeCountsSet.add(requestsOperationTypeCount);
    }

    RequestEntityStatusCount requestEntityTopicStatusCount = new RequestEntityStatusCount();
    requestEntityTopicStatusCount.setRequestEntityType(RequestEntityType.TOPIC);
    requestEntityTopicStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityTopicStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityTopicStatusCount);

    RequestEntityStatusCount requestEntityAclStatusCount = new RequestEntityStatusCount();
    requestEntityAclStatusCount.setRequestEntityType(RequestEntityType.ACL);
    requestEntityAclStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityAclStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityAclStatusCount);

    RequestEntityStatusCount requestEntitySchemaStatusCount = new RequestEntityStatusCount();
    requestEntitySchemaStatusCount.setRequestEntityType(RequestEntityType.SCHEMA);
    requestEntitySchemaStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntitySchemaStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntitySchemaStatusCount);

    RequestEntityStatusCount requestEntityConnectStatusCount = new RequestEntityStatusCount();
    requestEntityConnectStatusCount.setRequestEntityType(RequestEntityType.CONNECTOR);
    requestEntityConnectStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityConnectStatusCount.setRequestsOperationTypeCountSet(
        requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityConnectStatusCount);

    RequestEntityStatusCount requestEntityUsersStatusCount = new RequestEntityStatusCount();
    requestEntityUsersStatusCount.setRequestEntityType(RequestEntityType.USER);
    requestEntityUsersStatusCount.setRequestStatusCountSet(requestStatusCountSet);
    requestEntityUsersStatusCount.setRequestsOperationTypeCountSet(requestsOperationTypeCountsSet);
    requestEntityStatusCountSet.add(requestEntityUsersStatusCount);

    requestsCountOverview.setRequestEntityStatistics(requestEntityStatusCountSet);

    return requestsCountOverview;
  }

  public Map<String, Map<String, Long>> getRequestCounts() {
    Map<String, Map<String, Long>> allCountsMap = new HashMap<>();

    Map<String, Long> operationTypeCountsMap = new HashMap<>();
    Map<String, Long> statusCountsMap = new HashMap<>();

    operationTypeCountsMap.put(RequestOperationType.CREATE.value, 4L);
    operationTypeCountsMap.put(RequestOperationType.DELETE.value, 2L);
    statusCountsMap.put(RequestStatus.CREATED.value, 5L);
    statusCountsMap.put(RequestStatus.APPROVED.value, 3L);

    allCountsMap.put("OPERATION_TYPE_COUNTS", operationTypeCountsMap);
    allCountsMap.put("STATUS_COUNTS", statusCountsMap);

    return allCountsMap;
  }

  public SchemasInfoOfClusterResponse getSchemasInfoOfEnv() {
    SchemasInfoOfClusterResponse schemasInfoOfClusterResponse = new SchemasInfoOfClusterResponse();
    SchemaInfoOfTopic schemaInfoOfTopic1 = new SchemaInfoOfTopic();
    schemaInfoOfTopic1.setTopic("Topic0");
    schemaInfoOfTopic1.setSchemaVersions(Set.of(1, 2));

    SchemaInfoOfTopic schemaInfoOfTopic2 = new SchemaInfoOfTopic();
    schemaInfoOfTopic2.setTopic("Topic1");
    schemaInfoOfTopic2.setSchemaVersions(Set.of(1, 2, 3));

    List<SchemaInfoOfTopic> schemaInfoOfTopicList = new ArrayList<>();
    schemaInfoOfTopicList.add(schemaInfoOfTopic1);
    schemaInfoOfTopicList.add(schemaInfoOfTopic2);
    schemasInfoOfClusterResponse.setSchemaInfoOfTopicList(schemaInfoOfTopicList);

    return schemasInfoOfClusterResponse;
  }

  public Map<String, Set<String>> getTopicSchemaVersionsInDb() {
    Map<String, Set<String>> topicSchemaVersions = new HashMap<>();
    topicSchemaVersions.put("Topic0", Set.of("1", "2"));
    topicSchemaVersions.put("Topic1", Set.of("1", "2", "3"));
    return topicSchemaVersions;
  }

  public TreeMap<Integer, Map<String, Object>> createSchemaList() throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();

    String schemav2 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"2\", \"id\":3, \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvro\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";
    String schemav1 =
        "{\"subject\":\"2ndTopic-value\", \"version\":\"1\", \"id\":2, \"schema\":\"{\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"klawTestAvro\\\",\\\"namespace\\\": \\\"klaw.avro\\\",\\\"fields\\\": [{\\\"name\\\": \\\"producer\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"Name of the producer\\\"},{\\\"name\\\": \\\"body\\\",\\\"type\\\": \\\"string\\\",\\\"doc\\\": \\\"The body of the message being sent.\\\"},{\\\"name\\\": \\\"timestamp\\\",\\\"type\\\": \\\"long\\\",\\\"doc\\\": \\\"time in seconds from epoc when the message was created.\\\"}],\\\"doc:\\\": \\\"A new schema for testing klaw\\\"}\", \"compatibility\": \"NOT SET\"}";

    TreeMap<Integer, Map<String, Object>> allVersionSchemas =
        new TreeMap<>(Collections.reverseOrder());
    allVersionSchemas.put(1, mapper.readValue(schemav1, Map.class));
    allVersionSchemas.put(2, mapper.readValue(schemav2, Map.class));

    return allVersionSchemas;
  }

  public SyncSchemasList getSchemasSyncInfoOfEnv() {
    SyncSchemasList schemasInfoOfClusterResponse = new SyncSchemasList();
    SchemaSubjectInfoResponse schemaInfoOfTopic1 = new SchemaSubjectInfoResponse();
    schemaInfoOfTopic1.setTopic("test1");
    schemaInfoOfTopic1.setSchemaVersions(Set.of(1, 2));

    SchemaSubjectInfoResponse schemaInfoOfTopic2 = new SchemaSubjectInfoResponse();
    schemaInfoOfTopic2.setTopic("test1");
    schemaInfoOfTopic2.setSchemaVersions(Set.of(1, 2, 3));

    List<SchemaSubjectInfoResponse> schemaInfoOfTopicList = new ArrayList<>();
    schemaInfoOfTopicList.add(schemaInfoOfTopic1);
    schemaInfoOfTopicList.add(schemaInfoOfTopic2);
    schemasInfoOfClusterResponse.setSchemaSubjectInfoResponseList(schemaInfoOfTopicList);

    return schemasInfoOfClusterResponse;
  }

  public List<Topic> generateTopics(int numberOfTopics) {
    String[] topicNames = new String[numberOfTopics];
    for (int i = 0; i < numberOfTopics; i++) {
      topicNames[i] = "Topic" + i;
    }
    return generateTopics(topicNames);
  }

  public List<Topic> generateTopics(String... topicNames) {
    List<Topic> topics = new ArrayList<>();

    for (int i = 0; i < topicNames.length; i++) {
      Topic topic = new Topic();
      topic.setTopicname(topicNames[i]);
      topic.setTenantId(101);
      topic.setTopicid(i);
      topic.setTeamId(10);
      topic.setNoOfReplicas("3");
      topic.setNoOfPartitions(6);
      topic.setEnvironment("1");
      topics.add(topic);
    }
    return topics;
  }

  public ResetEntityCache getResetEntityCache() {
    return ResetEntityCache.builder()
        .tenantId(101)
        .entityType(EntityType.USERS.name())
        .entityValue("testuser")
        .operationType(MetadataOperationType.CREATE.name())
        .build();
  }

  public List<KwTenants> getTenants() {
    List<KwTenants> kwTenantsList = new ArrayList<>();
    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantId(101);
    kwTenants.setTenantName("default");
    kwTenantsList.add(kwTenants);

    return kwTenantsList;
  }

  public static TeamOverview getDummyTeamOverview() {
    TeamOverview teamOverview = new TeamOverview();
    teamOverview.setProducerAclsPerTeamsOverview(getDummyChartJsOverview("Producer Acls"));
    teamOverview.setConsumerAclsPerTeamsOverview(getDummyChartJsOverview("Consumer Acls"));
    teamOverview.setTopicsPerEnvOverview(getDummyChartJsOverview("Topics Per Env"));
    teamOverview.setPartitionsPerEnvOverview(getDummyChartJsOverview("Partitions Per Env"));
    teamOverview.setActivityLogOverview(getDummyChartJsOverview("Activity Log"));
    teamOverview.setAclsPerEnvOverview(getDummyChartJsOverview("Acls Per Env"));
    teamOverview.setTopicsPerTeamsOverview(getDummyChartJsOverview("Topic Per Teams"));

    return teamOverview;
  }

  public static ChartsJsOverview getDummyChartJsOverview(String reportTitle) {
    ChartsJsOverview chartsJsOverview = new ChartsJsOverview();
    chartsJsOverview.setXAxisLabel(TestConstants.X_AXIS_LABEL);
    chartsJsOverview.setYAxisLabel(TestConstants.Y_AXIS_LABEL);
    chartsJsOverview.setData(List.of(1, 2, 3, 4));
    chartsJsOverview.setLabels(List.of("Label 1", "Label 2", "Label 3", "Label 4"));
    chartsJsOverview.setTitleForReport(reportTitle);
    Options options = new Options();
    Title title = new Title();
    options.setTitle(title);
    chartsJsOverview.setOptions(options);

    return chartsJsOverview;
  }

  public static Topic getDummyTopic() {
    Topic topic = new Topic();
    topic.setEnvironment(TestConstants.ENV_ID);
    topic.setTopicname(TestConstants.TOPIC_NAME);
    return topic;
  }

  public static Acl getDummyAcl() {
    Acl acl = new Acl();
    acl.setEnvironment(TestConstants.ENV_ID);
    acl.setTopicname(TestConstants.TOPIC_NAME);
    acl.setConsumergroup(TestConstants.CONSUMER_GROUP);
    return acl;
  }

  public static <K, V> List<Map<K, V>> convertImmutableToMutable(List<Map<K, V>> immutableList) {
    return immutableList.stream()
        .map(HashMap::new)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  public static <T> void assertEqualsList(List<T> actual, List<T> expected) {
    Assertions.assertEquals(actual.size(), expected.size());

    IntStream.range(0, actual.size())
        .forEach(i -> Assertions.assertEquals(actual.get(i), expected.get(i)));
  }
}
