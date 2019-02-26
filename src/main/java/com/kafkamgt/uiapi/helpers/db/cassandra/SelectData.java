package com.kafkamgt.uiapi.helpers.db.cassandra;


import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.*;
import com.kafkamgt.uiapi.entities.*;
import com.kafkamgt.uiapi.entities.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SelectData{

    private static Logger LOG = LoggerFactory.getLogger(SelectData.class);

    Session session;

    @Value("${cassandradb.keyspace}")
    String keyspace;


    public int getAllRequestsToBeApproved(String requestor){

        List<AclRequests> allAclReqs = selectAclRequests(true,requestor);
        List<SchemaRequest> allSchemaReqs = selectSchemaRequests(true,requestor);
        List<TopicRequest> allTopicRequestReqs = selectTopicRequests(true,requestor);

        int allOutstanding = allAclReqs.size() + allSchemaReqs.size() + allTopicRequestReqs.size();

        return allOutstanding;
    }

    public List<AclRequests> selectAclRequests(boolean allReqs, String requestor){
        AclRequests aclReq = null;
        List<AclRequests> aclList = new ArrayList();
        ResultSet results = null;
        if(allReqs) {
            Clause eqclause = QueryBuilder.eq("topicstatus", "created");
            Select selectQuery = QueryBuilder.select().from(keyspace,"acl_requests").where(eqclause).allowFiltering();
            results = session.execute(selectQuery);
        }else{
            Select selectQuery = QueryBuilder.select().all().from(keyspace,"acl_requests");
            results = session.execute(selectQuery);
        }

        for (Row row : results) {
            String teamName = null;
            if(allReqs)
                teamName = row.getString("teamname");
            else
                teamName = row.getString("requestingteam");

            String teamSelected = null;

            List<Map<String, String>> userList = selectAllUsers();
            for (Map<String, String> stringStringMap : userList) {
                teamSelected = stringStringMap.get(requestor);
                if (teamSelected != null)
                    break;
            }

           // LOG.info("teamSelected--" + teamSelected);

            if (teamSelected != null && teamSelected.equals(teamName)) {

                aclReq = new AclRequests();
                aclReq.setUsername(row.getString("requestor"));
                aclReq.setAcl_ip(row.getString("acl_ip"));
                aclReq.setAcl_ssl(row.getString("acl_ssl"));
                aclReq.setTopicname(row.getString("topicname"));
                aclReq.setAppname(row.getString("appname"));
                aclReq.setEnvironment(row.getString("env"));
                aclReq.setTeamname(row.getString("teamname"));
                aclReq.setRequestingteam(row.getString("requestingteam"));
                aclReq.setRemarks(row.getString("remarks"));
                aclReq.setAclstatus(row.getString("topicstatus"));
                try {
                    aclReq.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
                }catch (Exception e){}
                aclReq.setRequesttime("" + row.getTimestamp("requesttime"));
                aclReq.setConsumergroup("" + row.getString("consumergroup"));
                aclReq.setTopictype("" + row.getString("topictype"));
                aclReq.setReq_no(row.getString("req_no"));
                aclList.add(aclReq);
            }


        }

        return aclList;
    }

    public List<SchemaRequest> selectSchemaRequests(boolean allReqs, String requestor){
        SchemaRequest schemaRequest = null;
        List<SchemaRequest> schemaList = new ArrayList();
        ResultSet results = null;
        if(allReqs) {
            Clause eqclause = QueryBuilder.eq("topicstatus", "created");
            Select selectQuery = QueryBuilder.select().from(keyspace,"schema_requests").where(eqclause).allowFiltering();
            results = session.execute(selectQuery);
        }else{
            Select selectQuery = QueryBuilder.select().all().from(keyspace,"schema_requests");
            results = session.execute(selectQuery);
        }

        for (Row row : results) {
            String teamName = row.getString("teamname");

            String teamSelected = null;

            List<Map<String, String>> userList = selectAllUsers();
            for (Map<String, String> stringStringMap : userList) {
                teamSelected = stringStringMap.get(requestor);
                if (teamSelected != null)
                    break;
            }

            LOG.info("teamSelected--" + teamSelected);

            if (teamSelected != null && teamSelected.equals(teamName)) {
                schemaRequest = new SchemaRequest();
                schemaRequest.setTopicname(row.getString("topicname"));
                schemaRequest.setUsername(row.getString("requestor"));
                schemaRequest.setSchemafull(row.getString("schemafull"));
                schemaRequest.setSchemaversion(row.getString("versionschema"));
                schemaRequest.setAppname(row.getString("appname"));
                schemaRequest.setEnvironment(row.getString("env"));
                schemaRequest.setTeamname(teamName);
                schemaRequest.setRemarks(row.getString("remarks"));
                schemaRequest.setTopicstatus(row.getString("topicstatus"));
                try {
                    schemaRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
                }catch (Exception e){}
                schemaRequest.setRequesttime("" + row.getTimestamp("requesttime"));

                schemaList.add(schemaRequest);
            }


        }

        return schemaList;
    }

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env){
        SchemaRequest schemaRequest = null;
        List<SchemaRequest> schemaList = new ArrayList();
        ResultSet results = null;
        Clause eqclause1 = QueryBuilder.eq("versionschema", schemaVersion);
        Clause eqclause2 = QueryBuilder.eq("topicname", topicName);
        Clause eqclause3 = QueryBuilder.eq("env", env);

        Select selectQuery = QueryBuilder.select().from(keyspace,"schema_requests").where(eqclause1)
                .and(eqclause2)
                .and(eqclause3)
                .allowFiltering();
        results = session.execute(selectQuery);

        for (Row row : results) {
            schemaRequest = new SchemaRequest();
            schemaRequest.setTopicname(row.getString("topicname"));
            schemaRequest.setUsername(row.getString("requestor"));
            schemaRequest.setSchemafull(row.getString("schemafull"));
            schemaRequest.setSchemaversion(row.getString("versionschema"));
            schemaRequest.setAppname(row.getString("appname"));
            schemaRequest.setEnvironment(row.getString("env"));
            schemaRequest.setTeamname(row.getString("teamname"));
            schemaRequest.setRemarks(row.getString("remarks"));
            schemaRequest.setTopicstatus(row.getString("topicstatus"));
            try {
                schemaRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
            }catch (Exception e){}
            schemaRequest.setRequesttime(""+row.getTimestamp("requesttime"));
        }

        return schemaRequest;
    }

    public Topic selectTopicDetails(String topic, String env){
        Topic topicObj = null;

        ResultSet results = null;
        Clause eqclause1 = QueryBuilder.eq("topicname", topic);
        Clause eqclause2 = QueryBuilder.eq("env", env);
        Select selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause1)
                .and(eqclause2)
                .allowFiltering();
        results = session.execute(selectQuery);

        for (Row row : results) {

            String teamName = row.getString("teamname");

            topicObj = new Topic();
            topicObj.setTeamname(teamName);
            }


        return topicObj;
    }

    public List<Topic> selectSyncTopics(String env){
        Topic topicRequest = null;
        List<Topic> topicRequestList = new ArrayList();
        ResultSet results = null;
        Clause eqclause = QueryBuilder.eq("env", env);
        Select selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause).allowFiltering();
        results = session.execute(selectQuery);


        for (Row row : results) {

            topicRequest = new Topic();
            topicRequest.setTopicname(row.getString("topicname"));
            topicRequest.setAppname(row.getString("appname"));
            topicRequest.setTeamname(row.getString("teamname"));

            topicRequestList.add(topicRequest);
            }


        return topicRequestList;
    }

    public List<Acl> selectSyncAcls(String env){
        Acl aclReq = null;
        List<Acl> aclList = new ArrayList();
        ResultSet results = null;
        Clause eqclause = QueryBuilder.eq("env", env);
        Select selectQuery = QueryBuilder.select().from(keyspace,"acls").where(eqclause).allowFiltering();
        results = session.execute(selectQuery);

        for (Row row : results) {

            aclReq = new Acl();
            aclReq.setReq_no(row.getString("req_no"));
            aclReq.setTopicname(row.getString("topicname"));
            aclReq.setTeamname(row.getString("teamname"));
            aclReq.setAclip(row.getString("acl_ip"));
            aclReq.setAclssl(row.getString("acl_ssl"));
            aclReq.setConsumergroup(row.getString("consumergroup"));
            aclReq.setTopictype(row.getString("topictype"));

            aclList.add(aclReq);
        }


        return aclList;
    }

    public List<TopicRequest> selectTopicRequests(boolean allReqs, String requestor){
        TopicRequest topicRequest = null;
        List<TopicRequest> topicRequestList = new ArrayList();
        ResultSet results = null;
        if(allReqs) {
            Clause eqclause = QueryBuilder.eq("topicstatus", "created");
            Select selectQuery = QueryBuilder.select().from(keyspace,"topic_requests").where(eqclause).allowFiltering();
            results = session.execute(selectQuery);
        }else{
            Select selectQuery = QueryBuilder.select().all().from(keyspace,"topic_requests");
            results = session.execute(selectQuery);
        }

        for (Row row : results) {

            String teamName = row.getString("teamname");

            String teamSelected = null;

            List<Map<String,String>> userList = selectAllUsers();
            for (Map<String, String> stringStringMap : userList) {
                teamSelected = stringStringMap.get(requestor);
                if(teamSelected!=null)
                    break;
            }

            //LOG.info("teamSelected--"+teamSelected);

            if(teamSelected!=null && teamSelected.equals(teamName)) {

                topicRequest = new TopicRequest();
                topicRequest.setUsername(row.getString("requestor"));
                topicRequest.setAcl_ip(row.getString("acl_ip"));
                topicRequest.setAcl_ssl(row.getString("acl_ssl"));
                topicRequest.setTopicname(row.getString("topicname"));
                topicRequest.setTopicpartitions(row.getString("partitions"));
                topicRequest.setReplicationfactor(row.getString("replicationfactor"));
                topicRequest.setAppname(row.getString("appname"));
                topicRequest.setEnvironment(row.getString("env"));
                topicRequest.setTeamname(teamName);
                topicRequest.setRemarks(row.getString("remarks"));
                topicRequest.setTopicstatus(row.getString("topicstatus"));
                try {
                    topicRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
                }catch (Exception e){}
                topicRequest.setRequesttime("" + row.getTimestamp("requesttime"));

                topicRequestList.add(topicRequest);
            }
        }

        return topicRequestList;
    }

    public TopicRequest selectTopicRequestsForTopic(String topicName, String env){
        TopicRequest topicRequest = null;
        ResultSet results = null;

            Clause eqclause = QueryBuilder.eq("topicname", topicName);
            Clause eqclause1 = QueryBuilder.eq("env", env);
            Select selectQuery = QueryBuilder.select().from(keyspace,"topic_requests").where(eqclause).and(eqclause1).allowFiltering();
            results = session.execute(selectQuery);


        for (Row row : results) {

                topicRequest = new TopicRequest();
                topicRequest.setUsername(row.getString("requestor"));
                topicRequest.setAcl_ip(row.getString("acl_ip"));
                topicRequest.setAcl_ssl(row.getString("acl_ssl"));
                topicRequest.setTopicname(row.getString("topicname"));
                topicRequest.setTopicpartitions(row.getString("partitions"));
                topicRequest.setReplicationfactor(row.getString("replicationfactor"));
                topicRequest.setAppname(row.getString("appname"));
                topicRequest.setEnvironment(row.getString("env"));
                topicRequest.setTeamname(row.getString("teamname"));
                topicRequest.setRemarks(row.getString("remarks"));
                topicRequest.setTopicstatus(row.getString("topicstatus"));
                try {
                    topicRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
                }catch (Exception e){}
                topicRequest.setRequesttime("" + row.getTimestamp("requesttime"));

            }

        return topicRequest;
    }

    public List<PCStream> selectTopicStreams(String envSelected){
        PCStream pcStream = null;
        List<PCStream> pcStreams = new ArrayList();
        ResultSet results = null;

        Clause eqclause = QueryBuilder.eq("env", envSelected);
        Select selectQuery = QueryBuilder.select("topicname","teamname")
                .from(keyspace,"topics").where(eqclause).allowFiltering();
        results = session.execute(selectQuery);

        Select selectQuery1 = QueryBuilder.select("topicname","teamname","topictype")
                .from(keyspace,"acls").where(eqclause).allowFiltering();
        ResultSet results1 = session.execute(selectQuery1);

        List<AclRequests> aclReqList = new ArrayList<>();
        for (Row row1 : results1) {
            AclRequests aclReq = new AclRequests();
            aclReq.setTopicname(row1.getString("topicname"));
            aclReq.setRequestingteam(row1.getString("teamname"));
            aclReq.setTopictype(row1.getString("topictype"));
            aclReqList.add(aclReq);
        }
        for (Row row : results) {
            String teamName = row.getString("teamname");
            String topicName = row.getString("topicname");

            pcStream = new PCStream();
            List<String> prodTeams = new ArrayList<>();
            List<String> consumerTeams = new ArrayList<>();
            pcStream.setTopicName(topicName);

            prodTeams.add(teamName);
           // LOG.info("-----------"+topicName);

            for (AclRequests row1 : aclReqList) {
                String teamName1 = row1.getRequestingteam();
                String topicName1 = row1.getTopicname();
                String aclType = row1.getTopictype();
              //  LOG.info("***-----------"+topicName1);
                if(topicName.equals(topicName1)){
                    LOG.info(topicName+"---"+aclType+"---"+teamName1+"---"+teamName);
                    if(aclType.equals("Producer"))
                        prodTeams.add(teamName1);
                    else if(aclType.equals("Consumer"))
                        consumerTeams.add(teamName1);
                }
            }


            consumerTeams = consumerTeams.stream()
                    .distinct()
                    .collect(Collectors.toList());

            prodTeams = prodTeams.stream()
                    .distinct()
                    .collect(Collectors.toList());

            pcStream.setConsumerTeams(consumerTeams);
            pcStream.setProducerTeams(prodTeams);

            pcStreams.add(pcStream);
        }

        return pcStreams;
    }

    public List<Team> selectAllTeams(){
        Team team = null;
        List<Team> teamList = new ArrayList();
        ResultSet results = null;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"teams");
        results = session.execute(selectQuery);

        for (Row row : results) {
            team = new Team();

            team.setTeamname(row.getString("team"));
            team.setApp(row.getString("app"));
            team.setTeammail(row.getString("teammail"));
            team.setTeamphone(row.getString("teamphone"));
            team.setContactperson(row.getString("contactperson"));

            teamList.add(team);
        }

        //LOG.info("--team is :"+teamList);
        return teamList;
    }

    public AclRequests selectAcl(String req_no){
        AclRequests aclReq = new AclRequests();
        Clause eqclause = QueryBuilder.eq("req_no",req_no);
        ResultSet results = null;
        Select.Where selectQuery = QueryBuilder.select().from(keyspace,"acl_requests").where(eqclause);
        results = session.execute(selectQuery);

        for (Row row : results) {
            aclReq.setEnvironment(row.getString("env"));
            aclReq.setAcl_ip(row.getString("acl_ip"));
            aclReq.setAcl_ssl(row.getString("acl_ssl"));
            aclReq.setConsumergroup(row.getString("consumergroup"));
            aclReq.setTopictype(row.getString("topictype"));
            aclReq.setTopicname(row.getString("topicname"));
        }

        return aclReq;
    }

    public List<Map<String,String>> selectAllUsers(){

        List<Map<String,String>> userList = new ArrayList();
        ResultSet results = null;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"users");
        results = session.execute(selectQuery);

        Map<String,String> userMap = null;

        for (Row row : results) {
            userMap = new HashMap<>();
            userMap.put(row.getString("userid"),row.getString("team"));
            userList.add(userMap);
        }

        //LOG.info("--team is :"+teamList);
        return userList;
    }

    public List<UserInfo> selectAllUsersInfo(){

        List<UserInfo> userList = new ArrayList();
        ResultSet results = null;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"users");
        results = session.execute(selectQuery);

        UserInfo userMap = null;

        for (Row row : results) {
            userMap = new UserInfo();
            userMap.setUsername(row.getString("userid"));
            userMap.setPwd(row.getString("pwd"));
            userMap.setTeam(row.getString("team"));
            userMap.setRole(row.getString("roleid"));
            userMap.setFullname(row.getString("fullname"));

            userList.add(userMap);
        }

        return userList;
    }

    public List<Env> selectAllEnvs(String type){

        List<Env> envList = new ArrayList();
        ResultSet results = null;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"env");
        results = session.execute(selectQuery);

        Env env = null;

        for (Row row : results) {
            env = new Env();
            env.setName(row.getString("name"));
            env.setHost(row.getString("host"));
            env.setPort(row.getString("port"));
            env.setProtocol(row.getString("protocol"));
            env.setType(row.getString("type"));
            env.setKeyStoreLocation(row.getString("keystorelocation"));
            env.setTrustStoreLocation(row.getString("truststorelocation"));
            env.setKeyStorePwd(row.getString("keystorepwd"));
            env.setKeyPwd(row.getString("keypwd"));
            env.setTrustStorePwd(row.getString("truststorepwd"));

            if(row.getString("type").equals(type))
                envList.add(env);

        }

        return envList;
    }

    public Env selectEnvDetails(String environment){

        ResultSet results = null;
        Clause eqclause = QueryBuilder.eq("name",environment);
        Select.Where selectQuery = QueryBuilder.select().all().from(keyspace,"env").where(eqclause);
        results = session.execute(selectQuery);

        Env env = null;

        for (Row row : results) {
            env = new Env();
            env.setName(row.getString("name"));
            env.setHost(row.getString("host"));
            env.setPort(row.getString("port"));
            env.setProtocol(row.getString("protocol"));
            env.setType(row.getString("type"));
            env.setKeyStoreLocation(row.getString("keystorelocation"));
            env.setTrustStoreLocation(row.getString("truststorelocation"));
            env.setKeyStorePwd(row.getString("keystorepwd"));
            env.setKeyPwd(row.getString("keypwd"));
            env.setTrustStorePwd(row.getString("truststorepwd"));

        }
        return env;
    }

    public UserInfo selectUserInfo(String username){
        UserInfo userMap = null;

        ResultSet results = null;
        Clause eqclause = QueryBuilder.eq("userid",username);
        Select.Where selectQuery = QueryBuilder.select().all().from(keyspace,"users").where(eqclause);
        results = session.execute(selectQuery);

        for (Row row : results) {
            userMap = new UserInfo();
            userMap.setUsername(username);
            //userMap.setPwd(row.getString("pwd"));
            userMap.setTeam(row.getString("team"));
            userMap.setRole(row.getString("roleid"));
            userMap.setFullname(row.getString("fullname"));
        }

        return userMap;
    }

    public List<ActivityLog> selectActivityLog(String username, String env){
        List<ActivityLog> activityList = new ArrayList();
        ActivityLog activityLog = null;
        String tableName="activitylog";
        ResultSet results = null;
        Select selectQuery = null;
        UserInfo userInfo = selectUserInfo(username);
        if(userInfo.getRole().equals("SUPERUSER"))
        {
            Clause eqclause2 = QueryBuilder.eq("env",env);
            selectQuery = QueryBuilder.select().all().from(keyspace,tableName).where(eqclause2).allowFiltering();
        }
        else{
            Clause eqclause1 = QueryBuilder.eq("team",userInfo.getTeam());
            Clause eqclause2 = QueryBuilder.eq("env",env);
            Ordering ordering = QueryBuilder.asc("activitytime");
            selectQuery = QueryBuilder.select().all().from(keyspace,tableName).where(eqclause1).and(eqclause2)
                    .allowFiltering();
        }

        results = session.execute(selectQuery);
        for (Row row : results) {
            activityLog = new ActivityLog();

            activityLog.setActivityName(row.getString("activityname"));
            activityLog.setActivityType(row.getString("activitytype"));
            try {
                activityLog.setActivityTime(new java.sql.Timestamp((row.getTimestamp("activitytime")).getTime()));
            }catch (Exception e){}
            activityLog.setDetails(row.getString("details"));
            activityLog.setUser(row.getString("user"));
            activityLog.setEnv(row.getString("env"));
            activityLog.setTeam(row.getString("team"));

            activityList.add(activityLog);
        }

        return activityList;
    }


    public List<Team> selectTeamsOfUsers(String username){

        List<Team> teamList = new ArrayList();
        List<Team> teamListSU = new ArrayList();
        List<String> superUserTeamListStr = new ArrayList();
        ResultSet results = null;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"users");
        results = session.execute(selectQuery);

        Map<String,String> userMap = null;
        Team team = null;

        String teamName = null;
        boolean isSuperUser = false;

        for (Row row : results) {
            team = new Team();

            teamName = row.getString("team");

            superUserTeamListStr.add(teamName);

            if(username.equals(row.getString("userid"))) {
                String role = row.getString("roleid");
                if(role.equals("SUPERUSER")){
                    isSuperUser = true;
                }else {
                    TeamPK teamPK = new TeamPK();
                    teamPK.setTeamname(teamName);
                    team.setTeamPK(teamPK);
                    teamList.add(team);
                }
            }
        }

        if(isSuperUser) {
            List<String> listWithoutDuplicates = superUserTeamListStr.stream()
                    .distinct().collect(Collectors.toList());
            listWithoutDuplicates.stream().forEach(teamNameNew->{
                Team suTeam = new Team();
                    TeamPK teamPK = new TeamPK();
                    teamPK.setTeamname(teamNameNew);
                    suTeam.setTeamPK(teamPK);
                teamListSU.add(suTeam);
            });
            return teamListSU;
        }
        else
            return teamList;
    }

}
