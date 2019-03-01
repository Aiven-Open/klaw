package com.kafkamgt.uiapi.helpers;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.helpers.db.cassandra.CassandraDataSourceCondition;
import com.kafkamgt.uiapi.helpers.db.cassandra.HandleDbRequestsCassandra;
import com.kafkamgt.uiapi.helpers.db.rdbms.HandleDbRequestsJdbc;
import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.model.UserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class ManageTopics {

    @Value("${db.storetype}")
    String dbStore;

    HandleDbRequests handleDbRequests;

    @PostConstruct
    public void loadDb() throws Exception {
        if(dbStore !=null && dbStore.equals("rdbms")){
            handleDbRequests = handleJdbc();
        }else
            handleDbRequests = handleCassandra();

        handleDbRequests.connectToDb();
    }

    @Bean()
    @Conditional(JdbcDataSourceCondition.class)
    HandleDbRequestsJdbc handleJdbc() {
        return new HandleDbRequestsJdbc();
    }

    @Bean()
    @Conditional(CassandraDataSourceCondition.class)
    HandleDbRequestsCassandra handleCassandra() {
        return new HandleDbRequestsCassandra();
    }


    /*--------------------Insert */

    public String requestForTopic(TopicRequest topicRequest){
        return handleDbRequests.requestForTopic(topicRequest);
    }

    public String requestForAcl(AclRequests aclReq){
        return handleDbRequests.requestForAcl(aclReq);
    }

    public String addNewUser(UserInfo userInfo){
        return handleDbRequests.addNewUser(userInfo);
    }

    public String addNewTeam(Team team){
        return handleDbRequests.addNewTeam(team);
    }

    public String addNewEnv(Env env){
        return handleDbRequests.addNewEnv(env);
    }

    public String requestForSchema(SchemaRequest schemaRequest){
        return handleDbRequests.requestForSchema(schemaRequest);
    }

    public String addToSynctopics(List<Topic> topicRequests) {
        return handleDbRequests.addToSynctopics(topicRequests);
    }

    public String addToSyncacls(List<Acl> acls) {
        return handleDbRequests.addToSyncacls(acls);
    }

    /*--------------------Select */

    public int getAllRequestsToBeApproved(String requestor){

        return handleDbRequests.getAllRequestsToBeApproved(requestor);
    }

    public List<TopicRequest> getAllTopicRequests(String requestor){
        return handleDbRequests.getAllTopicRequests(requestor);
    }
    public List<TopicRequest> getCreatedTopicRequests(String requestor){
        return handleDbRequests.getCreatedTopicRequests(requestor);
    }

    public TopicRequest selectTopicRequestsForTopic(String topicName, String env) {
        return handleDbRequests.selectTopicRequestsForTopic(topicName, env);
    }

    public List<Topic> getSyncTopics(String env){
        return handleDbRequests.getSyncTopics(env);
    }

    public List<Acl> getSyncAcls(String env){
        return handleDbRequests.getSyncAcls(env);
    }

    public List<AclRequests> getAllAclRequests(String requestor){
        return handleDbRequests.getAllAclRequests(requestor);
    }
    public List<AclRequests> getCreatedAclRequests(String requestor){
        return handleDbRequests.getCreatedAclRequests(requestor);
    }

    public List<SchemaRequest> getAllSchemaRequests(String requestor){
        return handleDbRequests.getAllSchemaRequests(requestor);
    }
    public List<SchemaRequest> getCreatedSchemaRequests(String requestor){
        return handleDbRequests.getCreatedSchemaRequests(requestor);
    }

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env){
        return handleDbRequests.selectSchemaRequest(topicName,schemaVersion, env);
    }

    public List<Team> selectAllTeamsOfUsers(String username){
        return handleDbRequests.selectAllTeamsOfUsers(username);
    }

    public List<Team> selectAllTeams(){
        return handleDbRequests.selectAllTeams();
    }

    public List<UserInfo> selectAllUsersInfo(){
        return handleDbRequests.selectAllUsersInfo();
    }

    public UserInfo getUsersInfo(String username){
        return handleDbRequests.getUsersInfo(username);
    }

    public AclRequests selectAcl(String req_no){
        return handleDbRequests.selectAcl(req_no);
    }

    public Topic getTopicTeam(String topicName, String env){
        return handleDbRequests.getTopicTeam(topicName, env);
    }

    public List<PCStream> selectTopicStreams(String envSelected){
        return handleDbRequests.selectTopicStreams(envSelected);
    }

    public List<Env> selectAllKafkaEnvs(){
        return handleDbRequests.selectAllKafkaEnvs();
    }

    public List<Env> selectAllSchemaRegEnvs(){
        return handleDbRequests.selectAllSchemaRegEnvs();
    }

    public Env selectEnvDetails(String env){return handleDbRequests.selectEnvDetails(env);}

    public List<ActivityLog> selectActivityLog(String user, String env){return handleDbRequests.selectActivityLog(user, env);}

    /*--------------------Update */
    public String updateTopicRequest(TopicRequest topicRequest, String approver){
        return handleDbRequests.updateTopicRequest(topicRequest, approver);
    }

    public String updateAclRequest(AclRequests aclRequests, String approver){
        return handleDbRequests.updateAclRequest(aclRequests, approver);
    }

    public String updateSchemaRequest(String topicName,String schemaVersion, String env, String approver){
        return handleDbRequests.updateSchemaRequest(topicName, schemaVersion, env,  approver);
    }

    public String updatePassword(String username, String pwd){
        return handleDbRequests.updatePassword(username,pwd);
    }

    /*--------------------Delete */
    public String deleteTopicRequest(String topicName, String env){
        return handleDbRequests.deleteTopicRequest(topicName, env);
    }

    public String deleteAclRequest(String req_no){
        return handleDbRequests.deleteAclRequest(req_no);
    }

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env){
        return handleDbRequests.deleteSchemaRequest(topicName,schemaVersion, env);
    }

    public String deletePrevAclRecs(List<Acl> aclReqs){ return handleDbRequests.deletePrevAclRecs(aclReqs);}
}
