package io.aiven.klaw.helpers;

import io.aiven.klaw.dao.*;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.RequestMode;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface HandleDbRequests {

  /*--------------------Insert */

  Map<String, String> requestForTopic(TopicRequest topicRequest);

  Map<String, String> requestForConnector(KafkaConnectorRequest connectorRequest);

  Map<String, String> requestForAcl(AclRequests aclReq);

  String addNewUser(UserInfo userInfo);

  String addNewTeam(Team team);

  String addNewTenant(KwTenants kwTenants);

  String addNewEnv(Env env);

  String addNewCluster(KwClusters kwClusters);

  String requestForSchema(SchemaRequest schemaRequest);

  String addToSynctopics(List<Topic> topicRequests);

  String addToSyncConnectors(List<KwKafkaConnector> connectorRequests);

  String addToSyncacls(List<Acl> acls);

  String registerUser(RegisterUserInfo newUser);

  String registerUserForAD(RegisterUserInfo newUser);

  String updatePermissions(List<KwRolesPermissions> permissions, String addDelete);

  String insertMetrics(KwMetrics kwMetrics);

  /*--------------------Select */

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
      String env,
      String wildcardSearch,
      int tenantId);

  List<KafkaConnectorRequest> getCreatedConnectorRequests(
      String requestor,
      String status,
      boolean showRequestsOfAllTeams,
      int tenantId,
      String env,
      String search);

  TopicRequest selectTopicRequestsForTopic(int topicId, int tenantId);

  KafkaConnectorRequest selectConnectorRequestsForConnector(int connectorId, int tenantId);

  List<TopicRequest> selectTopicRequests(
      String topicName, String envId, String status, int tenantId);

  List<KafkaConnectorRequest> selectConnectorRequests(
      String connectorName, String envId, String status, int tenantId);

  List<Topic> getSyncTopics(String env, Integer teamId, int tenantId);

  List<KwKafkaConnector> getSyncConnectors(String envId, Integer teamId, int tenantId);

  List<Topic> getTopics(String topicName, int tenantId);

  List<KwKafkaConnector> getConnectors(String connectorName, int tenantId);

  List<Topic> getTopicDetailsPerEnv(String topicName, String envId, int tenantId);

  List<Topic> getTopicsFromEnv(String envId, int tenantId);

  Optional<Topic> getTopicFromId(int topicId, int tenantId);

  List<Topic> getAllTopics(int tenantId);

  List<Acl> getSyncAcls(String env, int tenantId);

  List<Acl> getSyncAcls(String env, String topic, int tenantId);

  List<Acl> getPrefixedAclsSOT(String env, int tenantId);

  List<Acl> getUniqueConsumerGroups(int tenantId);

  Acl selectSyncAclsFromReqNo(int reqNo, int tenantId);

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
      boolean isMyRequest);

  SchemaRequest selectSchemaRequest(int avroSchemaId, int tenantId);

  List<Team> selectAllTeamsOfUsers(String username, int tenantId);

  List<Team> selectAllTeams(int tenantId);

  Team selectTeamDetails(Integer teamId, int tenantId);

  Team selectTeamDetailsFromName(String teamName, int defaultTenantId);

  Map<String, String> getDashboardInfo(Integer teamId, int tenantId);

  List<UserInfo> selectAllUsersInfo(int tenantId);

  List<UserInfo> selectAllUsersAllTenants();

  List<UserInfo> selectAllUsersInfoForTeam(Integer teamId, int tenantId);

  List<RegisterUserInfo> selectAllRegisterUsersInfoForTenant(int tenantId);

  List<RegisterUserInfo> selectAllRegisterUsersInfo();

  List<RegisterUserInfo> selectAllStagingRegisterUsersInfo(String userId);

  UserInfo getUsersInfo(String username);

  RegisterUserInfo getRegisterUsersInfo(String username);

  AclRequests selectAcl(int req_no, int tenantId);

  List<Topic> getTopicTeam(String topicName, int tenantId);

  List<KwKafkaConnector> getConnectorsFromName(String connectorName, int tenantId);

  List<Topic> getTopicsforTeam(Integer teamId, int tenantId);

  List<Acl> getConsumerGroupsforTeam(Integer teamId, int tenantId);

  List<Acl> getAllConsumerGroups(int tenantId);

  List<Env> selectAllEnvs(int tenantId);

  List<Env> selectAllKafkaEnvs(int tenantId);

  List<Env> selectAllSchemaRegEnvs(int tenantId);

  List<Env> selectAllKafkaConnectEnvs(int tenantId);

  Env selectEnvDetails(String env, int tenantId);

  List<ActivityLog> selectActivityLog(String user, String env, boolean allReqs, int tenantId);

  Map<Integer, Map<String, Map<String, String>>> selectAllKwProperties();

  List<KwProperties> selectAllKwPropertiesPerTenant(int tenantId);

  String insertDefaultKwProperties(List<KwProperties> kwPropertiesList);

  String insertDefaultRolesPermissions(List<KwRolesPermissions> kwRolesPermissionsList);

  String insertProductDetails(ProductDetails productDetails);

  Integer getNextTopicRequestId(String idType, int tenantId);

  Integer getNextConnectorRequestId(String idType, int tenantId);

  List<KwTenants> getTenants();

  Optional<KwTenants> getMyTenants(int tenantId);

  List<KwRolesPermissions> getRolesPermissions();

  List<KwRolesPermissions> getRolesPermissionsPerTenant(int tenantId);

  List<KwClusters> getAllClusters(KafkaClustersType typeOfCluster, int tenantId);

  KwClusters getClusterDetails(int id, int tenantId);

  // Analytics - charts - dashboard

  Map<String, String> getDashboardStats(Integer teamId, int tenantId);

  List<Topic> selectAllTopicsByTopictypeAndTeamname(String topicType, Integer teamId, int tenantId);

  List<Map<String, String>> selectActivityLogForLastDays(
      int numberOfDays, String[] envIdList, int tenantId);

  List<Map<String, String>> selectActivityLogByTeam(Integer teamId, int numberOfDays, int tenantId);

  List<Map<String, String>> selectTopicsCountByTeams(Integer teamId, int tenantId);

  List<Map<String, String>> selectTopicsCountByEnv(Integer tenantId);

  List<Map<String, String>> selectPartitionsCountByEnv(Integer teamId, Integer tenantId);

  List<Map<String, String>> selectAclsCountByEnv(Integer teamId, Integer tenantId);

  List<Map<String, String>> selectAclsCountByTeams(
      String aclType, Integer teamId, Integer tenantId);

  List<Map<String, String>> selectAllTopicsForTeamGroupByEnv(Integer teamId, int tenantId);

  List<Map<String, String>> selectAllMetrics(String metricsType, String metricsName, String env);

  /*--------------------Update */
  String updateTopicDocumentation(Topic topic);

  String updateConnectorDocumentation(KwKafkaConnector topic);

  String updateTopicRequest(TopicRequest topicRequest, String approver);

  String updateConnectorRequest(KafkaConnectorRequest topicRequest, String approver);

  String updateTopicRequestStatus(TopicRequest topicRequest, String approver);

  String updateConnectorRequestStatus(KafkaConnectorRequest topicRequest, String approver);

  String updateAclRequest(AclRequests aclRequests, String approver, String jsonParams);

  void updateNewUserRequest(String username, String approver, boolean isApprove);

  String updateSchemaRequest(SchemaRequest schemaRequest, String approver);

  String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver);

  String declineTopicRequest(TopicRequest topicRequest, String approver);

  String declineConnectorRequest(KafkaConnectorRequest topicRequest, String approver);

  String declineAclRequest(AclRequests aclRequests, String approver);

  String updatePassword(String username, String pwd);

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

  String deleteAclSubscriptionRequest(int req_no, int tenantId);

  String deleteEnvironmentRequest(String envId, int tenantId);

  String deleteCluster(int clusterId, int tenantId);

  String deleteRole(String roleId, int tenantId);

  String deleteUserRequest(String userId);

  String deleteTeamRequest(Integer teamId, int tenantId);

  String deleteSchemaRequest(int schemaId, String userName, int tenantId);

  String deleteAllUsers(int tenantId);

  String deleteAllTeams(int tenantId);

  String deleteAllEnvs(int tenantId);

  String deleteAllClusters(int tenantId);

  String deleteAllRolesPerms(int tenantId);

  String deleteAllKwProps(int tenantId);

  String deleteTxnData(int tenantId);

  String deleteTenant(int tenantId);

  String setTenantActivestatus(int tenantId, boolean status);

  String updateTenant(int tenantId, String organizationName);

  String disableTenant(int tenantId);

  Optional<ProductDetails> selectProductDetails(String name);

  int findAllKafkaComponentsCountForEnv(String env, int tenantId);

  int findAllConnectorComponentsCountForEnv(String env, int tenantId);

  int findAllSchemaComponentsCountForEnv(String env, int tenantId);

  int findAllComponentsCountForTeam(Integer teamId, int tenantId);

  int getAllTopicsCountInAllTenants();
}
