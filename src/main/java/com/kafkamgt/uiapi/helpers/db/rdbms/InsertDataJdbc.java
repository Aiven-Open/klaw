package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InsertDataJdbc {

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

  @Autowired private SelectDataJdbc jdbcSelectHelper;

  public InsertDataJdbc() {}

  public synchronized HashMap<String, String> insertIntoRequestTopic(TopicRequest topicRequest) {
    log.debug("insertIntoRequestTopic {}", topicRequest);

    HashMap<String, String> hashMap = new HashMap<>();
    Integer topicId = getNextTopicRequestId("TOPIC_REQ_ID", topicRequest.getTenantId());
    hashMap.put("topicId", "" + topicId);

    topicRequest.setTopicid(topicId);

    topicRequest.setTopicstatus("created");
    topicRequest.setRequesttime((new Timestamp(System.currentTimeMillis())));
    topicRequestsRepo.save(topicRequest);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(topicRequest.getUsername());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(topicRequest.getTenantId()));
    activityLog.setActivityName("TopicRequest");
    activityLog.setActivityType(topicRequest.getTopictype());
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(topicRequest.getTopicname());
    activityLog.setUser(topicRequest.getUsername());
    activityLog.setEnv(topicRequest.getEnvironment());
    activityLog.setTenantId(topicRequest.getTenantId());

    if (insertIntoActivityLog(activityLog).equals("success")) hashMap.put("result", "success");
    else hashMap.put("result", "failure");

    return hashMap;
  }

  public synchronized HashMap<String, String> insertIntoRequestConnector(
      KafkaConnectorRequest connectorRequest) {
    log.debug("insertIntoRequestConnector {}", connectorRequest);

    HashMap<String, String> hashMap = new HashMap<>();
    Integer topicId = getNextConnectorRequestId("CONNECTOR_REQ_ID", connectorRequest.getTenantId());
    hashMap.put("connectorId", "" + topicId);

    connectorRequest.setConnectorId(topicId);

    connectorRequest.setConnectorStatus("created");
    connectorRequest.setRequesttime((new Timestamp(System.currentTimeMillis())));
    kafkaConnectorRequestsRepo.save(connectorRequest);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(connectorRequest.getUsername());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(connectorRequest.getTenantId()));
    activityLog.setActivityName("ConnectorRequest");
    activityLog.setActivityType(connectorRequest.getConnectortype());
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(
        connectorRequest.getConnectorName() + "," + connectorRequest.getConnectortype());
    activityLog.setUser(connectorRequest.getUsername());
    activityLog.setEnv(connectorRequest.getEnvironment());
    activityLog.setTenantId(connectorRequest.getTenantId());

    if (insertIntoActivityLog(activityLog).equals("success")) hashMap.put("result", "success");
    else hashMap.put("result", "failure");

    return hashMap;
  }

  public synchronized String insertIntoTopicSOT(List<Topic> topics, boolean isSyncTopics) {

    topics.forEach(
        topic -> {
          log.debug("insertIntoTopicSOT {}", topic.getTopicname());
          if (!topic.isExistingTopic()) {
            TopicID topicID = new TopicID();
            topicID.setTenantId(topic.getTenantId());
            topicID.setTopicid(topic.getTopicid());

            if (topicRepo.existsById(topicID)) {
              topic.setTopicid(getNextTopicRequestId("TOPIC_ID", topic.getTenantId()));
            }
          }
          topicRepo.save(topic);
        });

    return "success";
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

    return "success";
  }

  private String insertIntoActivityLog(ActivityLog activityLog) {
    log.debug("insertIntoActivityLog {}", activityLog.getActivityName());
    activityLogRepo.save(activityLog);

    return "success";
  }

  synchronized HashMap<String, String> insertIntoRequestAcl(AclRequests aclReq) {
    log.debug("insertIntoRequestAcl {}", aclReq.getTopicname());
    HashMap<String, String> hashMap = new HashMap<>();
    Integer aclId = getNextAclRequestId(aclReq.getTenantId());
    hashMap.put("aclId", "" + aclId);

    if (aclReq.getAclType() != null) {
      aclReq.setReq_no(aclId);
    }

    aclReq.setAclstatus("created");
    aclReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
    aclReq.setRequestingteam(jdbcSelectHelper.selectUserInfo(aclReq.getUsername()).getTeamId());
    aclRequestsRepo.save(aclReq);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(aclReq.getUsername());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(aclReq.getTenantId()));
    activityLog.setActivityName("AclRequest");
    activityLog.setActivityType(aclReq.getAclType());
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
            + aclReq.getTopictype());
    activityLog.setUser(aclReq.getUsername());
    activityLog.setEnv(aclReq.getEnvironment());
    activityLog.setTenantId(aclReq.getTenantId());

    // Insert into acl activity log
    insertIntoActivityLog(activityLog);
    hashMap.put("result", "success");
    return hashMap;
  }

  public synchronized String insertIntoAclsSOT(List<Acl> acls, boolean isSyncAcls) {

    acls.forEach(
        acl -> {
          log.debug("insertIntoAclsSOT {}", acl.getTopicname());
          if (acl.getReq_no() == null) acl.setReq_no(getNextAclId(acl.getTenantId()));
          aclRepo.save(acl);
        });
    return "success";
  }

  public synchronized String insertIntoRequestSchema(SchemaRequest schemaRequest) {
    log.debug("insertIntoRequestSchema {}", schemaRequest.getTopicname());

    schemaRequest.setReq_no(getNextSchemaRequestId("SCHEMA_REQ_ID", schemaRequest.getTenantId()));
    schemaRequest.setSchemafull(schemaRequest.getSchemafull().trim());
    schemaRequest.setTopicstatus("created");
    schemaRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
    schemaRequest.setTeamId(
        jdbcSelectHelper.selectUserInfo(schemaRequest.getUsername()).getTeamId());

    schemaRequestRepo.save(schemaRequest);

    UserInfo userInfo = jdbcSelectHelper.selectUserInfo(schemaRequest.getUsername());

    ActivityLog activityLog = new ActivityLog();
    activityLog.setReq_no(getNextActivityLogRequestId(schemaRequest.getTenantId()));
    activityLog.setActivityName("SchemaRequest");
    activityLog.setActivityType("new");
    activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
    activityLog.setTeamId(userInfo.getTeamId());
    activityLog.setDetails(schemaRequest.getTopicname() + "-" + schemaRequest.getRemarks());
    activityLog.setUser(schemaRequest.getUsername());
    activityLog.setEnv(schemaRequest.getEnvironment());
    activityLog.setTenantId(schemaRequest.getTenantId());

    // Insert into acl activity log
    insertIntoActivityLog(activityLog);

    return "success";
  }

  public synchronized String insertIntoMessageSchemaSOT(List<MessageSchema> schemas) {

    for (MessageSchema mSchema : schemas) {
      log.debug("insertIntoMessageSchemaSOT {}", mSchema.getTopicname());
      if (mSchema.getReq_no() == null)
        mSchema.setReq_no(getNextSchemaRequestId("SCHEMA_ID", mSchema.getTenantId()));
      messageSchemaRepo.save(mSchema);
    }
    return "success";
  }

  public String insertIntoUsers(UserInfo userInfo) {
    log.debug("insertIntoUsers {}", userInfo.getUsername());
    Optional<UserInfo> userExists = userInfoRepo.findById(userInfo.getUsername());
    if (userExists.isPresent()) return "Failure. User already exists";

    userInfoRepo.save(userInfo);
    return "success";
  }

  public String insertIntoTeams(Team team) {
    log.debug("insertIntoTeams {}", team.getTeamname());
    int teamId = getNextTeamId(team.getTenantId());
    TeamID teamID = new TeamID(teamId, team.getTenantId());
    team.setTeamId(teamId);

    // making unique teamnames per tenant
    if (teamRepo.findAllByTenantId(team.getTenantId()).stream()
        .anyMatch(team1 -> team1.getTeamname().equals(team.getTeamname()))) {
      return "Failure. Team already exists";
    }

    Optional<Team> teamExists = teamRepo.findById(teamID);
    if (teamExists.isPresent()) return "Failure. Team already exists";

    team.setApp("");
    teamRepo.save(team);
    return "success";
  }

  public String insertIntoEnvs(Env env) {
    log.debug("insertIntoEnvs {}", env.getName());

    envRepo.save(env);
    return "success";
  }

  public String insertIntoClusters(KwClusters kwClusters) {
    log.debug("insertIntoClusters {}", kwClusters.getClusterName());
    Integer lastClusterId;
    if (kwClusters.getClusterId() == null) {
      lastClusterId = kwClusterRepo.getNextClusterId(kwClusters.getTenantId());

      if (lastClusterId == null) lastClusterId = 1;
      else lastClusterId = lastClusterId + 1;

      kwClusters.setClusterId(lastClusterId);
    }
    kwClusterRepo.save(kwClusters);
    return "success";
  }

  public String insertIntoRegisterUsers(RegisterUserInfo userInfo) {
    log.debug("insertIntoRegisterUsers {}", userInfo.getUsername());
    Optional<RegisterUserInfo> userExists = registerInfoRepo.findById(userInfo.getUsername());

    Optional<UserInfo> userNameExists = userInfoRepo.findById(userInfo.getUsername());
    if (userNameExists.isPresent()) return "Failure. User already exists";

    // STAGING status comes from AD users
    if (userExists.isPresent()) {
      if (userExists.get().getStatus().equals("APPROVED")) {
        // do nothing -- user is deleted
      } else if (!userExists.get().getStatus().equals("STAGING")
          && !userExists.get().getStatus().equals("PENDING"))
        return "Failure. Registration already exists";
    }

    registerInfoRepo.save(userInfo);
    return "success";
  }

  public Integer getNextTeamId(int tenantId) {
    Integer teamId = teamRepo.getNextTeamId(tenantId);
    if (teamId == null) return 1001;
    else return teamId + 1;
  }

  public Integer getNextAclRequestId(int tenantId) {
    Integer aclReqId = aclRequestsRepo.getNextAclRequestId(tenantId);
    if (aclReqId == null) return 1001;
    else return aclReqId + 1;
  }

  public Integer getNextAclId(int tenantId) {
    Integer aclId = aclRepo.getNextAclId(tenantId);
    if (aclId == null) return 1001;
    else return aclId + 1;
  }

  public synchronized Integer getNextActivityLogRequestId(int tenantId) {
    Integer activityLogId = activityLogRepo.getNextActivityLogRequestId(tenantId);

    if (activityLogId == null) return 1001;
    else return activityLogId + 1;
  }

  public Integer getNextTopicRequestId(String idType, int tenantId) {
    Integer topicId;
    if (idType.equals("TOPIC_REQ_ID")) topicId = topicRequestsRepo.getNextTopicRequestId(tenantId);
    else if (idType.equals("TOPIC_ID")) topicId = topicRepo.getNextTopicRequestId(tenantId);
    else topicId = null;

    if (topicId == null) return 1001;
    else return topicId + 1;
  }

  public Integer getNextConnectorRequestId(String idType, int tenantId) {
    Integer topicId;
    if (idType.equals("CONNECTOR_REQ_ID"))
      topicId = kafkaConnectorRequestsRepo.getNextConnectorRequestId(tenantId);
    else if (idType.equals("CONNECTOR_ID"))
      topicId = kafkaConnectorRepo.getNextConnectorRequestId(tenantId);
    else topicId = null;

    if (topicId == null) return 1001;
    else return topicId + 1;
  }

  public Integer getNextSchemaRequestId(String idType, int tenantId) {
    Integer schemaReqId;
    if (idType.equals("SCHEMA_REQ_ID"))
      schemaReqId = schemaRequestRepo.getNextSchemaRequestId(tenantId);
    else if (idType.equals("SCHEMA_ID")) schemaReqId = messageSchemaRepo.getNextSchemaId(tenantId);
    else schemaReqId = null;

    if (schemaReqId == null) return 1001;
    else return schemaReqId + 1;
  }

  public String addNewTenant(KwTenants kwTenants) {
    Integer maxTenantId = tenantRepo.getMaxTenantId();
    if (maxTenantId == null) maxTenantId = 101;
    else maxTenantId = maxTenantId + 1;
    kwTenants.setTenantId(maxTenantId);
    tenantRepo.save(kwTenants);
    return "success";
  }

  public String registerUserForAD(RegisterUserInfo newUser) {
    registerInfoRepo.save(newUser);
    return "success";
  }

  public String insertMetrics(KwMetrics kwMetrics) {
    Integer metricsId = metricsRepo.getNextId();
    if (metricsId == null) metricsId = 1001;
    else metricsId += 1;

    kwMetrics.setMetricsId(metricsId);

    metricsRepo.save(kwMetrics);
    return "success";
  }

  public String insertDefaultKwProperties(List<KwProperties> kwPropertiesList) {
    kwPropertiesRepo.saveAll(kwPropertiesList);
    return "success";
  }

  public String insertDefaultRolesPermissions(List<KwRolesPermissions> kwRolesPermissionsList) {

    for (KwRolesPermissions kwRolesPermissions : kwRolesPermissionsList) {
      kwRolesPermissions.setId(getNextRolePermissionId(kwRolesPermissions.getTenantId()));
      kwRolesPermsRepo.save(kwRolesPermissions);
    }

    return "success";
  }

  public Integer getNextRolePermissionId(int tenantId) {
    Integer maxId = kwRolesPermsRepo.getMaxRolePermissionId(tenantId);

    if (maxId == null) maxId = 1;

    return maxId + 1;
  }

  public String insertProductDetails(ProductDetails productDetails) {
    productDetailsRepo.save(productDetails);
    return "success";
  }
}
