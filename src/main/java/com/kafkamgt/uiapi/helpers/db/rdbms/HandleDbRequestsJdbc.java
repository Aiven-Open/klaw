package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.EntityType;
import com.kafkamgt.uiapi.model.MetadataOperationType;
import com.kafkamgt.uiapi.model.RequestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Configuration
public class HandleDbRequestsJdbc implements HandleDbRequests {

    @Value("${kafkawize.dbscripts.create.tables:false}")
    String dbCreateTables;

    @Value("${kafkawize.dbscripts.insert.basicdata:false}")
    String dbInsertData;

    @Autowired
    SelectDataJdbc jdbcSelectHelper;

    @Autowired
    InsertDataJdbc jdbcInsertHelper;

    @Autowired
    UpdateDataJdbc jdbcUpdateHelper;

    @Autowired
    DeleteDataJdbc jdbcDeleteHelper;

    /*--------------------Insert */

    public HashMap<String, String> requestForTopic(TopicRequest topicRequest){
        return jdbcInsertHelper.insertIntoRequestTopic(topicRequest);
    }

    public HashMap<String, String> requestForConnector(KafkaConnectorRequest connectorRequest){
        return jdbcInsertHelper.insertIntoRequestConnector(connectorRequest);
    }

    public HashMap<String, String> requestForAcl(AclRequests aclReq){
        return jdbcInsertHelper.insertIntoRequestAcl(aclReq);
    }

    public String addNewUser(UserInfo userInfo){
        return jdbcInsertHelper.insertIntoUsers(userInfo);
    }

    public String addNewTeam(Team team){
        return jdbcInsertHelper.insertIntoTeams(team);
    }

    @Override
    public String addNewTenant(KwTenants kwTenants) {
        return jdbcInsertHelper.addNewTenant(kwTenants);
    }

    public String addNewEnv(Env env){
        return jdbcInsertHelper.insertIntoEnvs(env);
    }

    @Override
    public String addNewCluster(KwClusters kwClusters) {
        return jdbcInsertHelper.insertIntoClusters(kwClusters);
    }

    public String requestForSchema(SchemaRequest schemaRequest){
        return jdbcInsertHelper.insertIntoRequestSchema(schemaRequest);
    }

    public String addToSynctopics(List<Topic> topicRequests) {
        return jdbcInsertHelper.insertIntoTopicSOT(topicRequests,true);
    }

    public String addToSyncConnectors(List<KwKafkaConnector> topicRequests) {
        return jdbcInsertHelper.insertIntoConnectorSOT(topicRequests,true);
    }

