package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.service.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class HandleDbRequestsCassandra implements HandleDbRequests {

    private static Logger LOG = LoggerFactory.getLogger(HandleDbRequestsCassandra.class);

    @Autowired(required=false)
    SelectData cassandraSelectHelper;

    @Autowired(required=false)
    InsertData cassandraInsertHelper;

    @Autowired(required=false)
    UpdateData cassandraUpdateHelper;

    @Autowired(required=false)
    DeleteData cassandraDeleteHelper;

    @Autowired
    UtilService utilService;

    @Autowired(required=false)
    LoadDb loadDb;

    Cluster cluster;
    Session session;

    @Value("${custom.cassandradb.url:@null}")
    String clusterConnHost;

    @Value("${custom.cassandradb.port:9042}")
    int clusterConnPort;

    @Value("${custom.cassandradb.keyspace:@null}")
    String keyspace;

    @Value("${custom.dbscripts.execution:auto}")
    String dbScriptsExecution;

    @Value("${custom.dbscripts.dropall_recreate:false}")
    String dbScriptsDropAllRecreate;

    public HandleDbRequestsCassandra(){

    }

    public HandleDbRequestsCassandra(SelectData cassandraSelectHelper, InsertData cassandraInsertHelper,
                                     UpdateData cassandraUpdateHelper, DeleteData cassandraDeleteHelper,
                                     LoadDb loadDb, Cluster cluster, UtilService utilService){
        this.cassandraSelectHelper = cassandraSelectHelper;
        this.cassandraInsertHelper = cassandraInsertHelper;
        this.cassandraDeleteHelper = cassandraDeleteHelper;
        this.cassandraUpdateHelper = cassandraUpdateHelper;
        this.loadDb = loadDb;
        this.cluster = cluster;
        this.utilService = utilService;

    }

    public void connectToDb(){
        LOG.info("Establishing Connection to Cassandra.");
        CodecRegistry myCodecRegistry;
        myCodecRegistry = CodecRegistry.DEFAULT_INSTANCE;
        myCodecRegistry.register(InstantCodec.instance);

        try {
            cluster = utilService.getCluster(clusterConnHost, clusterConnPort, myCodecRegistry);

            session = cluster.connect();

            if(dbScriptsExecution.equals("auto")){
                loadDb.session = session;
                if(dbScriptsDropAllRecreate.equals("true"))
                    loadDb.dropTables();
                loadDb.createTables();
                loadDb.insertData();
            }

            session = cluster.connect(keyspace);

            Session.State sessionState = session.getState();

            if(sessionState.getConnectedHosts().size() > 0 ){
                cassandraSelectHelper.session = session;
                cassandraInsertHelper.session = session;
                cassandraUpdateHelper.session = session;
                cassandraDeleteHelper.session = session;
                cassandraInsertHelper.initializeBoundStatements();

                cassandraInsertHelper.cassandraSelectHelper = cassandraSelectHelper;
            }
            else{
                LOG.error("Could not connect to Cassandra "+clusterConnHost+":"+clusterConnPort);
                System.exit(0);
            }
        }catch (Exception e){
            LOG.error("Could not connect to Cassandra "+clusterConnHost+":"+clusterConnPort + " Error : " + e.getMessage());
            System.exit(0);
        }
    }

    /*--------------------Insert */

    public String requestForTopic(TopicRequest topicRequest){
        return cassandraInsertHelper.insertIntoRequestTopic(topicRequest);
    }

    public String requestForAcl(AclRequests aclReq){
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

    public String addToSynctopics(List<Topic> topicRequests) {
        return cassandraInsertHelper.insertIntoTopicSOT(topicRequests,true);
    }

    public String addToSyncacls(List<Acl> acls) {
        return cassandraInsertHelper.insertIntoAclsSOT(acls,true);
    }

    /*--------------------Select */

    public HashMap<String, String> getAllRequestsToBeApproved(String requestor){

        return cassandraSelectHelper.getAllRequestsToBeApproved(requestor);
    }

    public List<TopicRequest> getAllTopicRequests(String requestor){
        return cassandraSelectHelper.selectTopicRequests(false, requestor);
    }
    public List<TopicRequest> getCreatedTopicRequests(String requestor){
        return cassandraSelectHelper.selectTopicRequests(true,requestor);
    }

    public TopicRequest selectTopicRequestsForTopic(String topicName, String env) {
        return cassandraSelectHelper.selectTopicRequestsForTopic(topicName, env);
    }

    public List<Topic> getSyncTopics(String env){
        return cassandraSelectHelper.selectSyncTopics(env);
    }

    public List<Acl> getSyncAcls(String env){
        return cassandraSelectHelper.selectSyncAcls(env);
    }

    public List<AclRequests> getAllAclRequests(String requestor){
        return cassandraSelectHelper.selectAclRequests(false,requestor);
    }
    public List<AclRequests> getCreatedAclRequests(String requestor){
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

    public AclRequests selectAcl(String req_no){
        return cassandraSelectHelper.selectAcl(req_no);
    }

    public Topic getTopicTeam(String topicName, String env){
        return cassandraSelectHelper.selectTopicDetails(topicName, env);
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
    public String updateTopicRequest(TopicRequest topicRequest, String approver){
        return cassandraUpdateHelper.updateTopicRequest(topicRequest, approver);
    }

    public String declineTopicRequest(TopicRequest topicRequest, String approver){
        return cassandraUpdateHelper.declineTopicRequest(topicRequest, approver);
    }

    public String updateAclRequest(AclRequests aclReq, String approver){
        return cassandraUpdateHelper.updateAclRequest(aclReq, approver);
    }

    public String declineAclRequest(AclRequests aclReq, String approver){
        return cassandraUpdateHelper.declineAclRequest(aclReq, approver);
    }

    public String updateSchemaRequest(SchemaRequest schemaRequest, String approver){
        return cassandraUpdateHelper.updateSchemaRequest(schemaRequest,  approver);
    }

    public String updatePassword(String username, String pwd){
        return cassandraUpdateHelper.updatePassword(username,pwd);
    }

    /*--------------------Delete */
    public String deleteTopicRequest(String topicName, String env){
        return cassandraDeleteHelper.deleteTopicRequest(topicName, env);
    }

    public String deleteAclRequest(String req_no){
        return cassandraDeleteHelper.deleteAclRequest(req_no);
    }

    public String deleteClusterRequest(String clusterId){
        return cassandraDeleteHelper.deleteClusterRequest(clusterId);
    }

    @Override
    public String deleteUserRequest(String userId) {
        return cassandraDeleteHelper.deleteUserRequest(userId);
    }

    @Override
    public String deleteTeamRequest(String teamId) {
        return cassandraDeleteHelper.deleteTeamRequest(teamId);
    }

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env){
        return cassandraDeleteHelper.deleteSchemaRequest(topicName,schemaVersion, env);
    }

    public String deletePrevAclRecs(List<Acl> aclReqs){ return cassandraDeleteHelper.deletePrevAclRecs(aclReqs);}
}
