package com.kafkamgt.uiapi.helpers.db.cassandra;


import com.datastax.driver.core.*;
import com.kafkamgt.uiapi.entities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InsertData {

    private static Logger LOG = LoggerFactory.getLogger(InsertData.class);

    Session session;

    @Value("${cassandradb.keyspace}")
    String keyspace;

    SelectData cassandraSelectHelper;

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

        String tableName = null, topicReqType=null, insertstat=null;

            tableName = "topic_requests";
            topicReqType = "Producer";
            insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,partitions,replicationfactor,env,teamname,appname,topictype,requestor," +
                    "requesttime, acl_ip, acl_ssl, remarks, topicstatus) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement statement = session.prepare(insertstat);
            BoundStatement boundStatement = new BoundStatement(statement);
            session.execute(boundStatement.bind(topicRequest.getTopicname(), topicRequest.getTopicpartitions(), topicRequest.getReplicationfactor(), topicRequest.getEnvironment(), topicRequest.getTeamname(), topicRequest.getAppname(),
                    topicReqType, topicRequest.getUsername(), new Date(), topicRequest.getAcl_ip(), topicRequest.getAcl_ssl(), topicRequest.getRemarks(), "created"));

            // Activity log
        insertIntoActivityLogTopic(topicRequest);

            // insert into SOT
        List<TopicRequest> topicRequests = new ArrayList<>();
        topicRequests.add(topicRequest);
            insertIntoTopicSOT(topicRequests);

        AclRequests aclReq = new AclRequests();
        aclReq.setTopictype("Producer");
        aclReq.setEnvironment(topicRequest.getEnvironment());
        aclReq.setTeamname(topicRequest.getTeamname());
        aclReq.setAcl_ssl(topicRequest.getAcl_ssl());
        aclReq.setAcl_ip(topicRequest.getAcl_ip());
        aclReq.setTopicname(topicRequest.getTopicname());
        aclReq.setRequestingteam(topicRequest.getTeamname());

        List<AclRequests> acls = new ArrayList<>();
        acls.add(aclReq);
            insertIntoAclsSOT(acls);

        return "success";
    }

    public String insertIntoTopicSOT(List<TopicRequest> topicRequests){

        String tableName = "topics", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,env,teamname,appname)" +
                "VALUES (?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        topicRequests.forEach(topic-> {
            session.execute(boundStatement.bind(topic.getTopicname(), topic.getEnvironment(),
                    topic.getTeamname(), topic.getAppname()));
        });

        return "success";
    }

    public String insertIntoActivityLogTopic(TopicRequest topicRequest){

        String tableName = "activitylog", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, activityname, activitytype, activitytime, details, user, env, team)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        com.kafkamgt.uiapi.entities.UserInfo userInfo = cassandraSelectHelper.selectUserInfo(topicRequest.getUsername());

        session.execute(boundStatement.bind(getRandom(), "topicRequest",
                    "new", new Date(),""+ topicRequest.getTopicname(),""+ topicRequest.getUsername(), topicRequest.getEnvironment(), userInfo.getTeam()));

        return "success";
    }

    public String insertIntoRequestAcl(AclRequests aclReq){

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
        List<AclRequests> acls = new ArrayList<>();
        acls.add(aclReq);
            insertIntoAclsSOT(acls);

        return "success";
    }

    public String insertIntoAclsSOT(List<AclRequests> acls){

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

    public String insertIntoActivityLogAcl(AclRequests aclReq){

        String tableName = "activitylog", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, activityname, activitytype, activitytime, details, user, env, team)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        com.kafkamgt.uiapi.entities.UserInfo userInfo = cassandraSelectHelper.selectUserInfo(aclReq.getUsername());

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

    public String insertIntoUsers(com.kafkamgt.uiapi.entities.UserInfo userInfo){
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
        ,env.getKeyStoreLocation(),env.getTrustStoreLocation(),env.getKeyStorePwd(),
                env.getKeyPwd(),env.getTrustStorePwd()));
        return "success";
    }

}
