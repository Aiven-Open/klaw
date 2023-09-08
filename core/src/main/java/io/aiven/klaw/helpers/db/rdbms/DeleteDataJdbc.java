package io.aiven.klaw.helpers.db.rdbms;

import static io.aiven.klaw.error.KlawErrorMessages.DELETE_REQ_ERR;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.*;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DeleteDataJdbc {

  @Autowired(required = false)
  TopicRequestsRepo topicRequestsRepo;

  @Autowired(required = false)
  OperationalRequestsRepo operationalRequestsRepo;

  @Autowired(required = false)
  private TopicRepo topicRepo;

  @Autowired(required = false)
  KwKafkaConnectorRequestsRepo kafkaConnectorRequestsRepo;

  @Autowired(required = false)
  private KwKafkaConnectorRepo kafkaConnectorRepo;

  @Autowired(required = false)
  SchemaRequestRepo schemaRequestRepo;

  @Autowired(required = false)
  MessageSchemaRepo messageSchemaRepo;

  @Autowired(required = false)
  EnvRepo envRepo;

  @Autowired(required = false)
  KwClusterRepo kwClusterRepo;

  @Autowired(required = false)
  TeamRepo teamRepo;

  @Autowired(required = false)
  AclRequestsRepo aclRequestsRepo;

  @Autowired(required = false)
  AclRepo aclRepo;

  @Autowired(required = false)
  UserInfoRepo userInfoRepo;

  @Autowired(required = false)
  private RegisterInfoRepo registerInfoRepo;

  @Autowired(required = false)
  private KwRolesPermsRepo kwRolesPermsRepo;

  @Autowired(required = false)
  private KwPropertiesRepo kwPropertiesRepo;

  @Autowired(required = false)
  private TenantRepo tenantRepo;

  public DeleteDataJdbc() {}

  public DeleteDataJdbc(
      TopicRequestsRepo topicRequestsRepo,
      SchemaRequestRepo schemaRequestRepo,
      EnvRepo envRepo,
      TeamRepo teamRepo,
      AclRequestsRepo aclRequestsRepo,
      AclRepo aclRepo,
      UserInfoRepo userInfoRepo) {
    this.topicRequestsRepo = topicRequestsRepo;
    this.schemaRequestRepo = schemaRequestRepo;
    this.envRepo = envRepo;
    this.teamRepo = teamRepo;
    this.aclRepo = aclRepo;
    this.aclRequestsRepo = aclRequestsRepo;
    this.userInfoRepo = userInfoRepo;
  }

  public String deleteConnectorRequest(int connectorId, int tenantId) {
    log.debug("deleteConnectorRequest {} {}", connectorId, tenantId);
    KafkaConnectorRequestID kafkaConnectorRequestID = new KafkaConnectorRequestID();
    kafkaConnectorRequestID.setConnectorId(connectorId);
    kafkaConnectorRequestID.setTenantId(tenantId);

    Optional<KafkaConnectorRequest> topicReq =
        kafkaConnectorRequestsRepo.findById(kafkaConnectorRequestID);
    if (topicReq.isPresent()) {
      topicReq.get().setRequestStatus(RequestStatus.DELETED.value);
      kafkaConnectorRequestsRepo.save(topicReq.get());
    }
    return ApiResultStatus.SUCCESS.value;
  }

  public String deleteTopicRequest(int topicId, String userName, int tenantId) {
    log.debug("deleteTopicRequest {}", topicId);

    TopicRequestID topicRequestID = new TopicRequestID();
    topicRequestID.setTenantId(tenantId);
    topicRequestID.setTopicid(topicId);

    Optional<TopicRequest> topicReq = topicRequestsRepo.findById(topicRequestID);
    // UserName is transient and is not set in the database but the requestor is. Both are set to
    // the userName when the request is created.
    if (topicReq.isPresent() && topicReq.get().getRequestor().equals(userName)) {
      topicReq.get().setRequestStatus(RequestStatus.DELETED.value);
      topicRequestsRepo.save(topicReq.get());
      return ApiResultStatus.SUCCESS.value;
    }
    return ApiResultStatus.FAILURE.value + DELETE_REQ_ERR;
  }

  public String deleteOperationalRequest(int operationalReqId, String userName, int tenantId) {
    log.debug("deleteOperationalRequest {}", operationalReqId);

    OperationalRequestID operationalRequestID = new OperationalRequestID();
    operationalRequestID.setTenantId(tenantId);
    operationalRequestID.setReqId(operationalReqId);

    Optional<OperationalRequest> operationalRequest =
        operationalRequestsRepo.findById(operationalRequestID);
    // UserName is transient and is not set in the database but the requestor is. Both are set to
    // the userName when the request is created.
    if (operationalRequest.isPresent()
        && operationalRequest.get().getRequestor().equals(userName)) {
      operationalRequest.get().setRequestStatus(RequestStatus.DELETED.value);
      operationalRequestsRepo.save(operationalRequest.get());
      return ApiResultStatus.SUCCESS.value;
    }
    return ApiResultStatus.FAILURE.value + DELETE_REQ_ERR;
  }

  public String deleteTopic(int topicId, int tenantId) {
    log.debug("deleteTopic {}", topicId);
    TopicID topicID = new TopicID();
    topicID.setTenantId(tenantId);
    topicID.setTopicid(topicId);

    Optional<Topic> topicReq = topicRepo.findById(topicID);
    topicReq.ifPresent(topic -> topicRepo.delete(topic));
    return ApiResultStatus.SUCCESS.value;
  }

  public String deleteConnector(int connectorId, int tenantId) {
    log.debug("deleteConnector {}", connectorId);
    KwKafkaConnectorID kwKafkaConnectorID = new KwKafkaConnectorID();
    kwKafkaConnectorID.setConnectorId(connectorId);
    kwKafkaConnectorID.setTenantId(tenantId);

    Optional<KwKafkaConnector> topicReq = kafkaConnectorRepo.findById(kwKafkaConnectorID);
    topicReq.ifPresent(topic -> kafkaConnectorRepo.delete(topic));
    return ApiResultStatus.SUCCESS.value;
  }

  public String deleteSchemaRequest(int avroSchemaId, String userName, int tenantId) {
    log.debug("deleteSchemaRequest {}", avroSchemaId);
    SchemaRequestID schemaRequestID = new SchemaRequestID(avroSchemaId, tenantId);
    Optional<SchemaRequest> schemaReq = schemaRequestRepo.findById(schemaRequestID);
    if (schemaReq.isPresent() && schemaReq.get().getRequestor().equals(userName)) {
      schemaReq.get().setRequestStatus(RequestStatus.DELETED.value);
      schemaRequestRepo.save(schemaReq.get());
      return ApiResultStatus.SUCCESS.value;
    }

    return ApiResultStatus.FAILURE.value + DELETE_REQ_ERR;
  }

  public String deleteAclRequest(int aclId, String userName, int tenantId) {
    log.debug("deleteAclRequest {}", aclId);
    AclRequestID aclRequestID = new AclRequestID();
    aclRequestID.setReq_no(aclId);
    aclRequestID.setTenantId(tenantId);
    Optional<AclRequests> optAclRequests = aclRequestsRepo.findById(aclRequestID);
    if (optAclRequests.isPresent() && optAclRequests.get().getRequestor().equals(userName)) {
      optAclRequests.get().setRequestStatus(RequestStatus.DELETED.value);
      aclRequestsRepo.save(optAclRequests.get());
      return ApiResultStatus.SUCCESS.value;
    }

    return ApiResultStatus.FAILURE.value + DELETE_REQ_ERR;
  }

  public String deleteEnvironment(String envId, int tenantId) {
    log.debug("deleteEnvironment {}", envId);
    EnvID envID = new EnvID();
    envID.setId(envId);
    envID.setTenantId(tenantId);
    Optional<Env> env = envRepo.findById(envID);

    if (env.isPresent()) {
      envRepo.delete(env.get());
      return ApiResultStatus.SUCCESS.value;
    } else {
      return ApiResultStatus.FAILURE.value;
    }
  }

  public String deleteCluster(int clusterId, int tenantId) {
    log.debug("deleteCluster {}", clusterId);
    KwClusters clusters = new KwClusters();
    clusters.setClusterId(clusterId);
    clusters.setTenantId(tenantId);

    List<Env> allAssociatedEnvs = envRepo.findAllByClusterIdAndTenantId(clusterId, tenantId);
    KwClusterID kwClusterID = new KwClusterID();
    kwClusterID.setClusterId(clusterId);
    kwClusterID.setTenantId(tenantId);

    if (kwClusterRepo.findById(kwClusterID).isPresent()) {
      kwClusterRepo.delete(clusters);
      envRepo.deleteAll(allAssociatedEnvs);
      return ApiResultStatus.SUCCESS.value;
    } else {
      return ApiResultStatus.FAILURE.value;
    }
  }

  public String deleteUserRequest(String userId) {
    log.debug("deleteUserRequest {}", userId);
    UserInfo user = new UserInfo();
    user.setUsername(userId);
    userInfoRepo.delete(user);
    return ApiResultStatus.SUCCESS.value;
  }

  public String deleteTeamRequest(Integer teamId, int tenantId) {
    log.debug("deleteTeamRequest {}", teamId);
    Team team = new Team();
    team.setTeamId(teamId);
    team.setTenantId(tenantId);

    teamRepo.delete(team);
    return ApiResultStatus.SUCCESS.value;
  }

  // the actual req of Acl stored in otherParams of AclReq
  public String deletePrevAclRecs(AclRequests aclsToBeDeleted) {

    log.debug("deletePrevAclRecs {}", aclsToBeDeleted.getOtherParams());
    AclID aclID = new AclID();
    aclID.setReq_no(Integer.parseInt(aclsToBeDeleted.getOtherParams()));
    aclID.setTenantId(aclsToBeDeleted.getTenantId());

    Optional<Acl> acl = aclRepo.findById(aclID);
    acl.ifPresent(value -> aclRepo.delete(value));

    return ApiResultStatus.SUCCESS.value;
  }

  public String deleteAclSubscriptionRequest(int req_no, int tenantId) {
    log.debug("deleteAclSubscriptionRequest {}", req_no);
    AclID aclID = new AclID();
    aclID.setReq_no(req_no);
    aclID.setTenantId(tenantId);

    Optional<Acl> aclRec = aclRepo.findById(aclID);
    aclRec.ifPresent(acl -> aclRepo.delete(acl));

    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public CRUDResponse<Topic> deleteTopics(Topic topic) {
    log.debug("deleteTopics {}", topic.getTopicname());
    topicRepo.deleteByTopicnameAndEnvironmentAndTenantId(
        topic.getTopicname(), topic.getEnvironment(), topic.getTenantId());
    return CRUDResponse.ok(List.of(topic));
  }

  @Transactional
  public void deleteConnectors(KwKafkaConnector connector) {
    log.debug("deleteConnectors {}", connector.getConnectorName());
    kafkaConnectorRepo.deleteByConnectorNameAndEnvironmentAndTenantId(
        connector.getConnectorName(), connector.getEnvironment(), connector.getTenantId());
  }

  @Transactional
  public String deleteRole(String roleId, int tenantId) {
    kwRolesPermsRepo.deleteByRoleIdAndTenantId(roleId, tenantId);

    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public String deleteAllUsers(int tenantId) {
    userInfoRepo.deleteByTenantId(tenantId);
    registerInfoRepo.deleteByTenantId(tenantId);
    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public String deleteAllTeams(int tenantId) {
    teamRepo.deleteByTenantId(tenantId);
    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public String deleteAllEnvs(int tenantId) {
    envRepo.deleteByTenantId(tenantId);
    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public String deleteAllClusters(int tenantId) {
    kwClusterRepo.deleteByTenantId(tenantId);
    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public String deleteAllRolesPerms(int tenantId) {
    kwRolesPermsRepo.deleteByTenantId(tenantId);
    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public String deleteAllKwProps(int tenantId) {
    kwPropertiesRepo.deleteByTenantId(tenantId);
    return ApiResultStatus.SUCCESS.value;
  }

  public String deleteTenant(int tenantId) {
    KwTenants kwTenants = new KwTenants();
    kwTenants.setTenantId(tenantId);
    tenantRepo.delete(kwTenants);
    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public String deleteTxnData(int tenantId) {
    topicRequestsRepo.deleteByTenantId(tenantId);
    topicRepo.deleteByTenantId(tenantId);

    aclRequestsRepo.deleteByTenantId(tenantId);
    aclRepo.deleteByTenantId(tenantId);

    schemaRequestRepo.deleteByTenantId(tenantId);
    messageSchemaRepo.deleteByTenantId(tenantId);

    kafkaConnectorRepo.deleteByTenantId(tenantId);
    kafkaConnectorRequestsRepo.deleteByTenantId(tenantId);

    return ApiResultStatus.SUCCESS.value;
  }

  @Transactional
  public void deleteSchemas(Topic topicObj) {
    messageSchemaRepo.deleteByTenantIdAndTopicnameAndEnvironment(
        topicObj.getTenantId(), topicObj.getTopicname(), topicObj.getEnvironment());
  }

  @Transactional
  public void deleteSchemasWithOptions(int tenantId, String topicName, String schemaEnv) {
    messageSchemaRepo.deleteByTenantIdAndTopicnameAndEnvironment(tenantId, topicName, schemaEnv);
  }

  public String deleteAcls(List<Acl> listDeleteAcls, int tenantId) {
    listDeleteAcls.forEach(
        a -> {
          AclID aclID = new AclID(a.getReq_no(), tenantId);
          Optional<Acl> optionalAcl = aclRepo.findById(aclID);
          optionalAcl.ifPresent(acl -> aclRepo.delete(acl));
        });
    return ApiResultStatus.SUCCESS.value;
  }
}
