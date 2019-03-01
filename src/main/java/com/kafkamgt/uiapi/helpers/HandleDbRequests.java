package com.kafkamgt.uiapi.helpers;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.dao.UserInfo;

import java.util.List;


public interface HandleDbRequests {

    public void connectToDb() throws Exception;

    /*--------------------Insert */

    public String requestForTopic(TopicRequest topicRequest);

    public String requestForAcl(AclRequests aclReq);

    public String addNewUser(UserInfo userInfo);

    public String addNewTeam(Team team);

    public String addNewEnv(Env env);

    public String requestForSchema(SchemaRequest schemaRequest);

    public String addToSynctopics(List<Topic> topicRequests);

    public String addToSyncacls(List<Acl> acls);

    /*--------------------Select */

    public int getAllRequestsToBeApproved(String requestor);

    public List<TopicRequest> getAllTopicRequests(String requestor);
    public List<TopicRequest> getCreatedTopicRequests(String requestor);

    public TopicRequest selectTopicRequestsForTopic(String topicName, String env) ;

    public List<Topic> getSyncTopics(String env);

    public List<Acl> getSyncAcls(String env);

    public List<AclRequests> getAllAclRequests(String requestor);
    public List<AclRequests> getCreatedAclRequests(String requestor);

    public List<SchemaRequest> getAllSchemaRequests(String requestor);
    public List<SchemaRequest> getCreatedSchemaRequests(String requestor);

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env);

    public List<Team> selectAllTeamsOfUsers(String username);

    public List<Team> selectAllTeams();

    public List<UserInfo> selectAllUsersInfo();

    public UserInfo getUsersInfo(String username);

    public AclRequests selectAcl(String req_no);

    public Topic getTopicTeam(String topicName, String env);

    public List<PCStream> selectTopicStreams(String envSelected);

    public List<Env> selectAllKafkaEnvs();

    public List<Env> selectAllSchemaRegEnvs();

    public Env selectEnvDetails(String env);

    public List<ActivityLog> selectActivityLog(String user, String env);

    /*--------------------Update */
    public String updateTopicRequest(TopicRequest topicRequest, String approver);

    public String updateAclRequest(AclRequests aclRequests, String approver);

    public String updateSchemaRequest(SchemaRequest schemaRequest, String approver);

    public String updatePassword(String username, String pwd);

    /*--------------------Delete */
    public String deleteTopicRequest(String topicName, String env);

    public String deleteAclRequest(String req_no);

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env);

    public String deletePrevAclRecs(List<Acl> aclReqs);
}
