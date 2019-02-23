package com.kafkamgt.uiapi.helpers;

import com.kafkamgt.uiapi.entities.*;

import java.util.List;
import java.util.Map;



public interface HandleDbRequests {

    public void connectToDb() throws Exception;

    /*--------------------Insert */

    public String requestForTopic(TopicRequest topicRequest);

    public String requestForAcl(AclRequests aclReq);

    public String addNewUser(com.kafkamgt.uiapi.entities.UserInfo userInfo);

    public String addNewTeam(com.kafkamgt.uiapi.entities.Team team);

    public String addNewEnv(Env env);

    public String requestForSchema(SchemaRequest schemaRequest);

    public String addToSynctopics(List<TopicRequest> topicRequests);

    public String addToSyncacls(List<AclRequests> acls);

    /*--------------------Select */

    public int getAllRequestsToBeApproved(String requestor);

    public List<TopicRequest> getAllTopicRequests(String requestor);
    public List<TopicRequest> getCreatedTopicRequests(String requestor);

    public TopicRequest selectTopicRequestsForTopic(String topicName) ;

    public List<TopicRequest> getSyncTopics(String env);

    public List<AclRequests> getSyncAcls(String env);

    public List<AclRequests> getAllAclRequests(String requestor);
    public List<AclRequests> getCreatedAclRequests(String requestor);

    public List<SchemaRequest> getAllSchemaRequests(String requestor);
    public List<SchemaRequest> getCreatedSchemaRequests(String requestor);

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env);

    public List<Team> selectAllTeamsOfUsers(String username);

    public List<Team> selectAllTeams();

    public List<com.kafkamgt.uiapi.entities.UserInfo> selectAllUsersInfo();

    public com.kafkamgt.uiapi.entities.UserInfo getUsersInfo(String username);
    public List<Map<String,String>> selectAllUsers();

    public AclRequests selectAcl(String req_no);

    public TopicRequest getTopicTeam(String topicName, String env);

    public List<PCStream> selectTopicStreams(String envSelected);

    public List<Env> selectAllKafkaEnvs();

    public List<Env> selectAllSchemaRegEnvs();

    public Env selectEnvDetails(String env);

    public List<ActivityLog> selectActivityLog(String user, String env);

    /*--------------------Update */
    public String updateTopicRequest(String topicName, String approver);

    public String updateAclRequest(String req_no, String approver);

    public String updateSchemaRequest(String topicName,String schemaVersion, String env, String approver);

    public String updatePassword(String username, String pwd);

    /*--------------------Delete */
    public String deleteTopicRequest(String topicName);

    public String deleteAclRequest(String req_no);

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env);

    public String deletePrevAclRecs(List<AclRequests> aclReqs);
}
