package com.kafkamgt.uiapi.helpers.db.jdbc;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;


@Component
public class HandleDbRequestsJdbc implements HandleDbRequests {

    private static Logger LOG = LoggerFactory.getLogger(HandleDbRequestsJdbc.class);

    @Autowired
    SelectDataJdbc jdbcSelectHelper;

    @Autowired
    InsertDataJdbc jdbcInsertHelper;

    @Autowired
    UpdateDataJdbc jdbcUpdateHelper;

    @Autowired
    DeleteDataJdbc jdbcDeleteHelper;

    @Autowired
    LoadDbJdbc loadDbJdbc;

    @Value("${jdbc.host:#{null}}")
    String jdbcConnHost;

    @Value("${jdbc.port:#{0}}")
    int jdbcConnPort;

    @Value("${jdbc.username:#{null}}")
    String jdbcUsername;

    @Value("${jdbc.pwd:#{null}}")
    String jdbcConnPwd;

//    @PostConstruct
    public void connectToDb() throws Exception {
        LOG.info("Establishing Connection to JDBC.");
        System.exit(0);
        throw new Exception("JDBC - Database not configured.");
    }

    /*--------------------Insert */

    public String requestForTopic(Topic topic){
        return jdbcInsertHelper.insertIntoRequestTopic(topic);
    }

    public String requestForAcl(AclReq aclReq){
        return jdbcInsertHelper.insertIntoRequestAcl(aclReq);
    }

    public String addNewUser(UserInfo userInfo){
        return jdbcInsertHelper.insertIntoUsers(userInfo);
    }

    public String addNewTeam(Team team){
        return jdbcInsertHelper.insertIntoTeams(team);
    }

    public String addNewEnv(Env env){
        return jdbcInsertHelper.insertIntoEnvs(env);
    }

    public String requestForSchema(SchemaRequest schemaRequest){
        return jdbcInsertHelper.insertIntoRequestSchema(schemaRequest);
    }

    public String addToSynctopics(List<Topic> topics) {
        return jdbcInsertHelper.insertIntoTopicSOT(topics);
    }

    public String addToSyncacls(List<AclReq> acls) {
        return jdbcInsertHelper.insertIntoAclsSOT(acls);
    }

    /*--------------------Select */

    public int getAllRequestsToBeApproved(String requestor){

        return jdbcSelectHelper.getAllRequestsToBeApproved(requestor);
    }

    public List<Topic> getAllTopicRequests(String requestor){
        return jdbcSelectHelper.selectTopicRequests(false, requestor);
    }
    public List<Topic> getCreatedTopicRequests(String requestor){
        return jdbcSelectHelper.selectTopicRequests(true,requestor);
    }

    public Topic selectTopicRequestsForTopic(String topicName) {
        return jdbcSelectHelper.selectTopicRequestsForTopic(topicName);
    }

    public List<Topic> getSyncTopics(String env){
        return jdbcSelectHelper.selectSyncTopics(env);
    }

    public List<AclReq> getSyncAcls(String env){
        return jdbcSelectHelper.selectSyncAcls(env);
    }

    public List<AclReq> getAllAclRequests(String requestor){
        return jdbcSelectHelper.selectAclRequests(false,requestor);
    }
    public List<AclReq> getCreatedAclRequests(String requestor){
        return jdbcSelectHelper.selectAclRequests(true,requestor);
    }

    public List<SchemaRequest> getAllSchemaRequests(String requestor){
        return jdbcSelectHelper.selectSchemaRequests(false,requestor);
    }
    public List<SchemaRequest> getCreatedSchemaRequests(String requestor){
        return jdbcSelectHelper.selectSchemaRequests(true,requestor);
    }

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env){
        return jdbcSelectHelper.selectSchemaRequest(topicName,schemaVersion, env);
    }

    public List<Team> selectAllTeamsOfUsers(String username){
        return jdbcSelectHelper.selectTeamsOfUsers(username);
    }

    public List<Team> selectAllTeams(){
        return jdbcSelectHelper.selectAllTeams();
    }

    public List<UserInfo> selectAllUsersInfo(){
        return jdbcSelectHelper.selectAllUsersInfo();
    }

    public UserInfo getUsersInfo(String username){
        return jdbcSelectHelper.selectUserInfo(username);
    }
    public List<Map<String,String>> selectAllUsers(){
        return jdbcSelectHelper.selectAllUsers();
    }

    public AclReq selectAcl(String req_no){
        return jdbcSelectHelper.selectAcl(req_no);
    }

    public Topic getTopicTeam(String topicName, String env){
        return jdbcSelectHelper.selectTopicDetails(topicName, env);
    }

    public List<PCStream> selectTopicStreams(String envSelected){
        return jdbcSelectHelper.selectTopicStreams(envSelected);
    }

    public List<Env> selectAllKafkaEnvs(){
        return jdbcSelectHelper.selectAllEnvs("kafka");
    }

    public List<Env> selectAllSchemaRegEnvs(){
        return jdbcSelectHelper.selectAllEnvs("schemaregistry");
    }

    public Env selectEnvDetails(String env){return jdbcSelectHelper.selectEnvDetails(env);}

    public List<ActivityLog> selectActivityLog(String user, String env){return jdbcSelectHelper.selectActivityLog(user, env);}

    /*--------------------Update */
    public String updateTopicRequest(String topicName, String approver){
        return jdbcUpdateHelper.updateTopicRequest(topicName, approver);
    }

    public String updateAclRequest(String req_no, String approver){
        return jdbcUpdateHelper.updateAclRequest(req_no, approver);
    }

    public String updateSchemaRequest(String topicName,String schemaVersion, String env, String approver){
        return jdbcUpdateHelper.updateSchemaRequest(topicName, schemaVersion, env,  approver);
    }

    public String updatePassword(String username, String pwd){
        return jdbcUpdateHelper.updatePassword(username,pwd);
    }

    /*--------------------Delete */
    public String deleteTopicRequest(String topicName){
        return jdbcDeleteHelper.deleteTopicRequest(topicName);
    }

    public String deleteAclRequest(String req_no){
        return jdbcDeleteHelper.deleteAclRequest(req_no);
    }

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env){
        return jdbcDeleteHelper.deleteSchemaRequest(topicName,schemaVersion, env);
    }

    public String deletePrevAclRecs(List<AclReq> aclReqs){ return jdbcDeleteHelper.deletePrevAclRecs(aclReqs);}
}
