package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.service.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Random;

@Component
public class InsertData {

    private static Logger LOG = LoggerFactory.getLogger(InsertData.class);

    @Autowired
    private UtilService utilService;

    Session session;

    private static BoundStatement boundStatementInsertIntoRequestTopic, boundStatementInsertIntoTopicSOT,
            boundStatementInsertIntoAclsSOT, boundStatementAclRequest,
            boundStatementInsertAclsSOT, boundStatementTopicsSOT, boundStatementInsertIntoActivityLogAcl,
            boundStatementSchemaReqs, boundStatementSchemas, boundStatementUsers, boundStatementTeams,
            boundStatementEnvs;

    @Value("${custom.cassandradb.keyspace:@null}")
    private String keyspace;

    SelectData cassandraSelectHelper;

    public InsertData(){
    }

    public InsertData(Session session, SelectData cassandraSelectHelper, UtilService utilService){
        this.session = session;
        this.cassandraSelectHelper = cassandraSelectHelper;
        this.utilService = utilService;
    }

    private String getRandom(){
        int length = 8;
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return builder.toString();
    }

    public void initializeBoundStatements(){
        getInsertIntoRequestTopicBoundStatement();
        getBoundStatementInsertIntoTopicSOT();
        getBoundStatementInsertIntoAclsSOT();
        getBoundStatementAclRequest();
        getBoundStatementAclsSOT();
        getBoundStatementActivityLogAcl();
        getBoundStatementTopicsSOT();
        getBoundStatementSchemaReqs();
        getBoundStatementSchemas();
        getBoundStatementTeams();
        getBoundStatementUsers();
        getBoundStatementEnvs();
    }

    private BoundStatement getBoundStatement(String query){
        return utilService.getBoundStatement(session, query);
    }

    private void getInsertIntoRequestTopicBoundStatement(){
        String tableName = "topic_requests";
        String insertStat = "INSERT INTO " + keyspace + "."+tableName+"(topicname, partitions, replicationfactor, env," +
                "teamname,appname,topictype,requestor," +
                "requesttime,  remarks, topicstatus) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?);";
        boundStatementInsertIntoRequestTopic  = getBoundStatement(insertStat);
    }

