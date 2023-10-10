package io.aiven.klaw.helpers.db.rdbms;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.EntityType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InsertDataJdbc {

  public static final int DEFAULT_REQ_ID_NUMBER = 1001;

  @Autowired(required = false)
  private UserInfoRepo userInfoRepo;

  @Autowired(required = false)
  private RegisterInfoRepo registerInfoRepo;

  @Autowired(required = false)
  private TeamRepo teamRepo;

  @Autowired(required = false)
  private EnvRepo envRepo;

  @Autowired(required = false)
  private KwClusterRepo kwClusterRepo;

  @Autowired(required = false)
  private ActivityLogRepo activityLogRepo;

  @Autowired(required = false)
  private AclRequestsRepo aclRequestsRepo;

  @Autowired(required = false)
  private TopicRepo topicRepo;

  @Autowired(required = false)
  private OperationalRequestsRepo operationalRequestsRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRepo kafkaConnectorRepo;

  @Autowired(required = false)
  private AclRepo aclRepo;

  @Autowired(required = false)
  private TenantRepo tenantRepo;

  @Autowired(required = false)
  private TopicRequestsRepo topicRequestsRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;

  @Autowired(required = false)
  private SchemaRequestRepo schemaRequestRepo;

  @Autowired(required = false)
  private MessageSchemaRepo messageSchemaRepo;

  @Autowired(required = false)
  private ProductDetailsRepo productDetailsRepo;

  @Autowired(required = false)
  private KwPropertiesRepo kwPropertiesRepo;

  @Autowired(required = false)
  private KwRolesPermsRepo kwRolesPermsRepo;

  @Autowired(required = false)
  private KwMetricsRepo metricsRepo;

  @Autowired(required = false)
  private KwEntitySequenceRepo kwEntitySequenceRepo;

  @Autowired private SelectDataJdbc jdbcSelectHelper;

  public InsertDataJdbc() {}

  public synchronized Map<String, String> insertIntoRequestTopic(TopicRequest topicRequest) {
    log.debug("insertIntoRequestTopic {}", topicRequest);

    Map<String, String> hashMap = new HashMap<>();
    Integer topicId;
    if (topicRequest.getTopicid() == null) {
      topicId = getNextTopicRequestId("TOPIC_REQ_ID", topicRequest.getTenantId());
    } else {
      topicId = topicRequest.getTopicid();
    }
    hashMap.put("topicId", "" + topicId);

    topicRequest.setTopicid(topicId);

    topicRequest.setRequestStatus(RequestStatus.CREATED.value);
    topicRequest.setRequesttime((new Timestamp(System.currentTimeMillis())));
    topicRequestsRepo.save(topicRequest);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(topicRequest.getRequestor());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(topicRequest.getTenantId()));
    activityLog.setActivityName("TopicRequest");
    activityLog.setActivityType(topicRequest.getRequestOperationType());
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(topicRequest.getTopicname());
    activityLog.setUser(topicRequest.getRequestor());
    activityLog.setEnv(topicRequest.getEnvironment());
    activityLog.setTenantId(topicRequest.getTenantId());

    if (ApiResultStatus.SUCCESS.value.equals(insertIntoActivityLog(activityLog))) {
      hashMap.put("result", ApiResultStatus.SUCCESS.value);
    } else {
      hashMap.put("result", ApiResultStatus.FAILURE.value);
    }

    return hashMap;
  }

  public synchronized Map<String, String> insertIntoRequestConnector(
      KafkaConnectorRequest connectorRequest) {
    log.debug("insertIntoRequestConnector {}", connectorRequest);

    Map<String, String> hashMap = new HashMap<>();
    Integer topicId = getNextConnectorRequestId("CONNECTOR_REQ_ID", connectorRequest.getTenantId());
    hashMap.put("connectorId", "" + topicId);

    connectorRequest.setConnectorId(topicId);

    connectorRequest.setRequestStatus(RequestStatus.CREATED.value);
    connectorRequest.setRequesttime((new Timestamp(System.currentTimeMillis())));
    kafkaConnectorRequestsRepo.save(connectorRequest);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(connectorRequest.getRequestor());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(connectorRequest.getTenantId()));
    activityLog.setActivityName("ConnectorRequest");
    activityLog.setActivityType(connectorRequest.getRequestOperationType());
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(
        connectorRequest.getConnectorName() + "," + connectorRequest.getRequestOperationType());
    activityLog.setUser(connectorRequest.getRequestor());
    activityLog.setEnv(connectorRequest.getEnvironment());
    activityLog.setTenantId(connectorRequest.getTenantId());

    if (ApiResultStatus.SUCCESS.value.equals(insertIntoActivityLog(activityLog))) {
      hashMap.put("result", ApiResultStatus.SUCCESS.value);
    } else {
      hashMap.put("result", ApiResultStatus.FAILURE.value);
    }

    return hashMap;
  }

  public synchronized CRUDResponse<Topic> insertIntoTopicSOT(List<Topic> topics) {
    Set<Integer> existingTopicIds = new HashSet<>();
    topicRepo
        .findAllById(
            topics.stream()
                .filter(t -> !t.isExistingTopic())
                .map(t -> new TopicID(t.getTopicid(), t.getTenantId()))
                .collect(Collectors.toList()))
        .forEach(entry -> existingTopicIds.add(entry.getTopicid()));

    for (Topic topic : topics) {
      if (existingTopicIds.contains(topic.getTopicid())) {
        log.debug("insertIntoTopicSOT {} update topic Id", topic.getTopicname());
        topic.setTopicid(getNextTopicRequestId("TOPIC_ID", topic.getTenantId()));
      }
      log.debug("insertIntoTopicSOT {}", topic.getTopicname());
      topicRepo.save(topic);
    }

    return CRUDResponse.ok(topics);
  }

  public synchronized String insertIntoConnectorSOT(
      List<KwKafkaConnector> kafkaConnectors, boolean isSyncTopics) {

    kafkaConnectors.forEach(
        connector -> {
          log.debug("insertIntoConnectorSOT {}", connector.getConnectorName());
          if (!connector.isExistingConnector()) {
            KwKafkaConnectorID kwKafkaConnectorID = new KwKafkaConnectorID();
            kwKafkaConnectorID.setConnectorId(connector.getConnectorId());
            kwKafkaConnectorID.setTenantId(connector.getTenantId());

            if (kafkaConnectorRepo.existsById(kwKafkaConnectorID)) {
              connector.setConnectorId(
                  getNextConnectorRequestId("CONNECTOR_ID", connector.getTenantId()));
            }
          }
          kafkaConnectorRepo.save(connector);
        });

    return ApiResultStatus.SUCCESS.value;
  }

  private String insertIntoActivityLog(ActivityLog activityLog) {
    log.debug("insertIntoActivityLog {}", activityLog.getActivityName());
    activityLogRepo.save(activityLog);

    return ApiResultStatus.SUCCESS.value;
  }

  synchronized Map<String, String> insertIntoRequestAcl(AclRequests aclReq) {
    log.debug("insertIntoRequestAcl {}", aclReq.getTopicname());
    Map<String, String> hashMap = new HashMap<>();
    if (aclReq.getReq_no() == null) {
      Integer aclId = getNextAclRequestId(aclReq.getTenantId());
      hashMap.put("aclId", "" + aclId);

      if (aclReq.getRequestOperationType() != null) {
        aclReq.setReq_no(aclId);
      }
    }

    aclReq.setRequestStatus(RequestStatus.CREATED.value);
    aclReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
    final UserInfo userInfo = jdbcSelectHelper.selectUserInfo(aclReq.getRequestor());
    aclReq.setRequestingteam(userInfo.getTeamId());
    aclRequestsRepo.save(aclReq);

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(aclReq.getTenantId()));
    activityLog.setActivityName("AclRequest");
    activityLog.setActivityType(aclReq.getRequestOperationType());
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(
        aclReq.getAcl_ip()
            + "-"
            + aclReq.getTopicname()
            + "-"
            + aclReq.getAcl_ssl()
            + "-"
            + aclReq.getConsumergroup()
            + "-"
            + aclReq.getAclType());
    activityLog.setUser(aclReq.getRequestor());
    activityLog.setEnv(aclReq.getEnvironment());
    activityLog.setTenantId(aclReq.getTenantId());

    // Insert into acl activity log
    insertIntoActivityLog(activityLog);
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    return hashMap;
  }

  synchronized Map<String, String> insertIntoOperationalRequests(
      OperationalRequest operationalRequest) {
    log.debug("insertIntoOperationalRequests {}", operationalRequest.getTopicname());
    Map<String, String> hashMap = new HashMap<>();

    Integer nextOperationalRequestId =
        getNextOperationalRequestId(operationalRequest.getTenantId());
    hashMap.put("reqId", "" + nextOperationalRequestId);
    operationalRequest.setReqId(nextOperationalRequestId);

    operationalRequest.setRequestStatus(RequestStatus.CREATED.value);
    operationalRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    operationalRequestsRepo.save(operationalRequest);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(operationalRequest.getRequestor());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(operationalRequest.getTenantId()));
    activityLog.setActivityName("OperationalRequest");
    activityLog.setActivityType(operationalRequest.getOperationalRequestType().value);
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(
        operationalRequest.getTopicname() + "-" + operationalRequest.getConsumerGroup());
    activityLog.setUser(operationalRequest.getRequestor());
    activityLog.setEnv(operationalRequest.getEnvironment());
    activityLog.setTenantId(operationalRequest.getTenantId());

    // Insert into acl activity log
    insertIntoActivityLog(activityLog);
    hashMap.put("result", ApiResultStatus.SUCCESS.value);
    return hashMap;
  }

  public synchronized String insertIntoAclsSOT(List<Acl> acls, boolean isSyncAcls) {

    acls.forEach(
        acl -> {
          log.debug("insertIntoAclsSOT {}", acl.getTopicname());
          if (acl.getReq_no() == null) {
            acl.setReq_no(getNextAclId(acl.getTenantId()));
          }
          aclRepo.save(acl);
        });
    return ApiResultStatus.SUCCESS.value;
  }

  public synchronized String insertIntoRequestSchema(SchemaRequest schemaRequest) {
    log.debug("insertIntoRequestSchema {}", schemaRequest.getTopicname());

    schemaRequest.setReq_no(getNextSchemaRequestId("SCHEMA_REQ_ID", schemaRequest.getTenantId()));
    schemaRequest.setSchemafull(schemaRequest.getSchemafull().trim());
    schemaRequest.setRequestStatus(RequestStatus.CREATED.value);
    schemaRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));

    schemaRequestRepo.save(schemaRequest);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(schemaRequest.getRequestor());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(schemaRequest.getTenantId()));
    activityLog.setActivityName("SchemaRequest");
    activityLog.setActivityType("new");
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(schemaRequest.getTopicname() + "-" + schemaRequest.getRemarks());
    activityLog.setUser(schemaRequest.getRequestor());
    activityLog.setEnv(schemaRequest.getEnvironment());
    activityLog.setTenantId(schemaRequest.getTenantId());

    // Insert into acl activity log
    insertIntoActivityLog(activityLog);

    return ApiResultStatus.SUCCESS.value;
  }

  public synchronized String insertIntoMessageSchemaSOT(List<MessageSchema> schemas) {

    for (MessageSchema mSchema : schemas) {
      log.debug("insertIntoMessageSchemaSOT {}", mSchema.getTopicname());
      if (mSchema.getReq_no() == null) {
        mSchema.setReq_no(getNextSchemaRequestId("SCHEMA_ID", mSchema.getTenantId()));
      }
      messageSchemaRepo.save(mSchema);
    }
    return ApiResultStatus.SUCCESS.value;
  }

  public String insertIntoUsers(UserInfo userInfo) {
    log.debug("insertIntoUsers {}", userInfo.getUsername());
    Optional<UserInfo> userExists = userInfoRepo.findById(userInfo.getUsername());
    if (userExists.isPresent()) {
      return "Failure. User already exists";
    }

    userInfoRepo.save(userInfo);
    return ApiResultStatus.SUCCESS.value;
  }

  public String insertIntoTeams(Team team) {
    log.debug("insertIntoTeams {}", team.getTeamname());
    Integer teamId = getNextSeqIdAndUpdate(EntityType.TEAM.name(), team.getTenantId());
    if (teamId == null) { // check for INFRATEAM, STAGINTEAM
      teamId = DEFAULT_REQ_ID_NUMBER;
      insertIntoKwEntitySequence(EntityType.TEAM.name(), teamId + 1, team.getTenantId());
    }
    TeamID teamID = new TeamID(teamId, team.getTenantId());
    team.setTeamId(teamId);

    Optional<Team> teamExists = teamRepo.findById(teamID);
    if (teamExists.isPresent()) {
      return "Failure. Team already exists";
    }

    team.setApp("");
    teamRepo.save(team);
    return ApiResultStatus.SUCCESS.value;
  }

  public Integer getNextTeamId(int tenantId) {
    Integer teamId = teamRepo.getNextTeamId(tenantId);
    if (teamId == null) {
      return DEFAULT_REQ_ID_NUMBER;
    } else {
      return teamId + 1;
    }
  }

  public String addNewEnv(Env env) {
    log.debug("Insert or Update Env {}", env.getName());

    Integer lastId;
    if (env.getId() == null) {
      lastId = getNextSeqIdAndUpdate(EntityType.ENVIRONMENT.name(), env.getTenantId());
      env.setId(lastId + "");
    }

    envRepo.save(env);
    return ApiResultStatus.SUCCESS.value;
  }

  public void insertIntoKwEntitySequence(String entityName, int maxId, int tenantId) {
    KwEntitySequence kwEntitySequence = new KwEntitySequence();
    kwEntitySequence.setEntityName(entityName);
    kwEntitySequence.setSeqId(maxId);
    kwEntitySequence.setTenantId(tenantId);
    kwEntitySequenceRepo.save(kwEntitySequence);
  }

  public String insertIntoClusters(KwClusters kwClusters) {
    log.debug("insertIntoClusters {}", kwClusters.getClusterName());

    Integer lastId;
    if (kwClusters.getClusterId() == null) {
      lastId = getNextSeqIdAndUpdate(EntityType.CLUSTER.name(), kwClusters.getTenantId());
      kwClusters.setClusterId(lastId);
    }
    kwClusterRepo.save(kwClusters);

    return ApiResultStatus.SUCCESS.value;
  }

  public Integer getNextSeqIdAndUpdate(String entityName, int tenantId) {
    List<KwEntitySequence> kwEntitySequenceList =
        kwEntitySequenceRepo.findAllByEntityNameAndTenantId(entityName, tenantId);
    if (!kwEntitySequenceList.isEmpty()) {
      Integer lastId = kwEntitySequenceList.get(0).getSeqId();
      insertIntoKwEntitySequence(entityName, lastId + 1, tenantId);
      return lastId;
    } else {
      // only for entityName TEAM as STAGING and INFRATEAM are added during startup
      return null;
    }
  }

  public String insertIntoRegisterUsers(RegisterUserInfo userInfo) {
    log.debug("insertIntoRegisterUsers {}", userInfo.getUsername());
    Optional<RegisterUserInfo> userExists = registerInfoRepo.findById(userInfo.getUsername());

    Optional<UserInfo> userNameExists = userInfoRepo.findById(userInfo.getUsername());
    if (userNameExists.isPresent()) return "Failure. User already exists";

    // STAGING status comes from AD users
    if (userExists.isPresent()) {
      if ("APPROVED".equals(userExists.get().getStatus())) {
        // do nothing -- user is deleted
      } else if (!"STAGING".equals(userExists.get().getStatus())
          && !"PENDING".equals(userExists.get().getStatus()))
        return "Failure. Registration already exists";
    }

    registerInfoRepo.save(userInfo);
    return ApiResultStatus.SUCCESS.value;
  }

  public Integer getNextAclRequestId(int tenantId) {
    Integer aclReqId = aclRequestsRepo.getNextAclRequestId(tenantId);
    if (aclReqId == null) {
      return DEFAULT_REQ_ID_NUMBER;
    } else {
      return aclReqId + 1;
    }
  }

  public Integer getNextOperationalRequestId(int tenantId) {
    Integer operationalRequestId = operationalRequestsRepo.getNextOperationalRequestId(tenantId);
    if (operationalRequestId == null) {
      return DEFAULT_REQ_ID_NUMBER;
    } else {
      return operationalRequestId + 1;
    }
  }

  public Integer getNextAclId(int tenantId) {
    Integer aclId = aclRepo.getNextAclId(tenantId);
    if (aclId == null) {
      return DEFAULT_REQ_ID_NUMBER;
    } else {
      return aclId + 1;
    }
  }

  public synchronized Integer getNextActivityLogRequestId(int tenantId) {
    Integer activityLogId = activityLogRepo.getNextActivityLogRequestId(tenantId);

    if (activityLogId == null) {
      return DEFAULT_REQ_ID_NUMBER;
    } else {
      return activityLogId + 1;
    }
  }

  public Integer getNextTopicRequestId(String idType, int tenantId) {
    Integer topicId = null;
    if ("TOPIC_REQ_ID".equals(idType)) {
      topicId = topicRequestsRepo.getNextTopicRequestId(tenantId);
    } else if ("TOPIC_ID".equals(idType)) {
      topicId = topicRepo.getNextTopicRequestId(tenantId);
    }

    return topicId == null ? DEFAULT_REQ_ID_NUMBER : topicId + 1;
  }

  public Integer getNextConnectorRequestId(String idType, int tenantId) {
    Integer topicId;
    if (idType.equals("CONNECTOR_REQ_ID")) {
      topicId = kafkaConnectorRequestsRepo.getNextConnectorRequestId(tenantId);
    } else if (idType.equals("CONNECTOR_ID")) {
      topicId = kafkaConnectorRepo.getNextConnectorRequestId(tenantId);
    } else {
      topicId = null;
    }

    if (topicId == null) {
      return DEFAULT_REQ_ID_NUMBER;
    } else {
      return topicId + 1;
    }
  }

  public Integer getNextSchemaRequestId(String idType, int tenantId) {
    Integer schemaReqId = null;
    if ("SCHEMA_REQ_ID".equals(idType)) {
      schemaReqId = schemaRequestRepo.getNextSchemaRequestId(tenantId);
    } else if ("SCHEMA_ID".equals(idType)) {
      schemaReqId = messageSchemaRepo.getNextSchemaId(tenantId);
    }

    return schemaReqId == null ? DEFAULT_REQ_ID_NUMBER : schemaReqId + 1;
  }

  public String addNewTenant(KwTenants kwTenants) {
    if (kwTenants.getTenantId() != null) {
      Optional<KwTenants> tenantExists = tenantRepo.findById(kwTenants.getTenantId());
      if (tenantExists.isPresent()) {
        return "Failure. Tenant already exists";
      }
    }

    Integer maxTenantId = tenantRepo.getMaxTenantId();
    if (maxTenantId == null) {
      maxTenantId = 101;
    } else {
      maxTenantId = maxTenantId + 1;
    }
    kwTenants.setTenantId(maxTenantId);

    tenantRepo.save(kwTenants);
    return ApiResultStatus.SUCCESS.value;
  }

  public String registerUserForAD(RegisterUserInfo newUser) {
    registerInfoRepo.save(newUser);
    return ApiResultStatus.SUCCESS.value;
  }

  public String insertMetrics(KwMetrics kwMetrics) {
    Integer metricsId = metricsRepo.getNextId();
    if (metricsId == null) {
      metricsId = DEFAULT_REQ_ID_NUMBER;
    } else {
      metricsId += 1;
    }

    kwMetrics.setMetricsId(metricsId);

    metricsRepo.save(kwMetrics);
    return ApiResultStatus.SUCCESS.value;
  }

  public String insertDefaultKwProperties(List<KwProperties> kwPropertiesList) {
    kwPropertiesRepo.saveAll(kwPropertiesList);
    return ApiResultStatus.SUCCESS.value;
  }

  public String insertDefaultRolesPermissions(List<KwRolesPermissions> kwRolesPermissionsList) {

    for (KwRolesPermissions kwRolesPermissions : kwRolesPermissionsList) {
      kwRolesPermissions.setId(getNextRolePermissionId(kwRolesPermissions.getTenantId()));
      kwRolesPermsRepo.save(kwRolesPermissions);
    }

    return ApiResultStatus.SUCCESS.value;
  }

  public Integer getNextRolePermissionId(int tenantId) {
    Integer maxId = kwRolesPermsRepo.getMaxRolePermissionId(tenantId);
    if (maxId == null) {
      maxId = 1;
    }

    return maxId + 1;
  }

  public String insertProductDetails(ProductDetails productDetails) {
    productDetailsRepo.save(productDetails);
    return ApiResultStatus.SUCCESS.value;
  }
}
