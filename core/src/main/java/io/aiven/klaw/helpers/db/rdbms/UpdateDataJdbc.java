package io.aiven.klaw.helpers.db.rdbms;

import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.NewUserStatus;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.AclRepo;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.KwKafkaConnectorRepo;
import io.aiven.klaw.repository.KwKafkaConnectorRequestsRepo;
import io.aiven.klaw.repository.KwPropertiesRepo;
import io.aiven.klaw.repository.KwRolesPermsRepo;
import io.aiven.klaw.repository.MessageSchemaRepo;
import io.aiven.klaw.repository.OperationalRequestsRepo;
import io.aiven.klaw.repository.RegisterInfoRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.TenantRepo;
import io.aiven.klaw.repository.TopicRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateDataJdbc {

  @Value("${klaw.reset.password.token.ttl:600000}")
  public int tokenTTL;

  @Autowired(required = false)
  private TopicRequestsRepo topicRequestsRepo;

  @Autowired(required = false)
  private OperationalRequestsRepo operationalRequestsRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;

  @Autowired(required = false)
  private TopicRepo topicRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRepo kafkaConnectorRepo;

  @Autowired(required = false)
  private AclRequestsRepo aclRequestsRepo;

  @Autowired(required = false)
  private AclRepo aclRepo;

  @Autowired(required = false)
  private UserInfoRepo userInfoRepo;

  @Autowired(required = false)
  private TeamRepo teamRepo;

  @Autowired(required = false)
  private RegisterInfoRepo registerInfoRepo;

  @Autowired(required = false)
  private SchemaRequestRepo schemaRequestRepo;

  @Autowired(required = false)
  private InsertDataJdbc insertDataJdbcHelper;

  @Autowired(required = false)
  private DeleteDataJdbc deleteDataJdbcHelper;

  @Autowired(required = false)
  private SelectDataJdbc selectDataJdbcHelper;

  @Autowired(required = false)
  private KwPropertiesRepo kwPropertiesRepo;

  @Autowired(required = false)
  private KwRolesPermsRepo kwRolesPermsRepo;

  @Autowired(required = false)
  private TenantRepo tenantRepo;

  @Autowired(required = false)
  private MessageSchemaRepo messageSchemaRepo;

  public UpdateDataJdbc(
      TopicRequestsRepo topicRequestsRepo,
      AclRequestsRepo aclRequestsRepo,
      UserInfoRepo userInfoRepo,
      SchemaRequestRepo schemaRequestRepo,
      InsertDataJdbc insertDataJdbcHelper,
      SelectDataJdbc selectDataJdbcHelper) {
    this.topicRequestsRepo = topicRequestsRepo;
    this.aclRequestsRepo = aclRequestsRepo;
    this.userInfoRepo = userInfoRepo;
    this.schemaRequestRepo = schemaRequestRepo;
    this.insertDataJdbcHelper = insertDataJdbcHelper;
    this.selectDataJdbcHelper = selectDataJdbcHelper;
  }

  public UpdateDataJdbc() {}

  public String declineTopicRequest(TopicRequest topicRequest, String approver) {
    log.debug("declineTopicRequest {} {}", topicRequest.getTopicname(), approver);
    topicRequest.setApprover(approver);
    topicRequest.setRequestStatus(RequestStatus.DECLINED.value);
    topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    topicRequestsRepo.save(topicRequest);

    return ApiResultStatus.SUCCESS.value;
  }

  public String updateOperationalChangeRequest(
      OperationalRequest operationalRequest, String approver, RequestStatus requestStatus) {
    log.debug("updateOperationalChangeRequest {} {}", operationalRequest.getTopicname(), approver);
    operationalRequest.setApprover(approver);
    operationalRequest.setRequestStatus(requestStatus.value);
    operationalRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    operationalRequestsRepo.save(operationalRequest);

    return ApiResultStatus.SUCCESS.value;
  }

  public String declineConnectorRequest(KafkaConnectorRequest connectorRequest, String approver) {
    log.debug("declineConnectorRequest {} {}", connectorRequest.getConnectorName(), approver);
    connectorRequest.setApprover(approver);
    connectorRequest.setRequestStatus(RequestStatus.DECLINED.value);
    connectorRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    kafkaConnectorRequestsRepo.save(connectorRequest);

    return ApiResultStatus.SUCCESS.value;
  }

  public String updateTopicRequestStatus(TopicRequest topicRequest, String approver) {
    log.debug("updateTopicRequestStatus {} {}", topicRequest.getTopicname(), approver);
    topicRequest.setApprover(approver);
    topicRequest.setRequestStatus(RequestStatus.APPROVED.value);
    topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    topicRequestsRepo.save(topicRequest);

    return ApiResultStatus.SUCCESS.value;
  }

  public TopicRequest updateTopicRequest(TopicRequest topicRequest) {
    log.debug("updateTopicRequest {}", topicRequest.getTopicname());
    return topicRequestsRepo.save(topicRequest);
  }

  public KafkaConnectorRequest updateConnectorRequest(KafkaConnectorRequest kcRequest) {
    log.debug("updateKafkaConnectorRequest {}", kcRequest.getConnectorName());
    return kafkaConnectorRequestsRepo.save(kcRequest);
  }

  public String updateConnectorRequestStatus(
      KafkaConnectorRequest connectorRequest, String approver) {
    log.debug("updateConnectorRequestStatus {} {}", connectorRequest.getConnectorName(), approver);
    connectorRequest.setApprover(approver);
    connectorRequest.setRequestStatus(RequestStatus.APPROVED.value);
    connectorRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    kafkaConnectorRequestsRepo.save(connectorRequest);

    return ApiResultStatus.SUCCESS.value;
  }

  public CRUDResponse<Topic> updateTopicRequest(TopicRequest topicRequest, String approver) {
    log.debug("updateTopicRequest {} {}", topicRequest.getTopicname(), approver);
    topicRequest.setApprover(approver);
    topicRequest.setRequestStatus(RequestStatus.APPROVED.value);
    topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    topicRequestsRepo.save(topicRequest);

    // insert into SOT
    List<Topic> topics = new ArrayList<>();

    Topic topicObj = new Topic();
    copyProperties(topicRequest, topicObj);
    topicObj.setTopicid(
        insertDataJdbcHelper.getNextTopicRequestId("TOPIC_ID", topicRequest.getTenantId()));
    topicObj.setNoOfReplicas(topicRequest.getReplicationfactor());
    topicObj.setNoOfPartitions(topicRequest.getTopicpartitions());
    topicObj.setExistingTopic(false);
    topicObj.setHistory(topicRequest.getHistory());
    topics.add(topicObj);

    final RequestOperationType requestOperationType =
        RequestOperationType.of(topicRequest.getRequestOperationType());
    if (requestOperationType == null) {
      return CRUDResponse.ok(Collections.emptyList());
    }
    CRUDResponse<Topic> saveResult = null;
    switch (requestOperationType) {
      case CREATE, PROMOTE -> saveResult = insertDataJdbcHelper.insertIntoTopicSOT(topics);
      case UPDATE -> saveResult = updateTopicSOT(topics, topicRequest.getOtherParams());
      case DELETE -> {
        saveResult = deleteDataJdbcHelper.deleteTopics(topicObj);
        if (topicRequest.getDeleteAssociatedSchema()) {
          Env kafkaEnv =
              selectDataJdbcHelper.selectEnvDetails(
                  topicRequest.getEnvironment(), topicObj.getTenantId());
          if (kafkaEnv.getAssociatedEnv() != null) {
            deleteDataJdbcHelper.deleteSchemasWithOptions(
                topicObj.getTenantId(),
                topicObj.getTopicname(),
                kafkaEnv.getAssociatedEnv().getId());
          }
        }
      }
    }

    return saveResult;
  }

  public String updateConnectorRequest(KafkaConnectorRequest connectorRequest, String approver) {
    log.debug("updateConnectorRequest {} {}", connectorRequest.getConnectorName(), approver);
    connectorRequest.setApprover(approver);
    connectorRequest.setRequestStatus(RequestStatus.APPROVED.value);
    connectorRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    kafkaConnectorRequestsRepo.save(connectorRequest);

    // insert into SOT
    List<KwKafkaConnector> connectors = new ArrayList<>();

    KwKafkaConnector topicObj = new KwKafkaConnector();
    copyProperties(connectorRequest, topicObj);
    topicObj.setConnectorId(
        insertDataJdbcHelper.getNextConnectorRequestId(
            "CONNECTOR_ID", connectorRequest.getTenantId()));
    topicObj.setExistingConnector(false);
    topicObj.setHistory(connectorRequest.getHistory());
    connectors.add(topicObj);

    final RequestOperationType rot =
        RequestOperationType.of(connectorRequest.getRequestOperationType());
    if (rot == null) {
      return ApiResultStatus.SUCCESS.value;
    }
    switch (rot) {
      case CREATE, PROMOTE -> insertDataJdbcHelper.insertIntoConnectorSOT(connectors, false);
      case UPDATE -> updateConnectorSOT(connectors, connectorRequest.getOtherParams());
      case DELETE -> deleteDataJdbcHelper.deleteConnectors(topicObj);
    }

    return ApiResultStatus.SUCCESS.value;
  }

  private CRUDResponse<Topic> updateTopicSOT(List<Topic> topics, String topicId) {
    topics.forEach(
        topic -> {
          log.debug("updateTopicSOT {}", topic.getTopicname());
          topic.setTopicid(Integer.parseInt(topicId));
          topicRepo.save(topic);
        });
    return CRUDResponse.<Topic>ok(topics);
  }

  private void updateConnectorSOT(List<KwKafkaConnector> topics, String topicId) {
    topics.forEach(
        topic -> {
          log.debug("updateConnectorSOT {}", topic.getConnectorName());
          topic.setConnectorId(Integer.parseInt(topicId));
          kafkaConnectorRepo.save(topic);
        });
  }

  public String updateJsonParams(Map<String, String> jsonParams, int reqNo, int tenantId) {
    AclID aclID = new AclID();
    aclID.setReq_no(reqNo);
    aclID.setTenantId(tenantId);

    Optional<Acl> acl = aclRepo.findById(aclID);
    acl.ifPresent(
        value -> {
          value.setJsonParams(jsonParams);
          aclRepo.save(acl.get());
        });

    return ApiResultStatus.SUCCESS.value;
  }

  public String updateAclRequest(
      AclRequests aclReq, String approver, Map<String, String> jsonParams, boolean saveReqOnly) {
    log.debug("updateAclRequest {} {}", aclReq.getTopicname(), approver);
    aclReq.setApprover(approver);
    aclReq.setRequestStatus(RequestStatus.APPROVED.value);
    aclReq.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    aclRequestsRepo.save(aclReq);
    if (saveReqOnly) {
      return ApiResultStatus.SUCCESS.value;
    }

    return processMultipleAcls(aclReq, jsonParams);
  }

  private String processMultipleAcls(AclRequests aclReq, Map<String, String> jsonParams) {
    List<Acl> acls;
    if (aclReq.getAcl_ip() != null) {
      String[] aclListIp = aclReq.getAcl_ip().split("<ACL>");
      for (String aclString : aclListIp) {
        Acl aclObj = new Acl();
        copyProperties(aclReq, aclObj);
        aclObj.setTeamId(aclReq.getRequestingteam());
        aclObj.setJsonParams(jsonParams);

        acls = new ArrayList<>();
        aclObj.setAclip(aclString);
        aclObj.setAclssl(aclReq.getAcl_ssl());
        acls.add(aclObj);

        if (aclReq.getRequestOperationType() != null
            && RequestOperationType.CREATE.value.equals(aclReq.getRequestOperationType())) {
          acls.forEach(acl -> acl.setReq_no(null));
          insertDataJdbcHelper.insertIntoAclsSOT(acls, false);
        } else {
          // Delete SOT //
          deleteDataJdbcHelper.deletePrevAclRecs(aclReq);
        }
      }
    } else if (aclReq.getAcl_ssl() != null) {
      String[] aclListSsl = aclReq.getAcl_ssl().split("<ACL>");
      for (String aclString : aclListSsl) {
        Acl aclObj = new Acl();
        copyProperties(aclReq, aclObj);
        aclObj.setTeamId(aclReq.getRequestingteam());
        aclObj.setJsonParams(jsonParams);

        acls = new ArrayList<>();
        aclObj.setAclip(aclReq.getAcl_ip());
        aclObj.setAclssl(aclString);
        acls.add(aclObj);

        if (aclReq.getRequestOperationType() != null
            && RequestOperationType.CREATE.value.equals(aclReq.getRequestOperationType())) {
          acls.forEach(acl -> acl.setReq_no(null));
          insertDataJdbcHelper.insertIntoAclsSOT(acls, false);
        } else {
          // Delete SOT //
          deleteDataJdbcHelper.deletePrevAclRecs(aclReq);
        }
      }
    }

    return ApiResultStatus.SUCCESS.value;
  }

  public String declineAclRequest(AclRequests aclRequests, String approver) {
    log.debug("declineAclRequest {} {}", aclRequests.getTopicname(), approver);
    aclRequests.setApprover(approver);
    aclRequests.setRequestStatus(RequestStatus.DECLINED.value);
    aclRequests.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    aclRequestsRepo.save(aclRequests);

    return ApiResultStatus.SUCCESS.value;
  }

  public String claimAclRequest(AclRequests aclRequests, RequestStatus requestStatus) {
    log.debug("claimAclRequest {} {}", aclRequests.getTopicname());

    aclRequests.setRequestStatus(requestStatus.value);
    if (requestStatus.equals(RequestStatus.APPROVED)) {
      aclRequests.setApprovingtime(new Timestamp(System.currentTimeMillis()));
    }
    if (aclRequests.getApprovals() != null) {
      AclRequests parent = new AclRequests();
      parent.setReq_no(aclRequests.getReq_no());
      parent.setTenantId(aclRequests.getTenantId());
      for (AclApproval req : aclRequests.getApprovals()) {
        req.setParent(parent);
      }
    }
    aclRequestsRepo.save(aclRequests);

    return ApiResultStatus.SUCCESS.value;
  }

  public String updateAcl(Acl acl) {

    aclRepo.save(acl);
    return ApiResultStatus.SUCCESS.value;
  }

  public String updatePassword(String username, String password) {
    log.debug("updatePassword {}", username);
    Optional<UserInfo> userRec = userInfoRepo.findById(username);
    if (userRec.isPresent()) {
      UserInfo userInfo = userRec.get();
      userInfo.setPwd(password);
      userInfo.setResetToken(null);
      userInfo.setResetTokenGeneratedAt(null);
      userInfoRepo.save(userInfo);
      return ApiResultStatus.SUCCESS.value;
    }
    return ApiResultStatus.FAILURE.value;
  }

  public String resetPassword(String username, String password, String resetToken)
      throws KlawNotAuthorizedException {
    log.debug("resetPassword {}, token {}", username, resetToken);
    Optional<UserInfo> userRec = userInfoRepo.findById(username);
    if (userRec.isPresent()
        && userRec.get().getResetToken().equals(resetToken)
        && isTimeStampStillValid(userRec.get().getResetTokenGeneratedAt(), tokenTTL)) {

      UserInfo userInfo = userRec.get();
      userInfo.setPwd(password);
      userInfo.setResetToken(null);
      userInfo.setResetTokenGeneratedAt(null);
      userInfoRepo.save(userInfo);
      return ApiResultStatus.SUCCESS.value;
    } else {
      String msg =
          String.format(
              "Password for %s not reset as token was no longer valid or supplied token was invalid.",
              username);
      log.warn(msg);
      throw new KlawNotAuthorizedException(msg);
    }
  }

  public String generatePasswordResetToken(String username) {
    log.debug("generatePasswordResetCode for {}", username);
    Optional<UserInfo> userRec = userInfoRepo.findById(username);
    if (userRec.isPresent()) {
      String resetToken = String.valueOf(UUID.randomUUID());
      UserInfo userInfo = userRec.get();
      userInfo.setResetToken(resetToken);
      userInfo.setResetTokenGeneratedAt(Timestamp.from(Instant.now()));
      userInfoRepo.save(userInfo);
      return resetToken;
    }
    return ApiResultStatus.FAILURE.value;
  }

  public String updateSchemaRequest(SchemaRequest schemaRequest, String approver) {
    log.debug("updateSchemaRequest {} {}", schemaRequest.getTopicname(), approver);
    schemaRequest.setApprover(approver);
    schemaRequest.setRequestStatus(RequestStatus.APPROVED.value);
    schemaRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));

    schemaRequestRepo.save(schemaRequest);

    // Insert to SOT

    List<MessageSchema> schemas = new ArrayList<>();
    MessageSchema schemaObj = new MessageSchema();
    copyProperties(schemaRequest, schemaObj);
    schemas.add(schemaObj);

    List<MessageSchema> messageSchemaList =
        messageSchemaRepo.findAllByTenantIdAndTopicnameAndSchemaversionAndEnvironment(
            schemaObj.getTenantId(),
            schemaObj.getTopicname(),
            schemaObj.getSchemaversion(),
            schemaObj.getEnvironment());
    if (messageSchemaList.isEmpty()) {
      insertDataJdbcHelper.insertIntoMessageSchemaSOT(schemas);
    }

    return ApiResultStatus.SUCCESS.value;
  }

  public String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver) {
    log.debug("updateSchemaRequestDecline {} {}", schemaRequest.getTopicname(), approver);
    schemaRequest.setApprover(approver);
    schemaRequest.setRequestStatus(RequestStatus.DECLINED.value);
    schemaRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));

    schemaRequestRepo.save(schemaRequest);

    return ApiResultStatus.SUCCESS.value;
  }

  public void updateNewUserRequest(String username, String approver, boolean isApprove) {
    log.debug("updateNewUserRequest {} {} {}", username, approver, isApprove);
    Optional<RegisterUserInfo> registerUser = registerInfoRepo.findById(username);
    String status;
    if (isApprove) {
      status = NewUserStatus.APPROVED.value;
    } else {
      status = NewUserStatus.DECLINED.value;
    }
    if (registerUser.isPresent()) {
      RegisterUserInfo registerUserInfo = registerUser.get();
      if (NewUserStatus.PENDING.value.equals(registerUserInfo.getStatus())) {
        registerUserInfo.setStatus(status);
        registerUserInfo.setApprover(approver);
        registerUserInfo.setRegisteredTime(new Timestamp(System.currentTimeMillis()));

        registerInfoRepo.save(registerUserInfo);
      }
    }
  }

  public String updateUser(UserInfo userInfo) {
    log.debug("updateUser {}", userInfo.getUsername());
    Optional<UserInfo> userExists = userInfoRepo.findById(userInfo.getUsername());
    if (userExists.isEmpty()) {
      return "Failure. User doesn't exist";
    }

    userInfoRepo.save(userInfo);
    return ApiResultStatus.SUCCESS.value;
  }

  public String updateUserTeam(String userId, int teamId) {
    log.debug("updateUserTeam {}", userId);
    Optional<UserInfo> userExists = userInfoRepo.findById(userId);
    if (userExists.isEmpty()) {
      return "Failure. User doesn't exist";
    }
    UserInfo userInfo = userExists.get();
    userInfo.setTeamId(teamId);
    userInfoRepo.save(userInfo);
    return ApiResultStatus.SUCCESS.value;
  }

  public String updateTeam(Team team) {
    log.debug("updateTeam {}", team);
    TeamID teamID = new TeamID(team.getTeamId(), team.getTenantId());
    Optional<Team> teamExists = teamRepo.findById(teamID);
    if (teamExists.isEmpty()) {
      return "Failure. Team doesn't exist";
    }

    teamRepo.save(team);
    return ApiResultStatus.SUCCESS.value;
  }

  public String updateKwProperty(KwProperties kwProperties, int tenantId) {
    KwProperties kwProp =
        kwPropertiesRepo.findFirstByKwKeyAndTenantIdOrderByTenantId(
            kwProperties.getKwKey(), tenantId);
    if (kwProp != null) {
      kwProp.setKwValue(kwProperties.getKwValue());
      kwPropertiesRepo.save(kwProp);
      return ApiResultStatus.SUCCESS.value;
    }

    return ApiResultStatus.FAILURE.value;
  }

  public Integer getNextRolePermissionId(int tenantId) {
    Integer maxId = kwRolesPermsRepo.getMaxRolePermissionId(tenantId);
    if (maxId == null) maxId = 1;

    return maxId + 1;
  }

  public String updatePermissions(List<KwRolesPermissions> permissions, String addDelete) {

    List<KwRolesPermissions> rolePermFound;

    if ("DELETE".equals(addDelete)) {
      for (KwRolesPermissions permission : permissions) {
        rolePermFound =
            kwRolesPermsRepo.findAllByRoleIdAndPermissionAndTenantId(
                permission.getRoleId(), permission.getPermission(), permission.getTenantId());
        kwRolesPermsRepo.deleteAll(rolePermFound);
      }
    } else {
      for (KwRolesPermissions permission : permissions) {
        permission.setId(getNextRolePermissionId(permission.getTenantId()));
        kwRolesPermsRepo.save(permission);
      }
    }

    return ApiResultStatus.SUCCESS.value;
  }

  public String updateTopicDocumentation(Topic topic) {
    TopicID topicID = new TopicID();
    topicID.setTenantId(topic.getTenantId());
    topicID.setTopicid(topic.getTopicid());

    Optional<Topic> optTopic = topicRepo.findById(topicID);
    if (optTopic.isPresent()) {
      optTopic.get().setDocumentation(topic.getDocumentation());
      topicRepo.save(optTopic.get());
      return ApiResultStatus.SUCCESS.value;
    }

    return ApiResultStatus.FAILURE.value;
  }

  public String updateConnectorDocumentation(KwKafkaConnector kwKafkaConnector) {
    KwKafkaConnectorID kwKafkaConnectorID = new KwKafkaConnectorID();
    kwKafkaConnectorID.setConnectorId(kwKafkaConnector.getConnectorId());
    kwKafkaConnectorID.setTenantId(kwKafkaConnector.getTenantId());

    Optional<KwKafkaConnector> optTopic = kafkaConnectorRepo.findById(kwKafkaConnectorID);
    if (optTopic.isPresent()) {
      optTopic.get().setDocumentation(kwKafkaConnector.getDocumentation());
      kafkaConnectorRepo.save(optTopic.get());
      return ApiResultStatus.SUCCESS.value;
    }

    return ApiResultStatus.FAILURE.value;
  }

  public String setTenantActivestatus(int tenantId, boolean status) {
    Optional<KwTenants> kwTenant = tenantRepo.findById(tenantId);
    if (kwTenant.isPresent()) {
      if (status) {
        kwTenant.get().setIsActive("true");
      } else {
        kwTenant.get().setIsActive("false");
      }

      tenantRepo.save(kwTenant.get());
    }
    return ApiResultStatus.SUCCESS.value;
  }

  public String updateTenant(int tenantId, String organizationName) {
    Optional<KwTenants> kwTenant = tenantRepo.findById(tenantId);
    if (kwTenant.isPresent()) {
      kwTenant.get().setOrgName(organizationName);
      tenantRepo.save(kwTenant.get());
    }
    return ApiResultStatus.SUCCESS.value;
  }

  public String disableTenant(int tenantId) {
    Optional<KwTenants> kwTenant = tenantRepo.findById(tenantId);
    if (kwTenant.isPresent()) {
      kwTenant.get().setIsActive("false");
      tenantRepo.save(kwTenant.get());
    }
    return ApiResultStatus.SUCCESS.value;
  }

  /**
   * @param startTime Time that the token was generated at
   * @param tokenTTL Duration the token is valid for in milliseconds
   * @return Wether the token is still valid.
   */
  private boolean isTimeStampStillValid(Timestamp startTime, long tokenTTL) {
    // Taking the time at this instant if we add the amount of time this token is valid for to the
    // token
    // We expext that the instant.now() will be before the tokens end of life time.
    return Timestamp.from(Instant.now()).before(new Timestamp(startTime.getTime() + tokenTTL));
  }

  public String updateDbWithUpdatedVersions(List<MessageSchema> schemaListUpdated) {
    messageSchemaRepo.saveAll(schemaListUpdated);
    return ApiResultStatus.SUCCESS.value;
  }
}