    private void getBoundStatementInsertIntoTopicSOT() {
        String tableName = "topics";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,env,teamname,appname)" +
                "VALUES (?,?,?,?);";
        boundStatementInsertIntoTopicSOT = getBoundStatement(insertstat);
    }

    private void getBoundStatementTopicsSOT() {
        String tableName1 = "topics";

        String insertstat = "INSERT INTO " + keyspace + "."+tableName1+"(topicname,env,teamname)" +
                "VALUES (?,?,?);";
        boundStatementTopicsSOT =  getBoundStatement(insertstat);
    }

    private void getBoundStatementInsertIntoAclsSOT(){
        String tableName = "acls";
        String insertstat = "INSERT INTO " + keyspace + "." + tableName + "(req_no, topicname, env, teamname, topictype," +
                " acl_ip, acl_ssl)" +
                "VALUES (?,?,?,?,?,?,?);";
        boundStatementInsertIntoAclsSOT = getBoundStatement(insertstat);
    }

    private void getBoundStatementAclsSOT() {
        String tableName = "acls";

        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, topicname, env, teamname, consumergroup," +
                " topictype, acl_ip, acl_ssl)" + "VALUES (?,?,?,?,?,?,?,?);";
        boundStatementInsertAclsSOT = getBoundStatement(insertstat);
    }

    private void getBoundStatementAclRequest(){
        String tableName = "acl_requests";

        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no,topicname,env,teamname,appname," +
                "topictype,requestor," +
                "requesttime, acl_ip, acl_ssl, remarks, topicstatus,consumergroup,requestingteam) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        boundStatementAclRequest = getBoundStatement(insertstat);
    }

    private void getBoundStatementActivityLogAcl() {
        String tableName = "activitylog";

        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, activityname, activitytype, activitytime," +
                " details, user, env, team)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        boundStatementInsertIntoActivityLogAcl = getBoundStatement(insertstat);
    }

    private void getBoundStatementSchemaReqs() {
        String tableName = "schema_requests";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname, env, teamname, appname, requestor," +
                "requesttime, schemafull, remarks, topicstatus, versionschema) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?);";
        boundStatementSchemaReqs = getBoundStatement(insertstat);
    }

    private void getBoundStatementSchemas() {
        String tableName = "avroschemas";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname, env, teamname, " +
                " schemafull, versionschema) " +
                "VALUES (?,?,?,?,?);";
        boundStatementSchemas = getBoundStatement(insertstat);
    }

    private void getBoundStatementUsers() {
        String tableName = "users";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(fullname, userid, pwd, team, roleid) " +
                "VALUES (?,?,?,?,?);";
        boundStatementUsers = getBoundStatement(insertstat);
    }

    private void getBoundStatementTeams() {
        String tableName = "teams";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(team, teammail, app, teamphone," +
                " contactperson) " +
                "VALUES (?,?,?,?,?);";
        boundStatementTeams = getBoundStatement(insertstat);
    }

    private void getBoundStatementEnvs() {
        String tableName = "env";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(name, host, port, protocol, type," +
                " keystorelocation, truststorelocation, keystorepwd, keypwd, truststorepwd, other_params ) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?);";
        boundStatementEnvs = getBoundStatement(insertstat);
    }

    public String insertIntoRequestTopic(TopicRequest topicRequest){

        String  topicReqType;

        topicReqType = "Producer";
        session.execute(boundStatementInsertIntoRequestTopic.bind(topicRequest.getTopicname(), topicRequest.getTopicpartitions(),
                topicRequest.getReplicationfactor(), topicRequest.getEnvironment(), topicRequest.getTeamname(),
                topicRequest.getAppname(),
                topicReqType, topicRequest.getUsername(), new Date(), topicRequest.getRemarks(), "created"));

        // Activity log
        if(insertIntoActivityLogTopic(topicRequest).equals("success"))
            return "success";
        else
            return "failure";
    }

    public String insertIntoTopicSOT(List<Topic> topicRequests, boolean isSyncTopics){

        TopicRequest topicRequest ;

        for(Topic topic:topicRequests) {
            session.execute(boundStatementInsertIntoTopicSOT.bind(topic.getTopicname(), topic.getEnvironment(),
                    topic.getTeamname(), topic.getAppname()));

            if(isSyncTopics) {
                topicRequest = cassandraSelectHelper.selectTopicRequestsForTopic(topic.getTopicname(),topic.getEnvironment());
                if(topicRequest!=null)
                    session.execute(boundStatementInsertIntoAclsSOT.bind(getRandom(), topic.getTopicname(),
                            topic.getEnvironment(), topic.getTeamname(),
                        "Producer", topicRequest.getAcl_ip(), topicRequest.getAcl_ssl()));
                else
                    session.execute(boundStatementInsertIntoAclsSOT.bind(getRandom(), topic.getTopicname(),
                            topic.getEnvironment(), topic.getTeamname(),
                            "Producer", null, null));
            }
        }

        return "success";
    }

    public String insertIntoActivityLogTopic(TopicRequest topicRequest){

        UserInfo userInfo = cassandraSelectHelper.selectUserInfo(topicRequest.getUsername());

        session.execute(boundStatementInsertIntoActivityLogAcl.bind(getRandom(), "topicRequest",
                    "new", new Date(),""+ topicRequest.getTopicname(),""+ topicRequest.getUsername(),
                topicRequest.getEnvironment(), userInfo.getTeam()));

        return "success";
    }

    public String insertIntoRequestAcl(AclRequests aclReq){

        String topicReqType = aclReq.getTopictype();
        session.execute(boundStatementAclRequest.bind(getRandom(),aclReq.getTopicname(),aclReq.getEnvironment(),
                aclReq.getTeamname(), aclReq.getAppname(),
                topicReqType, aclReq.getUsername(), new Date(),aclReq.getAcl_ip(),aclReq.getAcl_ssl(),
                aclReq.getRemarks(), "created",
                aclReq.getConsumergroup(), cassandraSelectHelper.selectTeamsOfUsers(aclReq.getUsername()).get(0).getTeamname()));

        // Insert into acl
        if(insertIntoActivityLogAcl(aclReq).equals("success"))
            return "success";
        else
            return "failure";
    }

    public String insertIntoAclsSOT(List<Acl> acls, boolean isSyncAcls){

        acls.forEach(aclReq-> {
            session.execute(boundStatementInsertAclsSOT.bind(getRandom(),aclReq.getTopicname(),
                    aclReq.getEnvironment(), aclReq.getTeamname(),
                    aclReq.getConsumergroup(), aclReq.getTopictype(),
                    aclReq.getAclip(),aclReq.getAclssl()));

            if(isSyncAcls && aclReq.getTopictype() !=null && aclReq.getTopictype().equals("Producer")){
                session.execute(boundStatementTopicsSOT.bind(aclReq.getTopicname(), aclReq.getEnvironment(),
                        aclReq.getTeamname()));
            }
        });

        return "success";
    }

    private String insertIntoActivityLogAcl(AclRequests aclReq){

        UserInfo userInfo = cassandraSelectHelper.selectUserInfo(aclReq.getUsername());

        session.execute(boundStatementInsertIntoActivityLogAcl.bind(getRandom(), "acl",
                "new", new Date(),aclReq.getAcl_ip()+"-"+aclReq.getTopicname()+"-"+aclReq.getAcl_ssl()+"-"+
                        aclReq.getConsumergroup()+"-"+aclReq.getTopictype()
                ,""+aclReq.getUsername(), aclReq.getEnvironment(), userInfo.getTeam()));

        return "success";
    }

    public String insertIntoRequestSchema(SchemaRequest schemaRequest){
        session.execute(boundStatementSchemaReqs.bind(schemaRequest.getTopicname(), schemaRequest.getEnvironment(),
                schemaRequest.getTeamname(), schemaRequest.getAppname(),
                schemaRequest.getUsername(), new Date(), schemaRequest.getSchemafull(), schemaRequest.getRemarks(), "created",
                schemaRequest.getSchemaversion()));
        return "success";
    }

    public String insertIntoMessageSchemaSOT(List<MessageSchema> messageSchemas){
        messageSchemas.forEach(messageSchema ->
            session.execute(boundStatementSchemas.bind(messageSchema.getTopicname(),messageSchema.getEnvironment(),
                messageSchema.getTeamname(), messageSchema.getSchemafull(),
                messageSchema.getSchemaversion())));
        return "success";
    }

    public String insertIntoUsers(UserInfo userInfo){
        session.execute(boundStatementUsers.bind(userInfo.getFullname(),userInfo.getUsername(),
                userInfo.getPwd(),userInfo.getTeam(),userInfo.getRole()));
        return "success";
    }

    public String insertIntoTeams(Team team){
        session.execute(boundStatementTeams.bind(team.getTeamname(),team.getTeammail(),
                team.getApp(),team.getTeamphone(), team.getContactperson()));
        return "success";
    }

    public String insertIntoEnvs(Env env){
        session.execute(boundStatementEnvs.bind(env.getName(),env.getHost(),env.getPort(),
                env.getProtocol(),env.getType()
                ,env.getKeyStoreLocation(),env.getTrustStoreLocation(),env.getKeyStorePwd(),
                env.getKeyPwd(),env.getTrustStorePwd(), env.getOtherParams()));
        return "success";
    }

}
