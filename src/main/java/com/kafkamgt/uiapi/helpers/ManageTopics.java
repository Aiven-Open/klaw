package com.kafkamgt.uiapi.helpers;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.db.DeleteData;
import com.kafkamgt.uiapi.helpers.db.InsertData;
import com.kafkamgt.uiapi.helpers.db.SelectData;
import com.kafkamgt.uiapi.helpers.db.UpdateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class ManageTopics {

    @Autowired
    SelectData cassandraSelectHelper;

    @Autowired
    InsertData cassandraInsertHelper;

    @Autowired
    UpdateData cassandraUpdateHelper;

    @Autowired
    DeleteData cassandraDeleteHelper;

    /*--------------------Insert */

    public String requestForTopic(Topic topic){
        return cassandraInsertHelper.insertIntoRequestTopic(topic);
    }

    public String requestForAcl(AclReq aclReq){
        return cassandraInsertHelper.insertIntoRequestAcl(aclReq);
    }

    public String addNewUser(UserInfo userInfo){
        return cassandraInsertHelper.insertIntoUsers(userInfo);
    }

    public String addNewTeam(Team team){
        return cassandraInsertHelper.insertIntoTeams(team);
    }

    public String addNewEnv(Env env){
        return cassandraInsertHelper.insertIntoEnvs(env);
    }

    public String requestForSchema(SchemaRequest schemaRequest){
        return cassandraInsertHelper.insertIntoRequestSchema(schemaRequest);
    }

    public String addToSynctopics(List<Topic> topics) {
        return cassandraInsertHelper.insertIntoTopicSOT(topics);
    }

    public String addToSyncacls(List<AclReq> acls) {
        return cassandraInsertHelper.insertIntoAclsSOT(acls);
    }

    /*--------------------Select */

    public int getAllRequestsToBeApproved(String requestor){
        return cassandraSelectHelper.getAllRequestsToBeApproved(requestor);
    }

    public List<Topic> getAllTopicRequests(String requestor){
        return cassandraSelectHelper.selectTopicRequests(false, requestor);
    }
    public List<Topic> getCreatedTopicRequests(String requestor){
        return cassandraSelectHelper.selectTopicRequests(true,requestor);
    }

    public Topic selectTopicRequestsForTopic(String topicName) {
        return cassandraSelectHelper.selectTopicRequestsForTopic(topicName);
    }

    public List<Topic> getSyncTopics(String env){
        return cassandraSelectHelper.selectSyncTopics(env);
    }

    public List<AclReq> getSyncAcls(String env){
        return cassandraSelectHelper.selectSyncAcls(env);
    }

    public List<AclReq> getAllAclRequests(String requestor){
        return cassandraSelectHelper.selectAclRequests(false,requestor);
    }
    public List<AclReq> getCreatedAclRequests(String requestor){
        return cassandraSelectHelper.selectAclRequests(true,requestor);
    }

    public List<SchemaRequest> getAllSchemaRequests(String requestor){
        return cassandraSelectHelper.selectSchemaRequests(false,requestor);
    }
    public List<SchemaRequest> getCreatedSchemaRequests(String requestor){
        return cassandraSelectHelper.selectSchemaRequests(true,requestor);
    }

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env){
        return cassandraSelectHelper.selectSchemaRequest(topicName,schemaVersion, env);
    }

    public List<Team> selectAllTeamsOfUsers(String username){
        return cassandraSelectHelper.selectTeamsOfUsers(username);
    }

    public List<Team> selectAllTeams(){
        return cassandraSelectHelper.selectAllTeams();
    }

    public List<UserInfo> selectAllUsersInfo(){
        return cassandraSelectHelper.selectAllUsersInfo();
    }

    public UserInfo getUsersInfo(String username){
        return cassandraSelectHelper.selectUserInfo(username);
    }
    public List<Map<String,String>> selectAllUsers(){
        return cassandraSelectHelper.selectAllUsers();
    }

    public AclReq selectAcl(String req_no){
        return cassandraSelectHelper.selectAcl(req_no);
    }

    public Topic getTopicTeam(String topicName, String env){
        return cassandraSelectHelper.selectTopicDetails(topicName, env);
    }

    public List<PCStream> selectTopicStreams(String envSelected){
        return cassandraSelectHelper.selectTopicStreams(envSelected);
    }

    public List<Env> selectAllKafkaEnvs(){
        return cassandraSelectHelper.selectAllEnvs("kafka");
    }

    public List<Env> selectAllSchemaRegEnvs(){
        return cassandraSelectHelper.selectAllEnvs("schemaregistry");
    }

    public Env selectEnvDetails(String env){return cassandraSelectHelper.selectEnvDetails(env);}

    public List<ActivityLog> selectActivityLog(String user, String env){return cassandraSelectHelper.selectActivityLog(user, env);}

    /*--------------------Update */
    public String updateTopicRequest(String topicName, String approver){
        return cassandraUpdateHelper.updateTopicRequest(topicName, approver);
    }

    public String updateAclRequest(String req_no, String approver){
        return cassandraUpdateHelper.updateAclRequest(req_no, approver);
    }

    public String updateSchemaRequest(String topicName,String schemaVersion, String env, String approver){
        return cassandraUpdateHelper.updateSchemaRequest(topicName, schemaVersion, env,  approver);
    }

    public String updatePassword(String username, String pwd){
        return cassandraUpdateHelper.updatePassword(username,pwd);
    }

    /*--------------------Delete */
    public String deleteTopicRequest(String topicName){
        return cassandraDeleteHelper.deleteTopicRequest(topicName);
    }

    public String deleteAclRequest(String req_no){
        return cassandraDeleteHelper.deleteAclRequest(req_no);
    }

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env){
        return cassandraDeleteHelper.deleteSchemaRequest(topicName,schemaVersion, env);
    }

    public String deletePrevAclRecs(List<AclReq> aclReqs){ return cassandraDeleteHelper.deletePrevAclRecs(aclReqs);}
}
