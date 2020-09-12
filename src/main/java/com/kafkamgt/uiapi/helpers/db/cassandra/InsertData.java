package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.BoundStatement;
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
            boundStatementSchemaReqs, boundStatementSchemas, boundStatementUsers, boundStatementRegisterUsers,
            boundStatementTeams,  boundStatementEnvs;

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
                "teamname, appname, topictype, requestor," +
                "requesttime,  remarks, topicstatus) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?);";
        boundStatementInsertIntoRequestTopic  = getBoundStatement(insertStat);
    }

    private void getBoundStatementInsertIntoTopicSOT() {
        String tableName = "topics";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,env,partitions,replicationfactor,teamname,appname)" +
                "VALUES (?,?,?,?,?,?);";
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
                "requesttime, acl_ip, acl_ssl, remarks, topicstatus,consumergroup,requestingteam,acltype) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
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
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(fullname, userid, pwd, team, roleid, mailid) " +
                "VALUES (?,?,?,?,?,?);";
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

        session.execute(boundStatementInsertIntoRequestTopic.bind(topicRequest.getTopicname(),
                topicRequest.getTopicpartitions(),
                topicRequest.getReplicationfactor(),
                topicRequest.getEnvironment(), topicRequest.getTeamname(),
                topicRequest.getAppname(),
                topicRequest.getTopictype(), topicRequest.getUsername(), new Date(), topicRequest.getRemarks(), "created"));

        // Activity log
        if(insertIntoActivityLogTopic(topicRequest).equals("success"))
            return "success";
        else
            return "failure";
    }

    public String insertIntoTopicSOT(List<Topic> topicRequests, boolean isSyncTopics){
        for(Topic topic:topicRequests) {
            session.execute(boundStatementInsertIntoTopicSOT.bind(topic.getTopicname(), topic.getEnvironment(),
                    topic.getNoOfPartitions(), topic.getNoOfReplcias(), topic.getTeamname(), topic.getAppname()));
        }

        return "success";
    }

    String insertIntoActivityLogTopic(TopicRequest topicRequest){

        UserInfo userInfo = cassandraSelectHelper.selectUserInfo(topicRequest.getUsername());

        session.execute(boundStatementInsertIntoActivityLogAcl.bind(getRandom(), "topicRequest",
                    "new", new Date(),""+ topicRequest.getTopicname(),""+ topicRequest.getUsername(),
                topicRequest.getEnvironment(), userInfo.getTeam()));

        return "success";
    }

    String insertIntoRequestAcl(AclRequests aclReq){
        String reqNo = aclReq.getReq_no();

        if(aclReq.getAclType() !=null && aclReq.getAclType().equals("Create")){
            reqNo = getRandom();
        }

        String topicReqType = aclReq.getTopictype();
        session.execute(boundStatementAclRequest.bind(reqNo,aclReq.getTopicname(),aclReq.getEnvironment(),
                aclReq.getTeamname(), aclReq.getAppname(),
                topicReqType, aclReq.getUsername(), new Date(),aclReq.getAcl_ip(),aclReq.getAcl_ssl(),
                aclReq.getRemarks(), "created",
                aclReq.getConsumergroup(), cassandraSelectHelper.selectTeamsOfUsers(aclReq.getUsername()).get(0).getTeamname(),
                aclReq.getAclType()));

        // Insert into acl
        if(insertIntoActivityLogAcl(aclReq).equals("success"))
            return "success";
        else
            return "failure";
    }

    public String insertIntoAclsSOT(List<Acl> acls, boolean isSyncAcls){

        acls.forEach(aclReq-> {
            if(aclReq.getReq_no()!=null)
                session.execute(boundStatementInsertAclsSOT.bind(aclReq.getReq_no(),aclReq.getTopicname(),
                    aclReq.getEnvironment(), aclReq.getTeamname(),
                    aclReq.getConsumergroup(), aclReq.getTopictype(),
                    aclReq.getAclip(),aclReq.getAclssl()));
            else
                session.execute(boundStatementInsertAclsSOT.bind(getRandom(),aclReq.getTopicname(),
                        aclReq.getEnvironment(), aclReq.getTeamname(),
                        aclReq.getConsumergroup(), aclReq.getTopictype(),
                        aclReq.getAclip(),aclReq.getAclssl()));
        });

        return "success";
    }

    private String insertIntoActivityLogAcl(AclRequests aclReq){
        UserInfo userInfo = cassandraSelectHelper.selectUserInfo(aclReq.getUsername());

        session.execute(boundStatementInsertIntoActivityLogAcl.bind(getRandom(), "acl",
                aclReq.getAclType(), new Date(),aclReq.getAcl_ip()+"-"+aclReq.getTopicname()+"-"+aclReq.getAcl_ssl()+"-"+
                        aclReq.getConsumergroup()+"-"+aclReq.getTopictype()
                ,""+aclReq.getUsername(), aclReq.getEnvironment(), userInfo.getTeam()));

        return "success";
    }

    String insertIntoRequestSchema(SchemaRequest schemaRequest){
        session.execute(boundStatementSchemaReqs.bind(schemaRequest.getTopicname(), schemaRequest.getEnvironment(),
                schemaRequest.getTeamname(), schemaRequest.getAppname(),
                schemaRequest.getUsername(), new Date(), schemaRequest.getSchemafull().trim(), schemaRequest.getRemarks(), "created",
                schemaRequest.getSchemaversion()));
        return "success";
    }

    String insertIntoMessageSchemaSOT(List<MessageSchema> messageSchemas){
        messageSchemas.forEach(messageSchema ->
            session.execute(boundStatementSchemas.bind(messageSchema.getTopicname(),messageSchema.getEnvironment(),
                messageSchema.getTeamname(), messageSchema.getSchemafull().trim(),
                messageSchema.getSchemaversion())));
        return "success";
    }

    String insertIntoUsers(UserInfo userInfo){
        session.execute(boundStatementUsers.bind(userInfo.getFullname(),userInfo.getUsername(),
                userInfo.getPwd(),userInfo.getTeam(),userInfo.getRole(),userInfo.getMailid()));
        return "success";
    }

    String insertIntoTeams(Team team){
        session.execute(boundStatementTeams.bind(team.getTeamname(),team.getTeammail(),
                "",team.getTeamphone(), team.getContactperson()));
        return "success";
    }

    String insertIntoEnvs(Env env){
        session.execute(boundStatementEnvs.bind(env.getName(),env.getHost(),env.getPort(),
                env.getProtocol(),env.getType()
                ,env.getKeyStoreLocation(),env.getTrustStoreLocation(),env.getKeyStorePwd(),
                env.getKeyPwd(),env.getTrustStorePwd(), env.getOtherParams()));
        return "success";
    }

    String updateLicense(String org, String version, String licenseKey) throws Exception {
        String tableName = "productdetails";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(name, version, licensekey ) " +
                "VALUES (?,?,?);";
        BoundStatement bndProduct = new BoundStatement(session.prepare(insertstat));
        if(licenseKey!=null && licenseKey.length()>0)
            session.execute(bndProduct.bind(org, version, licenseKey));
        else
            throw new Exception("Invalid license");
        return "success";
    }

}
