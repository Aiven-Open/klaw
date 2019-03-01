package com.kafkamgt.uiapi.helpers.db.cassandra;


import com.datastax.driver.core.*;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.model.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.beans.BeanUtils.copyProperties;

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
        return "success";
    }

    public String insertIntoTopicSOT(List<Topic> topicRequests, boolean isSyncTopics){

        String tableName = "topics", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(topicname,env,teamname,appname)" +
                "VALUES (?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);
        String tableName1 = "acls", insertstat1 = null;
        PreparedStatement statement1 = null;
        BoundStatement boundStatement1 = null;
        TopicRequest topicRequest = null;

        for(Topic topic:topicRequests) {
            session.execute(boundStatement.bind(topic.getTopicname(), topic.getEnvironment(),
                    topic.getTeamname(), topic.getAppname()));

            if(isSyncTopics) {

                insertstat1 = "INSERT INTO " + keyspace + "." + tableName1 + "(req_no, topicname, env, teamname, topictype," +
                        " acl_ip, acl_ssl)" +
                        "VALUES (?,?,?,?,?,?,?);";
                statement1 = session.prepare(insertstat1);
                boundStatement1 = new BoundStatement(statement1);
                topicRequest = cassandraSelectHelper.selectTopicRequestsForTopic(topic.getTopicname(),topic.getEnvironment());
                session.execute(boundStatement1.bind(getRandom(), topic.getTopicname(), topic.getEnvironment(), topic.getTeamname(),
                        "Producer", topicRequest.getAcl_ip(), topicRequest.getAcl_ssl()));
            }
        }

        //--------------------------------------



        return "success";
    }

    public String insertIntoActivityLogTopic(TopicRequest topicRequest){

        String tableName = "activitylog", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, activityname, activitytype, activitytime, details, user, env, team)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        UserInfo userInfo = cassandraSelectHelper.selectUserInfo(topicRequest.getUsername());

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

        return "success";
    }

    public String insertIntoAclsSOT(List<Acl> acls, boolean isSyncAcls){

        String tableName = "acls", insertstat=null;

        insertstat = "INSERT INTO " + keyspace + "."+tableName+"(req_no, topicname, env, teamname, consumergroup, topictype, acl_ip, acl_ssl)" +
                "VALUES (?,?,?,?,?,?,?,?);";
        PreparedStatement statement = session.prepare(insertstat);
        BoundStatement boundStatement = new BoundStatement(statement);

        String tableName1 = "topics", insertstat1=null;

        insertstat1 = "INSERT INTO " + keyspace + "."+tableName1+"(topicname,env,teamname,appname)" +
                "VALUES (?,?,?);";
        PreparedStatement statement1 = session.prepare(insertstat1);
        BoundStatement boundStatement1 = new BoundStatement(statement1);

        acls.forEach(aclReq-> {
            session.execute(boundStatement.bind(getRandom(),aclReq.getTopicname(),aclReq.getEnvironment(), aclReq.getTeamname(),
                    aclReq.getConsumergroup(), aclReq.getTopictype(),aclReq.getAclip(),aclReq.getAclssl()));
            if(isSyncAcls && aclReq.getTopictype().equals("Producer")){
                session.execute(boundStatement.bind(aclReq.getTopicname(), aclReq.getEnvironment(),
                        aclReq.getTeamname()));
            }
        });

        return "success";
    }

    public String insertIntoActivityLogAcl(AclRequests aclReq){

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
        ,env.getKeyStoreLocation(),env.getTrustStoreLocation(),env.getKeyStorePwd(),
                env.getKeyPwd(),env.getTrustStorePwd()));
        return "success";
    }

}
