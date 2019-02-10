package com.kafkamgt.uiapi.helpers;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.db.cassandra.DeleteData;
import com.kafkamgt.uiapi.helpers.db.cassandra.InsertData;
import com.kafkamgt.uiapi.helpers.db.cassandra.UpdateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;



public interface HandleDbRequests {

    public void connectToDb() throws Exception;

    /*--------------------Insert */

    public String requestForTopic(Topic topic);

    public String requestForAcl(AclReq aclReq);

    public String addNewUser(UserInfo userInfo);

    public String addNewTeam(Team team);

    public String addNewEnv(Env env);

    public String requestForSchema(SchemaRequest schemaRequest);

    public String addToSynctopics(List<Topic> topics);

    public String addToSyncacls(List<AclReq> acls);

    /*--------------------Select */

    public int getAllRequestsToBeApproved(String requestor);

    public List<Topic> getAllTopicRequests(String requestor);
    public List<Topic> getCreatedTopicRequests(String requestor);

    public Topic selectTopicRequestsForTopic(String topicName) ;

    public List<Topic> getSyncTopics(String env);

    public List<AclReq> getSyncAcls(String env);

    public List<AclReq> getAllAclRequests(String requestor);
    public List<AclReq> getCreatedAclRequests(String requestor);

    public List<SchemaRequest> getAllSchemaRequests(String requestor);
    public List<SchemaRequest> getCreatedSchemaRequests(String requestor);

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env);

    public List<Team> selectAllTeamsOfUsers(String username);

    public List<Team> selectAllTeams();

    public List<UserInfo> selectAllUsersInfo();

    public UserInfo getUsersInfo(String username);
    public List<Map<String,String>> selectAllUsers();

    public AclReq selectAcl(String req_no);

    public Topic getTopicTeam(String topicName, String env);

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

    public String deletePrevAclRecs(List<AclReq> aclReqs);
}
