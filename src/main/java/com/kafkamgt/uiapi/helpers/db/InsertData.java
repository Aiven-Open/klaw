package com.kafkamgt.uiapi.helpers.db;


import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.querybuilder.*;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.kafkamgt.uiapi.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
public class InsertData {

    private static Logger LOG = LoggerFactory.getLogger(InsertData.class);
    Cluster cluster;
    Session session;

    @Value("${cassandradb.url}")
    String clusterConnHost;

    @Value("${cassandradb.port}")
    int clusterConnPort;

    @Value("${cassandradb.keyspace}")
    String keyspace;

    @Autowired
    SelectData cassandraSelectHelper;



    @PostConstruct
    public void startCassandra() {

        CodecRegistry myCodecRegistry;
        myCodecRegistry = CodecRegistry.DEFAULT_INSTANCE;
        myCodecRegistry.register(InstantCodec.instance);

        cluster = Cluster
                .builder()
                .addContactPoint(clusterConnHost)
                .withPort(clusterConnPort)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withCodecRegistry(myCodecRegistry)
                .withoutJMXReporting()
                .build();
        session = cluster.connect(keyspace);
    }

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

    public String insertIntoRequestTopic(Topic topic){

        String tableName = null, topicReqType=null, insertstat=null;

            tableName = "topic_requests";
            topicReqType = "Producer";
            insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,partitions,replicationfactor,env,teamname,appname,topictype,requestor," +
                    "requesttime, acl_ip, acl_ssl, remarks, topicstatus) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement statement = session.prepare(insertstat);
            BoundStatement boundStatement = new BoundStatement(statement);
            session.execute(boundStatement.bind(topic.getTopicName(),topic.getTopicpartitions(),topic.getReplicationfactor(),topic.getEnvironment(), topic.getTeamname(), topic.getAppname(),
                    topicReqType, topic.getUsername(), new Date(),topic.getAcl_ip(), topic.getAcl_ssl(), topic.getRemarks(), "created"));

            // Activity log
        insertIntoActivityLogTopic(topic);

            // insert into SOT
        List<Topic> topics = new ArrayList<>();
        topics.add(topic);
            insertIntoTopicSOT(topics);

        AclReq aclReq = new AclReq();
        aclReq.setTopictype("Producer");
        aclReq.setEnvironment(topic.getEnvironment());
        aclReq.setTeamname(topic.getTeamname());
        aclReq.setAcl_ssl(topic.getAcl_ssl());
        aclReq.setAcl_ip(topic.getAcl_ip());
        aclReq.setTopicname(topic.getTopicName());
        aclReq.setRequestingteam(topic.getTeamname());

        List<AclReq> acls = new ArrayList<>();
        acls.add(aclReq);
            insertIntoAclsSOT(acls);

        return "success";
    }

