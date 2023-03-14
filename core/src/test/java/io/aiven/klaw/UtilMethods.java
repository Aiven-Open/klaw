package io.aiven.klaw;

import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.*;
import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.AclPermissionType;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.SchemaPromotion;
import io.aiven.klaw.model.requests.SchemaRequestModel;
import io.aiven.klaw.model.requests.TopicCreateRequestModel;
import io.aiven.klaw.model.requests.TopicUpdateRequestModel;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.EnvModelResponse;
import io.aiven.klaw.model.response.SchemaRequestsResponseModel;
import io.aiven.klaw.model.response.TopicRequestsResponseModel;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
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
    userInfo.setTeam("Seahorses");
    userInfo.setUsername("kwusera");
    userInfo.setTeamId(101);
    userInfo.setTenantId(101);
    userInfo.setTenantName("default");
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

  public List<UserInfoModel> getUserInfoListModel(String username, String role) {
    List<UserInfoModel> userInfoList = new ArrayList<>();
    UserInfoModel userInfo = new UserInfoModel();
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

    allTopicReqs.add(team);
    return allTopicReqs;
  }

  public List<TeamModel> getTeamsModel() {
    List<TeamModel> allTopicReqs = new ArrayList<>();
    TeamModel team = new TeamModel();
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
    promo.setSchemaFull("Nonsense Schema");
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

  public List<Map<String, String>> getSyncEnv() {
    List<Map<String, String>> envList = new ArrayList<>();

    HashMap<String, String> hMap = new HashMap<>();
    hMap.put("key", "1");
    hMap.put("name", "DEV");
    envList.add(hMap);

    HashMap<String, String> hMap1 = new HashMap<>();
    hMap1.put("key", "2");
    hMap1.put("name", "TST");
    envList.add(hMap1);

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

  public List<Map<String, String>> getClusterApiTopics(String topicPrefix, int size) {
    List<Map<String, String>> listTopics = new ArrayList<>();
    HashMap<String, String> hashMap;
    for (int i = 0; i < size; i++) {
      hashMap = new HashMap<>();
      hashMap.put("topicName", topicPrefix + i);
      hashMap.put("replicationFactor", "1");
      hashMap.put("partitions", "2");
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

    SyncAclUpdates syncAclUpdates1 = new SyncAclUpdates();
    syncAclUpdates1.setTopicName("testtopic1");
    syncAclUpdates1.setReq_no("102");
    syncAclUpdates1.setAclType(AclType.CONSUMER.value);
    syncAclUpdates1.setAclIp("12.2.4.55");
    syncAclUpdates1.setTeamSelected("Team2");
    syncAclUpdates1.setEnvSelected("1");

    syncUpdatesList.add(syncAclUpdates);
    syncUpdatesList.add(syncAclUpdates1);

    return syncUpdatesList;
  }

  public TopicOverview getTopicOverview() {
    TopicOverview topicOverview = new TopicOverview();

    List<TopicInfo> allTopicReqs = new ArrayList<>();
    TopicInfo topicRequest = new TopicInfo();
    topicRequest.setTeamname("Seahorses");
    allTopicReqs.add(topicRequest);

    topicOverview.setAclInfoList(getAclInfoList());
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
    topicInfo.setCluster("DEV");
    topicInfo.setClusterId("1");
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
}
