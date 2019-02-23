package com.kafkamgt.uiapi.helpers.db.jdbc;

import com.kafkamgt.uiapi.entities.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;
import java.util.Map;

@Configuration
@PropertySource(value= {"classpath:application.properties"})
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

    public void connectToDb() throws Exception {
        LOG.info("Loading tables");

//        Employee emp = new Employee();
//        emp.setId(12778);
//        emp.setName("fds");
//
//        empRepo.save(emp);
        //loadDbJdbc.insertData();
    }

//    @Bean
//    public DataSource datasource() throws PropertyVetoException {
//        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver.class"));
//        dataSource.setUrl(environment.getProperty("spring.datasource.url"));
//        dataSource.setUsername(environment.getProperty("spring.datasource.username"));
//        dataSource.setPassword(environment.getProperty("spring.datasource.password"));
//        return dataSource;
//    }

//    @Bean
//    public void saveEmp(EmployeeRepo empRepo){
//        Employee emp = new Employee();
//        emp.setId(12);
//        emp.setName("fds");
//
//        empRepo.save(emp);
//
//        LOG.info("Saved emp to db.");
//    }

    /*--------------------Insert */

    public String requestForTopic(TopicRequest topicRequest){
        return jdbcInsertHelper.insertIntoRequestTopic(topicRequest);
    }

    public String requestForAcl(AclRequests aclReq){
        return jdbcInsertHelper.insertIntoRequestAcl(aclReq);
    }

    public String addNewUser(com.kafkamgt.uiapi.entities.UserInfo userInfo){
        return jdbcInsertHelper.insertIntoUsers(userInfo);
    }

    public String addNewTeam(com.kafkamgt.uiapi.entities.Team team){
        return jdbcInsertHelper.insertIntoTeams(team);
    }

    public String addNewEnv(Env env){
        return jdbcInsertHelper.insertIntoEnvs(env);
    }

    public String requestForSchema(SchemaRequest schemaRequest){
        return jdbcInsertHelper.insertIntoRequestSchema(schemaRequest);
    }

    public String addToSynctopics(List<TopicRequest> topicRequests) {
        return jdbcInsertHelper.insertIntoTopicSOT(topicRequests);
    }

    public String addToSyncacls(List<AclRequests> acls) {
        return jdbcInsertHelper.insertIntoAclsSOT(acls);
    }

    /*--------------------Select */

    public int getAllRequestsToBeApproved(String requestor){

        return jdbcSelectHelper.getAllRequestsToBeApproved(requestor);
    }

    public List<TopicRequest> getAllTopicRequests(String requestor){
        return jdbcSelectHelper.selectTopicRequests(false, requestor);
    }
    public List<TopicRequest> getCreatedTopicRequests(String requestor){
        return jdbcSelectHelper.selectTopicRequests(true,requestor);
    }

    public TopicRequest selectTopicRequestsForTopic(String topicName) {
        return jdbcSelectHelper.selectTopicRequestsForTopic(topicName);
    }

    public List<TopicRequest> getSyncTopics(String env){
        return jdbcSelectHelper.selectSyncTopics(env);
    }

    public List<AclRequests> getSyncAcls(String env){
        return jdbcSelectHelper.selectSyncAcls(env);
    }

    public List<AclRequests> getAllAclRequests(String requestor){
        return jdbcSelectHelper.selectAclRequests(false,requestor);
    }
    public List<AclRequests> getCreatedAclRequests(String requestor){
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

    public List<com.kafkamgt.uiapi.entities.UserInfo> selectAllUsersInfo(){
        return jdbcSelectHelper.selectAllUsersInfo();
    }

    public com.kafkamgt.uiapi.entities.UserInfo getUsersInfo(String username){
        return jdbcSelectHelper.selectUserInfo(username);
    }
    public List<Map<String,String>> selectAllUsers(){
        return jdbcSelectHelper.selectAllUsers();
    }

    public AclRequests selectAcl(String req_no){
        return jdbcSelectHelper.selectAcl(req_no);
    }

    public TopicRequest getTopicTeam(String topicName, String env){
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

    public String deletePrevAclRecs(List<AclRequests> aclReqs){ return jdbcDeleteHelper.deletePrevAclRecs(aclReqs);}
}