    public String insertIntoTopicSOT(List<Topic> topics){

        String tableName = "topics", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,env,teamname,appname)" +
                "VALUES (?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        topics.forEach(topic-> {
            session.execute(boundStatement.bind(topic.getTopicName(), topic.getEnvironment(),
                    topic.getTeamname(), topic.getAppname()));
        });

        return "success";
    }

    public String insertIntoActivityLogTopic(Topic topic){

        String tableName = "activitylog", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, activityname, activitytype, activitytime, details, user, env, team)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        UserInfo userInfo = cassandraSelectHelper.selectUserInfo(topic.getUsername());

        session.execute(boundStatement.bind(getRandom(), "topic",
                    "new", new Date(),""+topic.getTopicName(),""+topic.getUsername(), topic.getEnvironment(), userInfo.getTeam()));

        return "success";
    }

    public String insertIntoRequestAcl(AclReq aclReq){

        String tableName = null, topicReqType=null, insertstat=null;
            tableName = "acl_requests";
            topicReqType=aclReq.getTopictype();
            insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no,topicname,env,teamname,appname,topictype,requestor," +
                    "requesttime, acl_ip, acl_ssl, remarks, topicstatus,consumergroup,requestingteam) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement statement = session.prepare(insertstat);
            BoundStatement boundStatement = new BoundStatement(statement);

            session.execute(boundStatement.bind(getRandom(),aclReq.getTopicname(),aclReq.getEnvironment(),
                    aclReq.getTeamname(), aclReq.getAppname(),
                    topicReqType, aclReq.getUsername(), new Date(),aclReq.getAcl_ip(),aclReq.getAcl_ssl(),
                    aclReq.getRemarks(), "created",
                    aclReq.getConsumergroup(),cassandraSelectHelper.selectTeamsOfUsers(aclReq.getUsername()).get(0).getTeamname()));

            // Insert into acl
        insertIntoActivityLogAcl(aclReq);

            // Insert to SOT
        List<AclReq> acls = new ArrayList<>();
        acls.add(aclReq);
            insertIntoAclsSOT(acls);

        return "success";
    }

    public String insertIntoAclsSOT(List<AclReq> acls){

        String tableName = "acls", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, topicname, env, teamname, consumergroup, topictype, acl_ip, acl_ssl)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        acls.forEach(aclReq-> {
            session.execute(boundStatement.bind(getRandom(),aclReq.getTopicname(),aclReq.getEnvironment(), aclReq.getTeamname(),
                    aclReq.getConsumergroup(), aclReq.getTopictype(),aclReq.getAcl_ip(),aclReq.getAcl_ssl()));
        });

        return "success";
    }

    public String insertIntoActivityLogAcl(AclReq aclReq){

        String tableName = "activitylog", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, activityname, activitytype, activitytime, details, user, env, team)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        UserInfo userInfo = cassandraSelectHelper.selectUserInfo(aclReq.getUsername());

        session.execute(boundStatement.bind(getRandom(), "acl",
                "new", new Date(),aclReq.getAcl_ip()+"-"+aclReq.getTopicname()+"-"+aclReq.getAcl_ssl()+"-"+
                        aclReq.getConsumergroup()+"-"+aclReq.getTopictype()
                ,""+aclReq.getUsername(), aclReq.getEnvironment(), userInfo.getTeam()));

        return "success";
    }

    public String insertIntoRequestSchema(SchemaRequest schemaRequest){


        String tableName = "schema_requests";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname, env, teamname, appname, requestor," +
                "requesttime, schemafull, remarks, topicstatus, versionschema) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(schemaRequest.getTopicname(),schemaRequest.getEnvironment(),
                schemaRequest.getTeamname(), schemaRequest.getAppname(),
                schemaRequest.getUsername(), new Date(),schemaRequest.getSchemafull(), schemaRequest.getRemarks(), "created",
                schemaRequest.getSchemaversion()));
        return "success";
    }

    public String insertIntoUsers(UserInfo userInfo){
        String tableName = "users";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(fullname,userid, pwd, team, roleid) " +
                "VALUES (?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(userInfo.getFullname(),userInfo.getUsername(),userInfo.getPwd(),userInfo.getTeam(),userInfo.getRole()));
        return "success";
    }

    public String insertIntoTeams(Team team){
        String tableName = "teams";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(team, teammail, app, teamphone," +
                " contactperson) " +
                "VALUES (?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(team.getTeamname(),team.getTeammail(),team.getApp(),team.getTeamphone(),
                team.getContactperson()));
        return "success";
    }

    public String insertIntoEnvs(Env env){
        String tableName = "env";
        String insertstat = "INSERT INTO " + keyspace + "."+tableName+"(name, host, port, protocol, type," +
                " keystorelocation, truststorelocation, keystorepwd, keypwd, truststorepwd ) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);
        session.execute(boundStatement.bind(env.getName(),env.getHost(),env.getPort(),env.getProtocol(),env.getType()
        ,env.getKeystorelocation(),env.getTruststorelocation(),env.getKeystorepwd(),
                env.getKeypwd(),env.getTruststorepwd()));
        return "success";
    }

}
