package io.aiven.klaw.helpers;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.response.DashboardStats;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface HandleDbRequests {

  /*--------------------Insert */

  Map<String, String> requestForTopic(TopicRequest topicRequest);

  Map<String, String> requestForConnector(KafkaConnectorRequest connectorRequest);

  Map<String, String> requestForAcl(AclRequests aclReq);

  Map<String, String> requestForConsumerOffsetsReset(OperationalRequest operationalRequest);

  String addNewUser(UserInfo userInfo);

  String addNewTeam(Team team);

  String addNewTenant(KwTenants kwTenants);

  String addNewEnv(Env env);

  String addNewCluster(KwClusters kwClusters);

  String requestForSchema(SchemaRequest schemaRequest);

  CRUDResponse<Topic> addToSynctopics(List<Topic> topicRequests);

  String addToSyncConnectors(List<KwKafkaConnector> connectorRequests);

  String addToSyncacls(List<Acl> acls);

  public Integer getNextSeqIdAndUpdate(String entityName, int tenantId);

  String registerUser(RegisterUserInfo newUser);

  String registerUserForAD(RegisterUserInfo newUser);

  String updatePermissions(List<KwRolesPermissions> permissions, String addDelete);

  String insertMetrics(KwMetrics kwMetrics);

  /*--------------------Select */

  List<Topic> getAllTopicsByTopicNameAndTeamIdAndTenantId(
      String topicName, int teamId, int tenantId);

  String getRegistrationId(String userName);

  RegisterUserInfo getRegistrationDetails(String registrationId, String status);

  List<TopicRequest> getAllTopicRequests(
      String requestor,
      String status,
      RequestOperationType requestoperationType,
      String env,
      String wildcardSearch,
      boolean isMyRequest,
      int tenantId);

  List<OperationalRequest> getOperationalRequests(
      String userName,
      OperationalRequestType operationalRequestType,
      String requestStatus,
      String env,
      String wildcardSearch,
      boolean isMyRequest,
      int tenantId);

  Map<String, Map<String, Long>> getTopicRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor);

  Map<String, Map<String, Long>> getAclRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor);

  Map<String, Map<String, Long>> getSchemaRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor);

  Map<String, Map<String, Long>> getConnectorRequestsCounts(
      int teamId, RequestMode requestMode, int tenantId, String requestor);

  List<TopicRequest> getCreatedTopicRequests(
      String requestor, String status, boolean showRequestsOfAllTeams, int tenantId);

  List<KafkaConnectorRequest> getAllConnectorRequests(
      String requestor,
      RequestOperationType requestOperationType,
      RequestStatus requestStatus,
      String env,
      String wildcardSearch,
      int tenantId,
      boolean isMyRequest);

  List<KafkaConnectorRequest> getCreatedConnectorRequests(
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId,
      String env,
      RequestOperationType requestOperationType,
      String search);

  TopicRequest getTopicRequestsForTopic(int topicId, int tenantId);

  KafkaConnectorRequest getConnectorRequestsForConnector(int connectorId, int tenantId);

  List<TopicRequest> getTopicRequests(String topicName, String envId, String status, int tenantId);

  List<KafkaConnectorRequest> getConnectorRequests(
      String connectorName, String envId, String status, int tenantId);

  List<Topic> getSyncTopics(String env, Integer teamId, int tenantId);

  List<KwKafkaConnector> getSyncConnectors(String envId, Integer teamId, int tenantId);

  List<Topic> getTopics(String topicName, int tenantId);

  List<KwKafkaConnector> getConnectors(String connectorName, int tenantId);

  List<Topic> getTopicsFromEnv(String envId, int tenantId);

  Optional<Topic> getTopicFromId(int topicId, int tenantId);

  List<Topic> getAllTopics(int tenantId);

  List<Acl> getSyncAcls(String env, int tenantId);

  List<Acl> getSyncAcls(String env, String topic, int tenantId);

  List<Acl> getPrefixedAclsSOT(String env, int tenantId);

  List<Acl> getUniqueConsumerGroups(int tenantId);

  boolean validateIfConsumerGroupUsedByAnotherTeam(
      Integer teamId, int tenantId, String consumerGroup);

  Acl getSyncAclsFromReqNo(int reqNo, int tenantId);

  boolean existsAclRequest(String topicName, String requestStatus, String env, int tenantId);

  boolean existsSchemaRequest(String topicName, String requestStatus, String env, int tenantId);

  boolean existsSchemaRequest(
      String topicName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId);

  boolean existsTopicRequest(String topicName, String requestStatus, String env, int tenantId);

  boolean existsTopicRequest(
      String topicName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId);

  boolean existsClaimTopicRequest(String topicName, String requestStatus, int tenantId);

  boolean existsConnectorRequest(
      String connectorName, String requestStatus, String env, int tenantId);

  boolean existsConnectorRequest(
      String connectorName,
      String requestStatus,
      String requestOperationType,
      String env,
      int tenantId);

  boolean existsSchemaForTopic(String topicName, String env, int tenantId);

  List<AclRequests> getAllAclRequests(
      boolean allReqs,
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
      int tenantId);

  List<AclRequests> getCreatedAclRequestsByStatus(
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      RequestOperationType requestOperationType,
      String topic,
      String environment,
      String wildcardSearch,
      AclType aclType,
      int tenantId);

  List<SchemaRequest> getAllSchemaRequests(
      boolean allReqs,
      String requestor,
      int tenantId,
      RequestOperationType requestOperationType,
      String topic,
      String env,
      String status,
      String search,
      boolean showRequestsOfAllTeams,
      boolean isMyRequest);

  SchemaRequest getSchemaRequest(int avroSchemaId, int tenantId);

  List<Team> getAllTeamsOfUsers(String username, int tenantId);

  List<Team> getAllTeams(int tenantId);

  Team getTeamDetails(Integer teamId, int tenantId);

  Team getTeamDetailsFromName(String teamName, int defaultTenantId);

  Map<String, String> getDashboardInfo(Integer teamId, int tenantId);

  List<UserInfo> getAllUsersInfo(int tenantId);

  List<UserInfo> getAllUsersAllTenants();

  List<UserInfo> getAllUsersInfoForTeam(Integer teamId, int tenantId);

  List<RegisterUserInfo> getAllRegisterUsersInfoForTenant(int tenantId);

  List<RegisterUserInfo> getAllRegisterUsersInformation();

  RegisterUserInfo getFirstStagingRegisterUsersInfo(String userName);

  UserInfo getUsersInfo(String username);

  RegisterUserInfo getRegisterUsersInfo(String username);

  AclRequests getAcl(int req_no, int tenantId);

  OperationalRequest getOperationalRequest(int reqId, int tenantId);

  List<KwKafkaConnector> getConnectorsFromName(String connectorName, int tenantId);

  List<Topic> getTopicsforTeam(Integer teamId, int tenantId);

  List<Acl> getConsumerGroupsforTeam(Integer teamId, int tenantId);

  List<Acl> getAllConsumerGroups(int tenantId);

  List<Env> getAllEnvs(int tenantId);

  List<Env> getAllKafkaEnvs(int tenantId);

  List<Env> getAllSchemaRegEnvs(int tenantId);

  List<Env> getAllKafkaConnectEnvs(int tenantId);

  Env getEnvDetails(String env, int tenantId);

  List<ActivityLog> getActivityLog(String user, String env, boolean allReqs, int tenantId);

  Map<Integer, Map<String, Map<String, String>>> getAllKwProperties();

  List<KwProperties> getAllKwPropertiesPerTenant(int tenantId);

  String insertDefaultKwProperties(List<KwProperties> kwPropertiesList);

  String insertDefaultRolesPermissions(List<KwRolesPermissions> kwRolesPermissionsList);

  String insertProductDetails(ProductDetails productDetails);

  String insertIntoMessageSchemaSOT(List<MessageSchema> schemaList);

  Integer getNextTopicRequestId(String idType, int tenantId);

  Integer getNextConnectorRequestId(String idType, int tenantId);

  List<KwTenants> getTenants();

  Optional<KwTenants> getMyTenants(int tenantId);

  List<KwRolesPermissions> getRolesPermissions();

  List<KwRolesPermissions> getRolesPermissionsPerTenant(int tenantId);

  List<KwClusters> getAllClusters(KafkaClustersType typeOfCluster, int tenantId);

  KwClusters getClusterDetails(int id, int tenantId);

  // Analytics - charts - dashboard

  DashboardStats getDashboardStats(Integer teamId, int tenantId);

  List<Topic> getAllTopicsByTopictypeAndTeamname(String topicType, Integer teamId, int tenantId);

  List<Map<String, String>> getActivityLogForLastDays(
      int numberOfDays, String[] envIdList, int tenantId);

  List<Map<String, String>> getActivityLogByTeam(Integer teamId, int numberOfDays, int tenantId);

  List<Map<String, String>> getTopicsCountByTeams(Integer teamId, int tenantId);

  List<Map<String, String>> getTopicsCountByEnv(Integer tenantId);

  List<Map<String, String>> getPartitionsCountByEnv(Integer teamId, Integer tenantId);

  List<Map<String, String>> getAclsCountByEnv(Integer teamId, Integer tenantId);

  List<Map<String, String>> getAclsCountByTeams(String aclType, Integer teamId, Integer tenantId);

  List<Map<String, String>> getAllTopicsForTeamGroupByEnv(Integer teamId, int tenantId);

  List<Map<String, String>> getAllMetrics(String metricsType, String metricsName, String env);

  Optional<MessageSchema> getFirstSchemaForTenantAndEnvAndTopicAndVersion(
      int tenantId, String schemaEnvId, String topicName, String schemaVersion);

  List<MessageSchema> getSchemaForTenantAndEnvAndTopic(
      int tenantId, String schemaEnvId, String topicName);

  /*--------------------Update */
  String updateTopicDocumentation(Topic topic);

  String updateConnectorDocumentation(KwKafkaConnector topic);

  CRUDResponse<Topic> updateTopicRequest(TopicRequest topicRequest, String approver);

  String updateConnectorRequest(KafkaConnectorRequest topicRequest, String approver);

  Integer getNextClusterId(int tenantId);

  Integer getNextEnvId(int tenantId);

  Integer getNextTeamId(int tenantId);

  void insertIntoKwEntitySequence(String entityName, int maxId, int tenantId);

  String updateTopicRequestStatus(TopicRequest topicRequest, String approver);

  String updateConnectorRequestStatus(KafkaConnectorRequest topicRequest, String approver);

  String updateAclRequest(
      AclRequests aclRequests,
      String approver,
      Map<String, String> jsonParams,
      boolean saveReqOnly);

  void updateNewUserRequest(String username, String approver, boolean isApprove);

  String updateSchemaRequest(SchemaRequest schemaRequest, String approver);

  String updateDbWithUpdatedVersions(List<MessageSchema> schemaListUpdated);

  String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver);

  String declineTopicRequest(TopicRequest topicRequest, String approver);

  String declineConnectorRequest(KafkaConnectorRequest topicRequest, String approver);

  String declineAclRequest(AclRequests aclRequests, String approver);

  String updatePassword(String username, String pwd);

  String resetPassword(String username, String resetToken, String pwd)
      throws KlawNotAuthorizedException;

  String generatePasswordResetToken(String username);

  String updateUser(UserInfo userInfo);

  String updateUserTeam(String userId, int teamId);

  String updateTeam(Team team);

  String updateKwProperty(KwProperties kwProperties, int tenantId);

  /*--------------------Delete */
  String deleteConnectorRequest(int topicId, int tenantId);

  String deleteTopicRequest(int topicId, String userName, int tenantId);

  String deleteTopic(int topicId, int tenantId);

  String deleteConnector(int connectorId, int tenantId);

  String deleteAclRequest(int req_no, String userName, int tenantId);

  String deleteEnvironmentRequest(String envId, int tenantId);

  String deleteCluster(int clusterId, int tenantId);

  String deleteRole(String roleId, int tenantId);

  String deleteUserRequest(String userId);

  String deleteTeamRequest(Integer teamId, int tenantId);

  String deleteSchemaRequest(int schemaId, String userName, int tenantId);

  void deleteSchemas(Topic topicObj);

  void deleteSchema(int tenantId, String topicName, String schemaEnv);

  String deleteAllUsers(int tenantId);

  String deleteAllTeams(int tenantId);

  String deleteAllEnvs(int tenantId);

  String deleteAllClusters(int tenantId);

  String deleteAllRolesPerms(int tenantId);

  String deleteAllKwProps(int tenantId);

  String deleteTxnData(int tenantId);

  String setTenantActivestatus(int tenantId, boolean status);

  String updateTenant(int tenantId, String organizationName);

  String disableTenant(int tenantId);

  Optional<ProductDetails> getProductDetails(String name);

  boolean existsKafkaComponentsForEnv(String env, int tenantId);

  boolean existsConnectorComponentsForEnv(String env, int tenantId);

  boolean existsSchemaComponentsForEnv(String env, int tenantId);

  boolean existsComponentsCountForTeam(Integer teamId, int tenantId);

  int getAllTopicsCountInAllTenants();

  boolean existsComponentsCountForUser(String userId, int tenantId);

  Map<String, Set<String>> getTopicAndVersionsForEnvAndTenantId(String envId, int tenantId);

  MessageSchema getTeamIdFromSchemaTopicNameAndEnvAndTenantId(
      String schemaTopicName, String envId, int tenantId);

  List<Topic> getAllTopics();

  List<TopicRequest> getAllTopicRequests();

  List<KafkaConnectorRequest> getAllConnectorRequests();

  List<KwKafkaConnector> getAllConnectors();

  List<Acl> getAllSubscriptions();

  List<AclRequests> getAllAclRequests();

  List<SchemaRequest> getAllSchemaRequests();

  List<MessageSchema> getAllSchemas();

  List<Team> getTeams();

  List<Env> getEnvs();

  List<KwProperties> getKwProperties();

  List<KwClusters> getClusters();

  String updateJsonParams(Map<String, String> jsonParams, Integer req_no, int tenantId);

  String deleteAcls(List<Acl> listDeleteAcls, int tenantId);
}
