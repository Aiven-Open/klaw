package com.kafkamgt.uiapi.helpers;

import com.kafkamgt.uiapi.dao.*;

import java.util.HashMap;
import java.util.List;


public interface HandleDbRequests {

    void connectToDb(String licenseKey) throws Exception;

    /*--------------------Insert */

    String requestForTopic(TopicRequest topicRequest);

    String requestForAcl(AclRequests aclReq);

    String addNewUser(UserInfo userInfo);

    String addNewTeam(Team team);

    String addNewEnv(Env env);

    String requestForSchema(SchemaRequest schemaRequest);

    String addToSynctopics(List<Topic> topicRequests);

    String addToSyncacls(List<Acl> acls);

    /*--------------------Select */

    HashMap<String, String> getAllRequestsToBeApproved(String requestor, String role);

    List<TopicRequest> getAllTopicRequests(String requestor);
    List<TopicRequest> getCreatedTopicRequests(String requestor);

    TopicRequest selectTopicRequestsForTopic(String topicName, String env) ;

    List<Topic> getSyncTopics(String env, String teamName);

    List<Topic> getTopics(String topicName);

    List<Acl> getSyncAcls(String env);

    List<Acl> getSyncAcls(String env, String topic);

    Acl selectSyncAclsFromReqNo(String reqNo);

    List<AclRequests> getAllAclRequests(String requestor);
    List<AclRequests> getCreatedAclRequests(String requestor);

    List<SchemaRequest> getAllSchemaRequests(String requestor);
    List<SchemaRequest> getCreatedSchemaRequests(String requestor);

    SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env);

    List<Team> selectAllTeamsOfUsers(String username);

    List<Team> selectAllTeams();

    HashMap<String, String> getDashboardInfo(String teamName);

    List<UserInfo> selectAllUsersInfo();

    UserInfo getUsersInfo(String username);

    AclRequests selectAcl(String req_no);

    List<Topic> getTopicTeam(String topicName);

    List<Env> selectAllKafkaEnvs();

    List<Env> selectAllSchemaRegEnvs();

    Env selectEnvDetails(String env);

    List<ActivityLog> selectActivityLog(String user, String env);

    /*--------------------Update */
    String updateTopicRequest(TopicRequest topicRequest, String approver);

    String updateAclRequest(AclRequests aclRequests, String approver);

    String updateSchemaRequest(SchemaRequest schemaRequest, String approver);

    String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver);

    String declineTopicRequest(TopicRequest topicRequest, String approver);

    String declineAclRequest(AclRequests aclRequests, String approver);

    String updatePassword(String username, String pwd);

    /*--------------------Delete */
    String deleteTopicRequest(String topicName, String env);

    String deleteAclRequest(String req_no);

    String deleteAclSubscriptionRequest(String req_no);

    String deleteClusterRequest(String clusterId);

    String deleteUserRequest(String userId);

    String deleteTeamRequest(String teamId);

    String deleteSchemaRequest(String topicName, String schemaVersion, String env);
}
