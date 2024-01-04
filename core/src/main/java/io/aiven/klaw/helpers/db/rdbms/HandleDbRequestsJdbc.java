package io.aiven.klaw.helpers.db.rdbms;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.response.DashboardStats;
import io.aiven.klaw.service.CommonUtilsService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandleDbRequestsJdbc implements HandleDbRequests {

  @Value("${klaw.dbscripts.create.tables:false}")
  String dbCreateTables;

  @Value("${klaw.dbscripts.insert.basicdata:false}")
  String dbInsertData;

  @Autowired SelectDataJdbc jdbcSelectHelper;

  @Autowired InsertDataJdbc jdbcInsertHelper;

  @Autowired UpdateDataJdbc jdbcUpdateHelper;

  @Autowired DeleteDataJdbc jdbcDeleteHelper;

  /*--------------------Insert */

  public Map<String, String> requestForTopic(TopicRequest topicRequest) {
    return jdbcInsertHelper.insertIntoRequestTopic(topicRequest);
  }

  public Map<String, String> requestForConnector(KafkaConnectorRequest connectorRequest) {
    return jdbcInsertHelper.insertIntoRequestConnector(connectorRequest);
  }

  public Map<String, String> requestForAcl(AclRequests aclReq) {
    return jdbcInsertHelper.insertIntoRequestAcl(aclReq);
  }

  @Override
  public Map<String, String> requestForConsumerOffsetsReset(OperationalRequest operationalRequest) {
    return jdbcInsertHelper.insertIntoOperationalRequests(operationalRequest);
  }

  public String addNewUser(UserInfo userInfo) {
    return jdbcInsertHelper.insertIntoUsers(userInfo);
  }

  public String addNewTeam(Team team) {
    return jdbcInsertHelper.insertIntoTeams(team);
  }

  @Override
  public String addNewTenant(KwTenants kwTenants) {
    return jdbcInsertHelper.addNewTenant(kwTenants);
  }

  public String addNewEnv(Env env) {
    return jdbcInsertHelper.addNewEnv(env);
  }

  @Override
  public String addNewCluster(KwClusters kwClusters) {
    return jdbcInsertHelper.insertIntoClusters(kwClusters);
  }

  public String requestForSchema(SchemaRequest schemaRequest) {
    return jdbcInsertHelper.insertIntoRequestSchema(schemaRequest);
  }

  public CRUDResponse<Topic> addToSynctopics(List<Topic> topicRequests) {
    return jdbcInsertHelper.insertIntoTopicSOT(topicRequests);
  }

  public String addToSyncConnectors(List<KwKafkaConnector> topicRequests) {
    return jdbcInsertHelper.insertIntoConnectorSOT(topicRequests, true);
  }

  public String addToSyncacls(List<Acl> acls) {
    return jdbcInsertHelper.insertIntoAclsSOT(acls, true);
  }

  @Override
  public Integer getNextSeqIdAndUpdate(String entityName, int tenantId) {
    return jdbcInsertHelper.getNextSeqIdAndUpdate(entityName, tenantId);
  }

  @Override
  public boolean hasSequence(String entityName, int tenantId) {
    return jdbcInsertHelper.hasSequence(entityName, tenantId);
  }

  @Override
  public String registerUser(RegisterUserInfo newUser) {
    return jdbcInsertHelper.insertIntoRegisterUsers(newUser);
  }

  @Override
  public String registerUserForAD(RegisterUserInfo newUser) {
    return jdbcInsertHelper.registerUserForAD(newUser);
  }

  @Override
  public String updatePermissions(List<KwRolesPermissions> permissions, String addDelete) {
    return jdbcUpdateHelper.updatePermissions(permissions, addDelete);
  }

  @Override
  public String insertMetrics(KwMetrics kwMetrics) {
    return jdbcInsertHelper.insertMetrics(kwMetrics);
  }

  @Override
  public void insertIntoActivityLog(
      String requestType,
      int tenantId,
      String operationType,
      int teamId,
      String details,
      String envId,
      String requestor) {
    jdbcInsertHelper.insertIntoActivityLog(
        requestType, tenantId, operationType, teamId, details, envId, requestor);
  }

  @Override
  public List<Topic> getAllTopicsByTopicNameAndTeamIdAndTenantId(
      String topicName, int teamId, int tenantId) {
    return jdbcSelectHelper.getTopicsByTopicNameAndTeamId(topicName, teamId, tenantId);
  }

  /*--------------------Select */

  public List<TopicRequest> getAllTopicRequests(
      String requestor,
      String status,
      RequestOperationType requestOperationType,
      String env,
      String wildcardSearch,
      boolean isMyRequest,
      int tenantId) {
    return jdbcSelectHelper.selectFilteredTopicRequests(
        false,
        requestor,
        status,
        false,
        tenantId,
        null,
        requestOperationType,
        env,
        wildcardSearch,
        isMyRequest);
  }

  public List<OperationalRequest> getOperationalRequests(
      String requestor,
      OperationalRequestType operationalRequestType,
      String requestStatus,
      String env,
      String topicName,
      String consumerGroup,
      String wildcardSearch,
      boolean isMyRequest,
      int tenantId) {
    return jdbcSelectHelper.selectFilteredOperationalRequests(
        false,
        requestor,
        requestStatus,
        false,
        tenantId,
        null,
        operationalRequestType,
        env,
        topicName,
        consumerGroup,
        wildcardSearch,
        isMyRequest);
  }

  @Override
  public Map<String, Map<String, Long>> getTopicRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    return jdbcSelectHelper.getTopicRequestsCounts(teamId, requestMode, tenantId, requestor);
  }

  @Override
  public Map<String, Map<String, Long>> getAclRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    return jdbcSelectHelper.getAclRequestsCounts(teamId, requestMode, tenantId, requestor);
  }

  @Override
  public Map<String, Map<String, Long>> getSchemaRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    return jdbcSelectHelper.getSchemaRequestsCounts(teamId, requestMode, tenantId, requestor);
  }

  @Override
  public Map<String, Map<String, Long>> getConnectorRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor) {
    return jdbcSelectHelper.getConnectorRequestsCounts(teamId, requestMode, tenantId, requestor);
  }

  public List<TopicRequest> getCreatedTopicRequests(
      String requestor, String status, boolean showRequestsOfAllTeams, int tenantId) {
    return getCreatedTopicRequests(
        requestor, status, showRequestsOfAllTeams, tenantId, null, null, null, null);
  }

  @Override
  public List<OperationalRequest> getCreatedOperationalRequests(
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId,
      Integer teamId,
      String env,
      String topicName,
      String consumerGroup,
      OperationalRequestType operationalRequestType,
      String wildcardSearch) {
    return jdbcSelectHelper.selectFilteredOperationalRequests(
        true,
        requestor,
        status,
        showRequestsOfAllTeams,
        tenantId,
        teamId,
        operationalRequestType,
        env,
        topicName,
        consumerGroup,
        wildcardSearch,
        false);
  }

  public List<TopicRequest> getCreatedTopicRequests(
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId,
      Integer teamId,
      String env,
      RequestOperationType requestOperationType,
      String wildcardSearch) {
    return jdbcSelectHelper.selectFilteredTopicRequests(
        true,
        requestor,
        status,
        showRequestsOfAllTeams,
        tenantId,
        teamId,
        requestOperationType,
        env,
        wildcardSearch,
        false);
  }

  public List<KafkaConnectorRequest> getAllConnectorRequests(
      String requestor,
      RequestOperationType requestOperationType,
      RequestStatus requestStatus,
      String env,
      String wildcardSearch,
      int tenantId,
      boolean isMyRequest) {
    return jdbcSelectHelper.selectFilteredKafkaConnectorRequests(
        false,
        requestor,
        requestStatus.value,
        requestOperationType,
        false,
        tenantId,
        env,
        wildcardSearch,
        isMyRequest);
  }

  @Override
  public List<KafkaConnectorRequest> getCreatedConnectorRequests(
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId,
      String env,
      RequestOperationType requestOperationType,
      String search) {
    return jdbcSelectHelper.selectFilteredKafkaConnectorRequests(
        true,
        requestor,
        status,
        requestOperationType,
        showRequestsOfAllTeams,
        tenantId,
        env,
        search,
        false);
  }

  public TopicRequest getTopicRequestsForTopic(int topicId, int tenantId) {
    return jdbcSelectHelper.selectTopicRequestsForTopic(topicId, tenantId);
  }

  public OperationalRequest getOperationalRequestsForId(int reqId, int tenantId) {
    return jdbcSelectHelper.selectOperationalRequestsForId(reqId, tenantId);
  }

  public KafkaConnectorRequest getConnectorRequestsForConnector(int connectorId, int tenantId) {
    return jdbcSelectHelper.selectConnectorRequestsForConnector(connectorId, tenantId);
  }

  @Override
  public List<TopicRequest> getTopicRequests(
      String topicName, String envId, String status, int tenantId) {
    return jdbcSelectHelper.selectTopicRequests(topicName, envId, status, tenantId);
  }

  @Override
  public boolean existsTopicRequests(String topicName, String envId, String status, int tenantId) {
    return jdbcSelectHelper.existsTopicRequests(topicName, envId, status, tenantId);
  }

  @Override
  public List<KafkaConnectorRequest> getConnectorRequests(
      String connectorName, String envId, String status, int tenantId) {
    return jdbcSelectHelper.selectConnectorRequests(connectorName, envId, status, tenantId);
  }

  @Override
  public List<Topic> getSyncTopics(String env, Integer teamId, int tenantId) {
    return jdbcSelectHelper.selectSyncTopics(env, teamId, tenantId);
  }

  @Override
  public List<KwKafkaConnector> getSyncConnectors(String envId, Integer teamId, int tenantId) {
    return jdbcSelectHelper.selectSyncConnectors(envId, teamId, tenantId);
  }

  @Override
  public List<Topic> getTopics(String topicName, int tenantId) {
    return jdbcSelectHelper.getTopics(topicName, false, tenantId);
  }

  @Override
  public List<KwKafkaConnector> getConnectors(String topicName, int tenantId) {
    return jdbcSelectHelper.getConnectors(topicName, false, tenantId);
  }

  @Override
  public List<Topic> getTopicsFromEnv(String envId, int tenantId) {
    return jdbcSelectHelper.getTopicsFromEnv(envId, tenantId);
  }

  @Override
  public Optional<Topic> getTopicFromId(int topicId, int tenantId) {
    return jdbcSelectHelper.getTopicFromId(topicId, tenantId);
  }

  @Override
  public List<Topic> getAllTopics(int tenantId) {
    return jdbcSelectHelper.getTopics("", true, tenantId);
  }

  @Override
  public List<Acl> getSyncAcls(String env, int tenantId) {
    return jdbcSelectHelper.selectSyncAcls(env, tenantId);
  }

  @Override
  public List<Acl> getSyncAcls(String env, String topic, int tenantId) {
    return jdbcSelectHelper.selectSyncAcls(env, topic, tenantId);
  }

  @Override
  public List<Acl> getSyncAcls(
      String env, String topic, int teamId, String consumerGroup, int tenantId) {
    return jdbcSelectHelper.selectSyncAcls(env, topic, teamId, consumerGroup, tenantId);
  }

  @Override
  public List<Acl> getPrefixedAclsSOT(String env, int tenantId) {
    return jdbcSelectHelper.getPrefixedAclsSOT(env, tenantId);
  }

  @Override
  public List<Acl> getUniqueConsumerGroups(int tenantId) {
    return jdbcSelectHelper.getUniqueConsumerGroups(tenantId);
  }

  @Override
  public boolean validateIfConsumerGroupUsedByAnotherTeam(
      Integer teamId, int tenantId, String consumerGroup) {
    return jdbcSelectHelper.validateIfConsumerGroupUsedByAnotherTeam(
        teamId, tenantId, consumerGroup);
  }

  @Override
  public Acl getSyncAclsFromReqNo(int reqNo, int tenantId) {
    return jdbcSelectHelper.selectSyncAclsFromReqNo(reqNo, tenantId);
  }

  @Override
  public boolean existsAclRequest(
      String topicName, String requestStatus, String env, int tenantId) {
    return jdbcSelectHelper.existsAclRequest(topicName, requestStatus, env, tenantId);
  }

  @Override
  public boolean existsSchemaRequest(
      String topicName, String requestStatus, String env, int tenantId) {
    return jdbcSelectHelper.existsSchemaRequest(topicName, requestStatus, env, tenantId);
  }

  @Override
  public boolean existsSchemaRequest(
      String topicName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId) {
    return jdbcSelectHelper.existsSchemaRequest(
        topicName, requestStatus, requestOperationType, env, tenantId);
  }

  @Override
  public boolean existsTopicRequest(
      String topicName, String requestStatus, String env, int tenantId) {
    return jdbcSelectHelper.existsTopicRequest(topicName, requestStatus, env, tenantId);
  }

  @Override
  public boolean existsTopicRequest(
      String topicName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId) {
    return jdbcSelectHelper.existsTopicRequest(
        topicName, requestStatus, requestOperationType, env, tenantId);
  }

  @Override
  public boolean existsTopicRequest(String topicName, String requestStatus, int tenantId) {
    return jdbcSelectHelper.existsTopicRequestOnAnyEnv(topicName, requestStatus, tenantId);
  }

  @Override
  public boolean existsConnectorRequest(String connectorName, String requestStatus, int tenantId) {
    return jdbcSelectHelper.existsConnectorRequestOnAnyEnv(connectorName, requestStatus, tenantId);
  }

  @Override
  public boolean existsClaimTopicRequest(String topicName, String requestStatus, int tenantId) {
    return jdbcSelectHelper.existsClaimTopicRequest(
        topicName, requestStatus, RequestOperationType.CLAIM.value, tenantId);
  }

  @Override
  public boolean existsConnectorRequest(
      String connectorName, String requestStatus, String env, int tenantId) {
    return jdbcSelectHelper.existsConnectorRequest(connectorName, requestStatus, env, tenantId);
  }

  @Override
  public boolean existsConnectorRequest(
      String connectorName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId) {
    return jdbcSelectHelper.existsConnectorRequest(
        connectorName, requestStatus, requestOperationType, env, tenantId);
  }

  @Override
  public boolean existsClaimConnectorRequest(
      String connectorName, String requestStatus, int tenantId) {
    return jdbcSelectHelper.existsClaimConnectorRequest(connectorName, requestStatus, tenantId);
  }

  @Override
  public boolean existsSchemaForTopic(String topicName, String env, int tenantId) {
    return jdbcSelectHelper.existsSchemaForTopic(topicName, env, tenantId);
  }

  @Override
  public List<AclRequests> getAllAclRequests(
      boolean isApproval,
      String requestor,
      String role,
      String requestStatus,
      boolean showRequestsOfAllTeams,
      RequestOperationType requestOperationType,
      String topic,
      String environment,
      String wildcardSearch,
      AclType aclType,
      boolean isMyRequest,
      int tenantId) {
    return jdbcSelectHelper.selectFilteredAclRequests(
        isApproval,
        requestor,
        role,
        requestStatus,
        requestOperationType,
        showRequestsOfAllTeams,
        topic,
        environment,
        wildcardSearch,
        aclType,
        isMyRequest,
        tenantId);
  }

  @Override
  public List<AclRequests> getCreatedAclRequestsByStatus(
      String requestor,
      String requestStatus,
      boolean showRequestsOfAllTeams,
      RequestOperationType requestOperationType,
      String topic,
      String environment,
      String wildcardSearch,
      AclType aclType,
      int tenantId) {
    return jdbcSelectHelper.selectFilteredAclRequests(
        true,
        requestor,
        "",
        requestStatus,
        requestOperationType,
        showRequestsOfAllTeams,
        topic,
        environment,
        wildcardSearch,
        aclType,
        false,
        tenantId);
  }

  @Override
  public List<SchemaRequest> getAllSchemaRequests(
      boolean isApproval,
      String requestor,
      int tenantId,
      RequestOperationType requestOperationType,
      String topic,
      String env,
      String status,
      String search,
      boolean showRequestsOfAllTeams,
      boolean isMyRequest) {
    return jdbcSelectHelper.selectFilteredSchemaRequests(
        isApproval,
        requestor,
        tenantId,
        requestOperationType,
        topic,
        env,
        status,
        search,
        showRequestsOfAllTeams,
        isMyRequest);
  }

  @Override
  public SchemaRequest getSchemaRequest(int avroSchemaId, int tenantId) {
    return jdbcSelectHelper.selectSchemaRequest(avroSchemaId, tenantId);
  }

  @Override
  public List<Team> getAllTeamsOfUsers(String username, int tenantId) {
    return jdbcSelectHelper.selectTeamsOfUsers(username, tenantId);
  }

  @Override
  public List<Team> getAllTeams(int tenantId) {
    return jdbcSelectHelper.selectAllTeams(tenantId);
  }

  @Override
  public Team getTeamDetails(Integer teamId, int tenantId) {
    return jdbcSelectHelper.selectTeamDetails(teamId, tenantId);
  }

  @Override
  public Team getTeamDetailsFromName(String teamName, int tenantId) {
    return jdbcSelectHelper.selectTeamDetailsFromName(teamName, tenantId);
  }

  @Override
  public Map<String, String> getDashboardInfo(Integer teamId, int tenantId) {
    return jdbcSelectHelper.getDashboardInfo(teamId, tenantId);
  }

  @Override
  public List<UserInfo> getAllUsersInfo(int tenantId) {
    return jdbcSelectHelper.selectAllUsersInfo(tenantId);
  }

  @Override
  public List<UserInfo> getAllUsersAllTenants() {
    return jdbcSelectHelper.selectAllUsersAllTenants();
  }

  @Override
  public List<UserInfo> getAllUsersInfoForTeam(Integer teamId, int tenantId) {
    return jdbcSelectHelper.selectAllUsersInfoForTeam(teamId, tenantId);
  }

  @Override
  public boolean existsUsersInfoForTeam(Integer teamId, int tenantId) {
    return jdbcSelectHelper.existsUsersInfoForTeam(teamId, tenantId);
  }

  @Override
  public List<RegisterUserInfo> getAllRegisterUsersInfoForTenant(int tenantId) {
    return jdbcSelectHelper.selectAllRegisterUsersInfoForTenant(tenantId);
  }

  @Override
  public int getCountRegisterUsersInfoForTenant(int tenantId) {
    return jdbcSelectHelper.countRegisterUsersInfoForTenant(tenantId);
  }

  @Override
  public List<RegisterUserInfo> getAllRegisterUsersInformation() {
    return jdbcSelectHelper.selectAllRegisterUsersInfo();
  }

  @Override
  public RegisterUserInfo getFirstStagingRegisterUsersInfo(String userName) {
    return jdbcSelectHelper.selectFirstStagingRegisterUsersInfo(userName);
  }

  public UserInfo getUsersInfo(String username) {
    return jdbcSelectHelper.selectUserInfo(username);
  }

  @Override
  public RegisterUserInfo getRegisterUsersInfo(String username) {
    return jdbcSelectHelper.selectRegisterUsersInfo(username);
  }

  @Override
  public AclRequests getAclRequest(int req_no, int tenantId) {
    return jdbcSelectHelper.selectAcl(req_no, tenantId);
  }

  @Override
  public Optional<Acl> getAcl(int aclId, int tenantId) {
    return jdbcSelectHelper.getAcl(aclId, tenantId);
  }

  @Override
  public OperationalRequest getOperationalRequest(int reqId, int tenantId) {
    return jdbcSelectHelper.selectOperationalRequest(reqId, tenantId);
  }

  public List<KwKafkaConnector> getConnectorsFromName(String connectorName, int tenantId) {
    return jdbcSelectHelper.selectConnectorDetails(connectorName, tenantId);
  }

  @Override
  public List<Topic> getTopicsforTeam(Integer teamId, int tenantId) {
    return jdbcSelectHelper.getTopicsforTeam(teamId, tenantId);
  }

  @Override
  public List<Acl> getConsumerGroupsforTeam(Integer teamId, int tenantId) {
    return jdbcSelectHelper.getConsumerGroupsforTeam(teamId, tenantId);
  }

  @Override
  public List<Acl> getAllConsumerGroups(int tenantId) {
    return jdbcSelectHelper.getAllConsumerGroups(tenantId);
  }

  @Override
  public List<Env> getAllEnvs(int tenantId) {
    return jdbcSelectHelper.selectAllEnvs(KafkaClustersType.ALL, tenantId);
  }

  @Override
  public List<Env> getAllKafkaEnvs(int tenantId) {
    return jdbcSelectHelper.selectAllEnvs(KafkaClustersType.KAFKA, tenantId);
  }

  @Override
  public List<Env> getAllSchemaRegEnvs(int tenantId) {
    return jdbcSelectHelper.selectAllEnvs(KafkaClustersType.SCHEMA_REGISTRY, tenantId);
  }

  @Override
  public List<Env> getAllKafkaConnectEnvs(int tenantId) {
    return jdbcSelectHelper.selectAllEnvs(KafkaClustersType.KAFKA_CONNECT, tenantId);
  }

  @Override
  public Env getEnvDetails(String env, int tenantId) {
    return jdbcSelectHelper.selectEnvDetails(env, tenantId);
  }

  public List<ActivityLog> getActivityLog(String user, String env, boolean allReqs, int tenantId) {
    return jdbcSelectHelper.selectActivityLog(user, env, allReqs, tenantId);
  }

  @Override
  public Map<Integer, Map<String, Map<String, String>>> getAllKwProperties() {
    return jdbcSelectHelper.selectAllKwProperties();
  }

  @Override
  public List<KwProperties> getAllKwPropertiesPerTenant(int tenantId) {
    return jdbcSelectHelper.selectAllKwPropertiesPerTenant(tenantId);
  }

  @Override
  public String insertDefaultKwProperties(List<KwProperties> kwPropertiesList) {
    return jdbcInsertHelper.insertDefaultKwProperties(kwPropertiesList);
  }

  @Override
  public String insertDefaultRolesPermissions(List<KwRolesPermissions> kwRolesPermissionsList) {
    return jdbcInsertHelper.insertDefaultRolesPermissions(kwRolesPermissionsList);
  }

  @Override
  public String insertProductDetails(ProductDetails productDetails) {
    return jdbcInsertHelper.insertProductDetails(productDetails);
  }

  @Override
  public String insertIntoMessageSchemaSOT(List<MessageSchema> schemaList) {
    return jdbcInsertHelper.insertIntoMessageSchemaSOT(schemaList);
  }

  @Override
  public Integer getNextTopicRequestId(String idType, int tenantId) {
    return jdbcSelectHelper.getNextTopicRequestId(idType, tenantId);
  }

  @Override
  public Integer getNextConnectorRequestId(String idType, int tenantId) {
    return jdbcSelectHelper.getNextConnectorRequestId(idType, tenantId);
  }

  @Override
  public List<KwTenants> getTenants() {
    return jdbcSelectHelper.getTenants();
  }

  @Override
  public Optional<KwTenants> getMyTenants(int tenantId) {
    return jdbcSelectHelper.getMyTenants(tenantId);
  }

  @Override
  public List<KwRolesPermissions> getRolesPermissions() {
    return jdbcSelectHelper.getRolesPermissions();
  }

  @Override
  public List<KwRolesPermissions> getRolesPermissionsPerTenant(int tenantId) {
    return jdbcSelectHelper.getRolesPermissionsPerTenant(tenantId);
  }

  @Override
  public List<KwClusters> getAllClusters(KafkaClustersType typeOfCluster, int tenantId) {
    return jdbcSelectHelper.getAllClusters(typeOfCluster, tenantId);
  }

  @Override
  public boolean existsClusters(KafkaClustersType typeOfCluster, int tenantId) {
    return jdbcSelectHelper.existsClusters(typeOfCluster, tenantId);
  }

  @Override
  public KwClusters getClusterDetails(int id, int tenantId) {
    return jdbcSelectHelper.getClusterDetails(id, tenantId);
  }

  @Override
  public String getRegistrationId(String userName) {
    return jdbcSelectHelper.getRegistrationId(userName);
  }

  @Override
  public RegisterUserInfo getRegistrationDetails(String registrationId, String status) {
    return jdbcSelectHelper.getRegistrationDetails(registrationId, status);
  }

  @Override
  public DashboardStats getDashboardStats(Integer teamId, int tenantId) {
    return jdbcSelectHelper.getDashboardStats(teamId, tenantId);
  }

  @Override
  public List<Topic> getAllTopicsByTopictypeAndTeamname(
      String topicType, Integer teamId, int tenantId) {
    return jdbcSelectHelper.selectAllTopicsByTopictypeAndTeamname(topicType, teamId, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> getActivityLogForLastDays(
      int numberOfDays, String[] envId, int tenantId) {
    return jdbcSelectHelper.selectActivityLogForLastDays(numberOfDays, envId, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> getActivityLogByTeam(
      Integer teamId, int numberOfDays, int tenantId) {
    return jdbcSelectHelper.selectActivityLogByTeam(teamId, numberOfDays, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> getTopicsCountByTeams(
      Integer teamId, int tenantId) {
    return jdbcSelectHelper.selectTopicsCountByTeams(teamId, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> getTopicsCountByEnv(
      Integer tenantId) {
    return jdbcSelectHelper.selectTopicsCountByEnv(tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> getPartitionsCountByEnv(
      Integer teamId, Integer tenantId) {
    return jdbcSelectHelper.selectPartitionsCountByEnv(teamId, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> getAclsCountByEnv(
      Integer teamId, Integer tenantId) {
    return jdbcSelectHelper.selectAclsCountByEnv(teamId, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> getAclsCountByTeams(
      String aclType, Integer teamId, Integer tenantId) {
    return jdbcSelectHelper.selectAclsCountByTeams(aclType, teamId, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> getAllTopicsForTeamGroupByEnv(
      Integer teamId, int tenantId) {
    return jdbcSelectHelper.selectAllTopicsForTeamGroupByEnv(teamId, tenantId);
  }

  @Override
  public List<CommonUtilsService.ChartsOverviewItem<String, Integer>> getAllMetrics(
      String metricsType, String metricsName, String env) {
    return jdbcSelectHelper.selectAllMetrics(metricsType, metricsName, env);
  }

  @Override
  public Optional<MessageSchema> getFirstSchemaForTenantAndEnvAndTopicAndVersion(
      int tenantId, String schemaEnvId, String topicName, String schemaVersion) {
    return jdbcSelectHelper.getFirstSchemaForTenantAndEnvAndTopicAndVersion(
        tenantId, schemaEnvId, topicName, schemaVersion);
  }

  @Override
  public List<MessageSchema> getSchemaForTenantAndEnvAndTopic(
      int tenantId, String schemaEnvId, String topicName) {
    return jdbcSelectHelper.getSchemaForTenantAndEnvAndTopic(tenantId, schemaEnvId, topicName);
  }

  /*--------------------Update */

  @Override
  public String updateTopicDocumentation(Topic topic) {
    return jdbcUpdateHelper.updateTopicDocumentation(topic);
  }

  @Override
  public String updateConnectorDocumentation(KwKafkaConnector topic) {
    return jdbcUpdateHelper.updateConnectorDocumentation(topic);
  }

  @Override
  public CRUDResponse<Topic> updateTopicRequest(TopicRequest topicRequest, String approver) {
    return jdbcUpdateHelper.updateTopicRequest(topicRequest, approver);
  }

  @Override
  public String updateOperationalChangeRequest(
      OperationalRequest operationalRequest, String approver, RequestStatus requestStatus) {
    return jdbcUpdateHelper.updateOperationalChangeRequest(
        operationalRequest, approver, requestStatus);
  }

  @Override
  public String updateConnectorRequest(KafkaConnectorRequest topicRequest, String approver) {
    return jdbcUpdateHelper.updateConnectorRequest(topicRequest, approver);
  }

  @Override
  public String updateTopicRequestStatus(TopicRequest topicRequest, String approver) {
    return jdbcUpdateHelper.updateTopicRequestStatus(topicRequest, approver);
  }

  @Override
  public String updateConnectorRequestStatus(
      KafkaConnectorRequest connectorRequest, String approver) {
    return jdbcUpdateHelper.updateConnectorRequestStatus(connectorRequest, approver);
  }

  public String declineTopicRequest(TopicRequest topicRequest, String approver) {
    return jdbcUpdateHelper.declineTopicRequest(topicRequest, approver);
  }

  public String declineConnectorRequest(KafkaConnectorRequest topicRequest, String approver) {
    return jdbcUpdateHelper.declineConnectorRequest(topicRequest, approver);
  }

  @Override
  public String declineAclRequest(AclRequests aclReq, String approver) {
    return jdbcUpdateHelper.declineAclRequest(aclReq, approver);
  }

  @Override
  public String claimAclRequest(AclRequests aclReq, RequestStatus status) {
    return jdbcUpdateHelper.claimAclRequest(aclReq, status);
  }

  @Override
  public String updateAcl(Acl acl) {
    return jdbcUpdateHelper.updateAcl(acl);
  }

  public String updateAclRequest(
      AclRequests aclReq, String approver, Map<String, String> jsonParams, boolean saveReqOnly) {
    return jdbcUpdateHelper.updateAclRequest(aclReq, approver, jsonParams, saveReqOnly);
  }

  @Override
  public void updateNewUserRequest(String username, String approver, boolean isApprove) {
    jdbcUpdateHelper.updateNewUserRequest(username, approver, isApprove);
  }

  public String updateSchemaRequest(SchemaRequest schemaRequest, String approver) {
    return jdbcUpdateHelper.updateSchemaRequest(schemaRequest, approver);
  }

  @Override
  public String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver) {
    return jdbcUpdateHelper.updateSchemaRequestDecline(schemaRequest, approver);
  }

  public String updatePassword(String username, String pwd) {
    return jdbcUpdateHelper.updatePassword(username, pwd);
  }

  @Override
  public String resetPassword(String username, String resetToken, String pwd)
      throws KlawNotAuthorizedException {
    return jdbcUpdateHelper.resetPassword(username, pwd, resetToken);
  }

  public String generatePasswordResetToken(String username) {
    return jdbcUpdateHelper.generatePasswordResetToken(username);
  }

  @Override
  public String updateUser(UserInfo userInfo) {
    return jdbcUpdateHelper.updateUser(userInfo);
  }

  @Override
  public String updateUserTeam(String userId, int teamId) {
    return jdbcUpdateHelper.updateUserTeam(userId, teamId);
  }

  @Override
  public String updateTeam(Team team) {
    return jdbcUpdateHelper.updateTeam(team);
  }

  @Override
  public String updateKwProperty(KwProperties kwProperties, int tenantId) {
    return jdbcUpdateHelper.updateKwProperty(kwProperties, tenantId);
  }

  /*--------------------Delete */
  @Override
  public String deleteConnectorRequest(int connectorId, int tenantId) {
    return jdbcDeleteHelper.deleteConnectorRequest(connectorId, tenantId);
  }

  @Override
  public String deleteTopicRequest(int topicId, String userName, int tenantId) {
    return jdbcDeleteHelper.deleteTopicRequest(topicId, userName, tenantId);
  }

  @Override
  public String deleteOperationalRequest(int operationalRequestId, String userName, int tenantId) {
    return jdbcDeleteHelper.deleteOperationalRequest(operationalRequestId, userName, tenantId);
  }

  @Override
  public String deleteTopic(int topicId, int tenantId) {
    return jdbcDeleteHelper.deleteTopic(topicId, tenantId);
  }

  @Override
  public String deleteConnector(int connectorId, int tenantId) {
    return jdbcDeleteHelper.deleteConnector(connectorId, tenantId);
  }

  @Override
  public String deleteAclRequest(int req_no, String userName, int tenantId) {
    return jdbcDeleteHelper.deleteAclRequest(req_no, userName, tenantId);
  }

  @Override
  public String deleteEnvironmentRequest(String envId, int tenantId) {
    return jdbcDeleteHelper.deleteEnvironment(envId, tenantId);
  }

  @Override
  public String deleteCluster(int clusterId, int tenantId) {
    return jdbcDeleteHelper.deleteCluster(clusterId, tenantId);
  }

  @Override
  public String deleteRole(String roleId, int tenantId) {
    return jdbcDeleteHelper.deleteRole(roleId, tenantId);
  }

  @Override
  public String deleteUserRequest(String userId) {
    return jdbcDeleteHelper.deleteUserRequest(userId);
  }

  @Override
  public String deleteTeamRequest(Integer teamId, int tenantId) {
    return jdbcDeleteHelper.deleteTeamRequest(teamId, tenantId);
  }

  @Override
  public String deleteSchemaRequest(int schemaId, String userName, int tenantId) {
    return jdbcDeleteHelper.deleteSchemaRequest(schemaId, userName, tenantId);
  }

  @Override
  public void deleteSchemas(Topic topicObj) {
    jdbcDeleteHelper.deleteSchemas(topicObj);
  }

  @Override
  public void deleteSchema(int tenantId, String topicName, String schemaEnv) {
    jdbcDeleteHelper.deleteSchemasWithOptions(tenantId, topicName, schemaEnv);
  }

  @Override
  public String deleteAllUsers(int tenantId) {
    return jdbcDeleteHelper.deleteAllUsers(tenantId);
  }

  @Override
  public String deleteAllTeams(int tenantId) {
    return jdbcDeleteHelper.deleteAllTeams(tenantId);
  }

  @Override
  public String deleteAllEnvs(int tenantId) {
    return jdbcDeleteHelper.deleteAllEnvs(tenantId);
  }

  @Override
  public String deleteAllClusters(int tenantId) {
    return jdbcDeleteHelper.deleteAllClusters(tenantId);
  }

  @Override
  public String deleteAllRolesPerms(int tenantId) {
    return jdbcDeleteHelper.deleteAllRolesPerms(tenantId);
  }

  @Override
  public String deleteAllKwProps(int tenantId) {
    return jdbcDeleteHelper.deleteAllKwProps(tenantId);
  }

  @Override
  public String deleteTxnData(int tenantId) {
    return jdbcDeleteHelper.deleteTxnData(tenantId);
  }

  @Override
  public String setTenantActivestatus(int tenantId, boolean status) {
    return jdbcUpdateHelper.setTenantActivestatus(tenantId, status);
  }

  @Override
  public String updateTenant(int tenantId, String organizationName) {
    return jdbcUpdateHelper.updateTenant(tenantId, organizationName);
  }

  @Override
  public String disableTenant(int tenantId) {
    return jdbcUpdateHelper.disableTenant(tenantId);
  }

  @Override
  public Optional<ProductDetails> getProductDetails(String name) {
    return jdbcSelectHelper.selectProductDetails(name);
  }

  @Override
  public boolean existsKafkaComponentsForEnv(String env, int tenantId) {
    return jdbcSelectHelper.existsKafkaComponentsForEnv(env, tenantId);
  }

  @Override
  public boolean existsConnectorComponentsForEnv(String env, int tenantId) {
    return jdbcSelectHelper.existsConnectorComponentsForEnv(env, tenantId);
  }

  @Override
  public boolean existsSchemaComponentsForEnv(String env, int tenantId) {
    return jdbcSelectHelper.existsSchemaComponentsForEnv(env, tenantId);
  }

  @Override
  public boolean existsComponentsCountForTeam(Integer teamId, int tenantId) {
    return jdbcSelectHelper.existsComponentsCountForTeam(teamId, tenantId);
  }

  @Override
  public boolean existsComponentsCountForUser(String userId, int tenantId) {
    return jdbcSelectHelper.existsComponentsCountForUser(userId, tenantId);
  }

  @Override
  public Map<String, Set<String>> getTopicAndVersionsForEnvAndTenantId(String envId, int tenantId) {
    return jdbcSelectHelper.getTopicAndVersionsForEnvAndTenantId(envId, tenantId);
  }

  @Override
  public MessageSchema getTeamIdFromSchemaTopicNameAndEnvAndTenantId(
      String schemaTopicName, String envId, int tenantId) {
    return jdbcSelectHelper.getTeamIdFromSchemaTopicNameAndEnvAndTenantId(
        schemaTopicName, envId, tenantId);
  }

  @Override
  public int getAllTopicsCountInAllTenants() {
    return jdbcSelectHelper.getAllTopicsCountInAllTenants();
  }

  @Override
  public List<TopicRequest> getAllTopicRequests() {
    return jdbcSelectHelper.getAllTopicRequests();
  }

  @Override
  public List<KafkaConnectorRequest> getAllConnectorRequests() {
    return jdbcSelectHelper.getAllConnectorRequests();
  }

  @Override
  public Integer getNextClusterId(int tenantId) {
    return jdbcSelectHelper.getNextClusterId(tenantId);
  }

  @Override
  public Integer getNextEnvId(int tenantId) {
    return jdbcSelectHelper.getNextEnvId(tenantId);
  }

  @Override
  public Integer getNextTeamId(int tenantId) {
    return jdbcSelectHelper.getNextTeamId(tenantId);
  }

  @Override
  public void insertIntoKwEntitySequence(String entityName, int maxId, int tenantId) {
    jdbcInsertHelper.insertIntoKwEntitySequence(entityName, maxId, tenantId);
  }

  @Override
  public List<KwKafkaConnector> getAllConnectors() {
    return jdbcSelectHelper.getAllConnectors();
  }

  @Override
  public List<Topic> getAllTopics() {
    return jdbcSelectHelper.getAllTopics();
  }

  @Override
  public List<Acl> getAllSubscriptions() {
    return jdbcSelectHelper.getAllSubscriptions();
  }

  @Override
  public List<AclRequests> getAllAclRequests() {
    return jdbcSelectHelper.getAllAclRequests();
  }

  @Override
  public List<SchemaRequest> getAllSchemaRequests() {
    return jdbcSelectHelper.getAllSchemaRequests();
  }

  @Override
  public List<MessageSchema> getAllSchemas() {
    return jdbcSelectHelper.selectAllSchemas();
  }

  @Override
  public List<Team> getTeams() {
    return jdbcSelectHelper.selectTeams();
  }

  @Override
  public List<Env> getEnvs() {
    return jdbcSelectHelper.selectEnvs();
  }

  @Override
  public List<KwProperties> getKwProperties() {
    return jdbcSelectHelper.selectKwProperties();
  }

  @Override
  public List<KwClusters> getClusters() {
    return jdbcSelectHelper.getClusters();
  }

  @Override
  public String updateJsonParams(Map<String, String> jsonParams, Integer req_no, int tenantId) {
    return jdbcUpdateHelper.updateJsonParams(jsonParams, req_no, tenantId);
  }

  @Override
  public String deleteAcls(List<Acl> listDeleteAcls, int tenantId) {
    return jdbcDeleteHelper.deleteAcls(listDeleteAcls, tenantId);
  }

  public String updateDbWithUpdatedVersions(List<MessageSchema> schemaListUpdated) {
    return jdbcUpdateHelper.updateDbWithUpdatedVersions(schemaListUpdated);
  }
}
