package com.kafkamgt.uiapi.helpers.db.jdbc;

import com.kafkamgt.uiapi.entities.*;
import com.kafkamgt.uiapi.entities.Acl;
import com.kafkamgt.uiapi.entities.Topic;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
public class InsertDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(InsertDataJdbc.class);

    @Autowired
    UserInfoRepo userInfoRepo;

    @Autowired
    TeamRepo teamRepo;

    @Autowired
    EnvRepo envRepo;

    @Autowired
    ActivityLogRepo activityLogRepo;

    @Autowired
    AclRequestsRepo aclRequestsRepo;

    @Autowired
    TopicRepo topicRepo;

    @Autowired
    AclRepo aclRepo;

    @Autowired
    TopicRequestsRepo topicRequestsRepo;

    @Autowired
    SchemaRequestRepo schemaRequestRepo;

    @Autowired
    SelectDataJdbc jdbcSelectHelper;

    public String getRandom(){
        int length = 8;
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            builder.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return builder.toString();
    }

    public String insertIntoRequestTopic(TopicRequest topicRequest){

        TopicRequestPK topicRequestPK = new TopicRequestPK();
        topicRequestPK.setTopicname(topicRequest.getTopicname());
        topicRequestPK.setEnvironment(topicRequest.getEnvironment());
        topicRequest.setTopicRequestPK(topicRequestPK);
        topicRequest.setTopicstatus("created");
        topicRequest.setRequesttime((new Timestamp(System.currentTimeMillis())).toString());
        topicRequestsRepo.save(topicRequest);

        UserInfo userInfo = jdbcSelectHelper.selectUserInfo(topicRequest.getUsername());

        ActivityLog activityLog = new ActivityLog();
        activityLog.setReq_no(getRandom());
        activityLog.setActivityName("TopicRequest");
        activityLog.setActivityType("new");
        activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
        activityLog.setTeam(userInfo.getTeam());
        activityLog.setDetails(topicRequest.getTopicname());
        activityLog.setUser(topicRequest.getUsername());
        activityLog.setEnv(topicRequest.getEnvironment());

        insertIntoActivityLog(activityLog);

//        String tableName = null, topicReqType=null, insertstat=null;
//
//            tableName = "topic_requests";
//            topicReqType = "Producer";
//            insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,partitions,replicationfactor,env,teamname,appname,topictype,requestor," +
//                    "requesttime, acl_ip, acl_ssl, remarks, topicstatus) " +
//                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
//            PreparedStatement statement = session.prepare(insertstat);
//            BoundStatement boundStatement = new BoundStatement(statement);
//            session.execute(boundStatement.bind(topicRequest.getTopicName(),topicRequest.getTopicpartitions(),topicRequest.getReplicationfactor(),topicRequest.getEnvironment(), topicRequest.getTeamname(), topicRequest.getAppname(),
//                    topicReqType, topicRequest.getUsername(), new Date(),topicRequest.getAcl_ip(), topicRequest.getAcl_ssl(), topicRequest.getRemarks(), "created"));
//
        return "success";
    }

    public String insertIntoTopicSOT(List<Topic> topics){

        topics.forEach(topic->
                {
                    topicRepo.save(topic);
                }
        );

        return "success";
    }

    public String insertIntoActivityLog(ActivityLog activityLog){

        activityLogRepo.save(activityLog);

        return "success";
    }

    public String insertIntoRequestAcl(AclRequests aclReq){

        aclReq.setReq_no(getRandom());
        aclReq.setAclstatus("created");
        aclReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
        aclReq.setRequestingteam(jdbcSelectHelper.selectUserInfo(aclReq.getUsername()).getTeam());
        aclRequestsRepo.save(aclReq);

//        String tableName = null, topicReqType=null, insertstat=null;
//            tableName = "acl_requests";
//            topicReqType=aclReq.getTopictype();
//            insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no,topicname,env,teamname,appname,topictype,requestor," +
//                    "requesttime, acl_ip, acl_ssl, remarks, topicstatus,consumergroup,requestingteam) " +
//                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
//            PreparedStatement statement = session.prepare(insertstat);
//            BoundStatement boundStatement = new BoundStatement(statement);
//
//            session.execute(boundStatement.bind(getRandom(),aclReq.getTopicname(),aclReq.getEnvironment(),
//                    aclReq.getTeamname(), aclReq.getAppname(),
//                    topicReqType, aclReq.getUsername(), new Date(),aclReq.getAcl_ip(),aclReq.getAcl_ssl(),
//                    aclReq.getRemarks(), "created",
//                    aclReq.getConsumergroup(),cassandraSelectHelper.selectTeamsOfUsers(aclReq.getUsername()).get(0).getTeamname()));
//
        UserInfo userInfo = jdbcSelectHelper.selectUserInfo(aclReq.getUsername());

        ActivityLog activityLog = new ActivityLog();
        activityLog.setReq_no(getRandom());
        activityLog.setActivityName("AclRequest");
        activityLog.setActivityType("new");
        activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
        activityLog.setTeam(userInfo.getTeam());
        activityLog.setDetails(aclReq.getAcl_ip()+"-"+aclReq.getTopicname()+"-"+aclReq.getAcl_ssl()+"-"+
                        aclReq.getConsumergroup()+"-"+aclReq.getTopictype());
        activityLog.setUser(aclReq.getUsername());
        activityLog.setEnv(aclReq.getEnvironment());

            // Insert into acl activity log
        insertIntoActivityLog(activityLog);
        return "success";
    }

    public String insertIntoAclsSOT(List<Acl> acls){

        acls.forEach(acl->{
            acl.setReq_no(getRandom());
            aclRepo.save(acl);
        });
//        String tableName = "acls", insertstat=null;
//
//        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, topicname, env, teamname, consumergroup, topictype, acl_ip, acl_ssl)" +
//                "VALUES (?,?,?,?,?,?,?,?);";
//        PreparedStatement statement = session.prepare(insertstat);
//        BoundStatement boundStatement = new BoundStatement(statement);
//
//        acls.forEach(aclReq-> {
//            session.execute(boundStatement.bind(getRandom(),aclReq.getTopicname(),aclReq.getEnvironment(), aclReq.getTeamname(),
//                    aclReq.getConsumergroup(), aclReq.getTopictype(),aclReq.getAcl_ip(),aclReq.getAcl_ssl()));
//        });

        return "success";
    }

    public String insertIntoRequestSchema(SchemaRequest schemaRequest){

        schemaRequestRepo.save(schemaRequest);

        UserInfo userInfo = jdbcSelectHelper.selectUserInfo(schemaRequest.getUsername());

        ActivityLog activityLog = new ActivityLog();
        activityLog.setReq_no(getRandom());
        activityLog.setActivityName("SchemaRequest");
        activityLog.setActivityType("new");
        activityLog.setActivityTime(new Timestamp(System.currentTimeMillis()));
        activityLog.setTeam(userInfo.getTeam());
        activityLog.setDetails(schemaRequest.getTopicname()+"-"+schemaRequest.getRemarks());
        activityLog.setUser(schemaRequest.getUsername());
        activityLog.setEnv(schemaRequest.getEnvironment());

        // Insert into acl activity log
        insertIntoActivityLog(activityLog);
//
//        String tableName = "schema_requests";
//        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname, env, teamname, appname, requestor," +
//                "requesttime, schemafull, remarks, topicstatus, versionschema) " +
//                "VALUES (?,?,?,?,?,?,?,?,?,?);";
//        PreparedStatement statement = session.prepare(insertstat);
//        BoundStatement boundStatement = new BoundStatement(statement);
//        session.execute(boundStatement.bind(schemaRequest.getTopicname(),schemaRequest.getEnvironment(),
//                schemaRequest.getTeamname(), schemaRequest.getAppname(),
//                schemaRequest.getUsername(), new Date(),schemaRequest.getSchemafull(), schemaRequest.getRemarks(), "created",
//                schemaRequest.getSchemaversion()));
        return "success";
    }

    public String insertIntoUsers(UserInfo userInfo){
        userInfoRepo.save(userInfo);
//        String tableName = "users";
//        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(fullname,userid, pwd, team, roleid) " +
//                "VALUES (?,?,?,?,?);";
//        PreparedStatement statement = session.prepare(insertstat);
//        BoundStatement boundStatement = new BoundStatement(statement);
//        session.execute(boundStatement.bind(userInfo.getFullname(),userInfo.getUsername(),userInfo.getPwd(),userInfo.getTeam(),userInfo.getRole()));
        return "success";
    }

    public String insertIntoTeams(Team team){
        teamRepo.save(team);
//        String tableName = "teams";
//        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(team, teammail, app, teamphone," +
//                " contactperson) " +
//                "VALUES (?,?,?,?,?);";
//        PreparedStatement statement = session.prepare(insertstat);
//        BoundStatement boundStatement = new BoundStatement(statement);
//        session.execute(boundStatement.bind(team.getTeamname(),team.getTeammail(),team.getApp(),team.getTeamphone(),
//                team.getContactperson()));
        return "success";
    }

    public String insertIntoEnvs(Env env){
        envRepo.save(env);
//        String tableName = "env";
//        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(name, host, port, protocol, type," +
//                " keystorelocation, truststorelocation, keystorepwd, keypwd, truststorepwd ) " +
//                "VALUES (?,?,?,?,?,?,?,?,?,?);";
//        PreparedStatement statement = session.prepare(insertstat);
//        BoundStatement boundStatement = new BoundStatement(statement);
//        session.execute(boundStatement.bind(env.getName(),env.getHost(),env.getPort(),env.getProtocol(),env.getType()
//        ,env.getKeystorelocation(),env.getTruststorelocation(),env.getKeystorepwd(),
//                env.getKeypwd(),env.getTruststorepwd()));
        return "success";
    }

}