    public String addToSyncacls(List<Acl> acls) {
        return jdbcInsertHelper.insertIntoAclsSOT(acls, true);
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

    /*--------------------Select */

    public List<TopicRequest> getAllTopicRequests(String requestor, int tenantId){
        return jdbcSelectHelper.selectTopicRequestsByStatus(false, requestor, RequestStatus.created.name(), false, tenantId);
    }

    public List<TopicRequest> getCreatedTopicRequests(String requestor, String status, boolean showRequestsOfAllTeams, int tenantId){
        return jdbcSelectHelper.selectTopicRequestsByStatus(true, requestor, status, showRequestsOfAllTeams, tenantId);
    }

    public List<KafkaConnectorRequest> getAllConnectorRequests(String requestor, int tenantId){
        return jdbcSelectHelper.selectConnectorRequestsByStatus(false, requestor, RequestStatus.created.name(),
                false, tenantId);
    }

    public List<KafkaConnectorRequest> getCreatedConnectorRequests(String requestor, String status, boolean showRequestsOfAllTeams, int tenantId){
        return jdbcSelectHelper.selectConnectorRequestsByStatus(true, requestor, status, showRequestsOfAllTeams, tenantId);
    }

    public TopicRequest selectTopicRequestsForTopic(int topicId, int tenantId) {
        return jdbcSelectHelper.selectTopicRequestsForTopic(topicId, tenantId);
    }

    public KafkaConnectorRequest selectConnectorRequestsForConnector(int connectorId, int tenantId) {
        return jdbcSelectHelper.selectConnectorRequestsForConnector(connectorId, tenantId);
    }

    @Override
    public List<TopicRequest> selectTopicRequests(String topicName, String envId, String status, int tenantId) {
        return jdbcSelectHelper.selectTopicRequests(topicName, envId, status, tenantId);
    }

    @Override
    public List<KafkaConnectorRequest> selectConnectorRequests(String connectorName, String envId, String status, int tenantId) {
        return jdbcSelectHelper.selectConnectorRequests(connectorName, envId, status, tenantId);
    }

    @Override
    public List<Topic> getSyncTopics(String env, Integer teamId, int tenantId){
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
    public List<Topic> getTopicDetailsPerEnv(String topicName, String envId, int tenantId) {
        return jdbcSelectHelper.getTopicDetailsPerEnv(topicName, envId, tenantId);
    }

    @Override
    public List<Topic> getTopicsFromEnv(String envId, int tenantId) {
        return jdbcSelectHelper.getTopicsFromEnv(envId, tenantId);
    }

    @Override
    public Optional<Topic> getTopicFromId(int topicId,int tenantId) {
        return jdbcSelectHelper.getTopicFromId(topicId, tenantId);
    }

    @Override
    public List<Topic> getAllTopics(int tenantId) {
        return jdbcSelectHelper.getTopics("", true, tenantId);
    }

    @Override
    public List<Acl> getSyncAcls(String env, int tenantId){
        return jdbcSelectHelper.selectSyncAcls(env, tenantId);
    }

    @Override
    public List<Acl> getSyncAcls(String env, String topic, int tenantId) {
        return jdbcSelectHelper.selectSyncAcls(env, topic, tenantId);
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
    public Acl selectSyncAclsFromReqNo(int reqNo, int tenantId) {
        return jdbcSelectHelper.selectSyncAclsFromReqNo(reqNo, tenantId);
    }

    @Override
    public List<AclRequests> getAllAclRequests(boolean allReqs, String requestor, String role, String status, boolean showRequestsOfAllTeams, int tenantId){
        return jdbcSelectHelper.selectAclRequests(allReqs, requestor, role, status, showRequestsOfAllTeams, tenantId);
    }

    @Override
    public List<AclRequests> getCreatedAclRequestsByStatus(String requestor, String status, boolean showRequestsOfAllTeams, int tenantId){
        return jdbcSelectHelper.selectAclRequests(true, requestor, "", status, showRequestsOfAllTeams, tenantId);
    }

    @Override
    public List<SchemaRequest> getAllSchemaRequests(boolean allReqs, String requestor,int tenantId){
        return jdbcSelectHelper.selectSchemaRequests(allReqs, requestor, tenantId);
    }

    @Override
    public List<SchemaRequest> getCreatedSchemaRequests(String requestor, int tenantId){
        return jdbcSelectHelper.selectSchemaRequests(true, requestor, tenantId);
    }

    @Override
    public SchemaRequest selectSchemaRequest(int avroSchemaId, int tenantId){
        return jdbcSelectHelper.selectSchemaRequest(avroSchemaId, tenantId);
    }

    @Override
    public List<Team> selectAllTeamsOfUsers(String username, int tenantId){
        return jdbcSelectHelper.selectTeamsOfUsers(username, tenantId);
    }

    @Override
    public List<Team> selectAllTeams(int tenantId){
        return jdbcSelectHelper.selectAllTeams(tenantId);
    }

    @Override
    public Team selectTeamDetails(Integer teamId, int tenantId) {
        return jdbcSelectHelper.selectTeamDetails(teamId, tenantId);
    }

    @Override
    public Team selectTeamDetailsFromName(String teamName, int tenantId) {
        return jdbcSelectHelper.selectTeamDetailsFromName(teamName, tenantId);
    }

    @Override
    public HashMap<String, String> getDashboardInfo(Integer teamId, int tenantId) {
        return jdbcSelectHelper.getDashboardInfo(teamId, tenantId);
    }

    @Override
    public List<UserInfo> selectAllUsersInfo(int tenantId){
        return jdbcSelectHelper.selectAllUsersInfo(tenantId);
    }

    @Override
    public List<UserInfo> selectAllUsersAllTenants() {
        return jdbcSelectHelper.selectAllUsersAllTenants();
    }

    @Override
    public List<UserInfo> selectAllUsersInfoForTeam(Integer teamId, int tenantId) {
        return jdbcSelectHelper.selectAllUsersInfoForTeam(teamId, tenantId);
    }

    @Override
    public List<RegisterUserInfo> selectAllRegisterUsersInfoForTenant(int tenantId) {
        return jdbcSelectHelper.selectAllRegisterUsersInfoForTenant(tenantId);
    }

    @Override
    public List<RegisterUserInfo> selectAllRegisterUsersInfo() {
        return jdbcSelectHelper.selectAllRegisterUsersInfo();
    }

    public UserInfo getUsersInfo(String username){
        return jdbcSelectHelper.selectUserInfo(username);
    }

    @Override
    public RegisterUserInfo getRegisterUsersInfo(String username) {
        return jdbcSelectHelper.selectRegisterUsersInfo(username);
    }

    @Override
    public AclRequests selectAcl(int req_no, int tenantId){
        return jdbcSelectHelper.selectAcl(req_no, tenantId);
    }

    @Override
    public List<Topic> getTopicTeam(String topicName,int tenantId){
        return jdbcSelectHelper.selectTopicDetails(topicName, tenantId);
    }

    public List<KwKafkaConnector> getConnectorsFromName(String connectorName, int tenantId){
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
    public List<Env> selectAllEnvs(int tenantId) {
        return jdbcSelectHelper.selectAllEnvs("all", tenantId);
    }

    @Override
    public List<Env> selectAllKafkaEnvs(int tenantId){
        return jdbcSelectHelper.selectAllEnvs("kafka", tenantId);
    }

    @Override
    public List<Env> selectAllSchemaRegEnvs(int tenantId){
        return jdbcSelectHelper.selectAllEnvs("schemaregistry", tenantId);
    }

    @Override
    public List<Env> selectAllKafkaConnectEnvs(int tenantId) {
        return jdbcSelectHelper.selectAllEnvs("kafkaconnect", tenantId);
    }

    @Override
    public Env selectEnvDetails(String env, int tenantId){
        return jdbcSelectHelper.selectEnvDetails(env, tenantId);
    }

    public List<ActivityLog> selectActivityLog(String user, String env, boolean allReqs, int tenantId){
        return jdbcSelectHelper.selectActivityLog(user, env, allReqs, tenantId);
    }

    @Override
    public HashMap<Integer, HashMap<String, HashMap<String, String>>> selectAllKwProperties() {
        return jdbcSelectHelper.selectAllKwProperties();
    }

    @Override
    public List<KwProperties> selectAllKwPropertiesPerTenant(int tenantId) {
        return jdbcSelectHelper.selectAllKwPropertiesPerTenant(tenantId);
    }

    @Override
    public String insertDefaultKwProperties(List<KwProperties> kwPropertiesList){
        return jdbcInsertHelper.insertDefaultKwProperties(kwPropertiesList);
    }

    @Override
    public String insertDefaultRolesPermissions(List<KwRolesPermissions> kwRolesPermissionsList){
        return jdbcInsertHelper.insertDefaultRolesPermissions(kwRolesPermissionsList);
    }

    @Override
    public String insertProductDetails(ProductDetails productDetails) {
        return jdbcInsertHelper.insertProductDetails(productDetails);
    }

    @Override
    public Integer getNextTopicRequestId(String idType,int tenantId) {
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
    public List<KwClusters> getAllClusters(String typeOfCluster, int tenantId) {
        return jdbcSelectHelper.getAllClusters(typeOfCluster, tenantId);
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
    public HashMap<String, String> getDashboardStats(Integer teamId, int tenantId) {
        return jdbcSelectHelper.getDashboardStats(teamId, tenantId);
    }

    @Override
    public List<Topic> selectAllTopicsByTopictypeAndTeamname(String topicType, Integer teamId, int tenantId) {
        return jdbcSelectHelper.selectAllTopicsByTopictypeAndTeamname(topicType, teamId, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectActivityLogForLastDays(int numberOfDays, String[] envId, int tenantId) {
        return jdbcSelectHelper.selectActivityLogForLastDays(numberOfDays, envId, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectActivityLogByTeam(Integer teamId, int numberOfDays, int tenantId) {
        return jdbcSelectHelper.selectActivityLogByTeam(teamId, numberOfDays, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectTopicsCountByTeams(Integer teamId, int tenantId) {
        return jdbcSelectHelper.selectTopicsCountByTeams(teamId, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectTopicsCountByEnv(Integer tenantId) {
        return jdbcSelectHelper.selectTopicsCountByEnv(tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectPartitionsCountByEnv(Integer teamId, Integer tenantId) {
        return jdbcSelectHelper.selectPartitionsCountByEnv(teamId, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectAclsCountByEnv(Integer teamId, Integer tenantId) {
        return jdbcSelectHelper.selectAclsCountByEnv(teamId, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectAclsCountByTeams(String aclType, Integer teamId, Integer tenantId) {
        return jdbcSelectHelper.selectAclsCountByTeams(aclType, teamId, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectAllTopicsForTeamGroupByEnv(Integer teamId, int tenantId) {
        return jdbcSelectHelper.selectAllTopicsForTeamGroupByEnv(teamId, tenantId);
    }

    @Override
    public List<HashMap<String, String>> selectAllMetrics(String metricsType, String metricsName, String env) {
        return jdbcSelectHelper.selectAllMetrics(metricsType, metricsName, env);
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
    public String updateTopicRequest(TopicRequest topicRequest, String approver){
        return jdbcUpdateHelper.updateTopicRequest(topicRequest, approver);
    }

    @Override
    public String updateConnectorRequest(KafkaConnectorRequest topicRequest, String approver){
        return jdbcUpdateHelper.updateConnectorRequest(topicRequest, approver);
    }

    @Override
    public String updateTopicRequestStatus(TopicRequest topicRequest, String approver) {
        return jdbcUpdateHelper.updateTopicRequestStatus(topicRequest, approver);
    }

    @Override
    public String updateConnectorRequestStatus(KafkaConnectorRequest connectorRequest, String approver) {
        return jdbcUpdateHelper.updateConnectorRequestStatus(connectorRequest, approver);
    }

    public String declineTopicRequest(TopicRequest topicRequest, String approver){
        return jdbcUpdateHelper.declineTopicRequest(topicRequest, approver);
    }

    public String declineConnectorRequest(KafkaConnectorRequest topicRequest, String approver){
        return jdbcUpdateHelper.declineConnectorRequest(topicRequest, approver);
    }

    @Override
    public String declineAclRequest(AclRequests aclReq, String approver) {
        return jdbcUpdateHelper.declineAclRequest(aclReq, approver);
    }

    public String updateAclRequest(AclRequests aclReq, String approver){
        return jdbcUpdateHelper.updateAclRequest(aclReq, approver);
    }

    @Override
    public void updateNewUserRequest(String username, String approver, boolean isApprove) {
        jdbcUpdateHelper.updateNewUserRequest(username, approver, isApprove);
    }

    public String updateSchemaRequest(SchemaRequest schemaRequest, String approver){
        return jdbcUpdateHelper.updateSchemaRequest(schemaRequest,  approver);
    }

    @Override
    public String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver) {
        return jdbcUpdateHelper.updateSchemaRequestDecline(schemaRequest,  approver);
    }

    public String updatePassword(String username, String pwd){
        return jdbcUpdateHelper.updatePassword(username,pwd);
    }

    @Override
    public String updateUser(UserInfo userInfo) {
        return jdbcUpdateHelper.updateUser(userInfo);
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
    public String deleteConnectorRequest(int connectorId, int tenantId){
        return jdbcDeleteHelper.deleteConnectorRequest(connectorId, tenantId);
    }

    @Override
    public String deleteTopicRequest(int topicId, int tenantId){
        return jdbcDeleteHelper.deleteTopicRequest(topicId, tenantId);
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
    public String deleteAclRequest(int req_no, int tenantId){
        return jdbcDeleteHelper.deleteAclRequest(req_no, tenantId);
    }

    @Override
    public String deleteAclSubscriptionRequest(int req_no, int tenantId) {
        return jdbcDeleteHelper.deleteAclSubscriptionRequest(req_no, tenantId);
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
    public String deleteSchemaRequest(int schemaId, int tenantId){
        return jdbcDeleteHelper.deleteSchemaRequest(schemaId, tenantId);
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
    public String deleteTenant(int tenantId) {
        return jdbcDeleteHelper.deleteTenant(tenantId);
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
    public Optional<ProductDetails> selectProductDetails(String name) {
        return jdbcSelectHelper.selectProductDetails(name);
    }

    @Override
    public int findAllKafkaComponentsCountForEnv(String env, int tenantId) {
        return jdbcSelectHelper.findAllKafkaComponentsCountForEnv(env, tenantId);
    }

    @Override
    public int findAllConnectorComponentsCountForEnv(String env, int tenantId) {
        return jdbcSelectHelper.findAllConnectorComponentsCountForEnv(env, tenantId);
    }

    @Override
    public int findAllSchemaComponentsCountForEnv(String env, int tenantId) {
        return jdbcSelectHelper.findAllSchemaComponentsCountForEnv(env, tenantId);
    }

    @Override
    public int findAllComponentsCountForTeam(Integer teamId, int tenantId) {
        return jdbcSelectHelper.findAllComponentsCountForTeam(teamId, tenantId);
    }

    @Override
    public int getAllTopicsCountInAllTenants() {
        return jdbcSelectHelper.getAllTopicsCountInAllTenants();
    }

}
