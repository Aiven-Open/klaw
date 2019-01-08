package com.kafkamgt.uiapi.helpers.db;


import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.querybuilder.*;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.kafkamgt.uiapi.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SelectData {

    private static Logger LOG = LoggerFactory.getLogger(SelectData.class);
    Cluster cluster;
    Session session;

    @Value("${cassandradb.url}")
    String clusterConnHost;

    @Value("${cassandradb.port}")
    int clusterConnPort;

    @Value("${cassandradb.keyspace}")
    String keyspace;



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
        try {
            session = cluster.connect(keyspace);
        }catch (Exception e){
            LOG.error("Could not connect to Cassandra "+clusterConnHost+":"+clusterConnPort);
            System.exit(0);
        }
    }

    public int getAllRequestsToBeApproved(String requestor){

        List<AclReq> allAclReqs = selectAclRequests(true,requestor);
        List<SchemaRequest> allSchemaReqs = selectSchemaRequests(true,requestor);
        List<Topic> allTopicReqs = selectTopicRequests(true,requestor);

        int allOutstanding = allAclReqs.size() + allSchemaReqs.size() + allTopicReqs.size();

        return allOutstanding;
    }

    public List<AclReq> selectAclRequests(boolean allReqs, String requestor){
        AclReq aclReq = null;
        List<AclReq> aclList = new ArrayList();
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

                aclReq = new AclReq();
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
                aclReq.setApprovingtime("" + row.getTimestamp("exectime"));
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
                schemaRequest.setApprovingtime("" + row.getTimestamp("exectime"));
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
            schemaRequest.setApprovingtime(""+row.getTimestamp("exectime"));
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
        Topic topic = null;
        List<Topic> topicList = new ArrayList();
        ResultSet results = null;
        Clause eqclause = QueryBuilder.eq("env", env);
        Select selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause).allowFiltering();
        results = session.execute(selectQuery);


        for (Row row : results) {

            topic = new Topic();
            topic.setTopicName(row.getString("topicname"));
            topic.setAppname(row.getString("appname"));
            topic.setTeamname(row.getString("teamname"));

            topicList.add(topic);
            }


        return topicList;
    }

    public List<AclReq> selectSyncAcls(String env){
        AclReq aclReq = null;
        List<AclReq> aclList = new ArrayList();
        ResultSet results = null;
        Clause eqclause = QueryBuilder.eq("env", env);
        Select selectQuery = QueryBuilder.select().from(keyspace,"acls").where(eqclause).allowFiltering();
        results = session.execute(selectQuery);

        for (Row row : results) {

            aclReq = new AclReq();
            aclReq.setReq_no(row.getString("req_no"));
            aclReq.setTopicname(row.getString("topicname"));
            aclReq.setTeamname(row.getString("teamname"));
            aclReq.setAcl_ip(row.getString("acl_ip"));
            aclReq.setAcl_ssl(row.getString("acl_ssl"));
            aclReq.setConsumergroup(row.getString("consumergroup"));
            aclReq.setTopictype(row.getString("topictype"));

            aclList.add(aclReq);
        }


        return aclList;
    }

    public List<Topic> selectTopicRequests(boolean allReqs, String requestor){
        Topic topic = null;
        List<Topic> topicList = new ArrayList();
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

                topic = new Topic();
                topic.setUsername(row.getString("requestor"));
                topic.setAcl_ip(row.getString("acl_ip"));
                topic.setAcl_ssl(row.getString("acl_ssl"));
                topic.setTopicName(row.getString("topicname"));
                topic.setTopicpartitions(row.getString("partitions"));
                topic.setReplicationfactor(row.getString("replicationfactor"));
                topic.setAppname(row.getString("appname"));
                topic.setEnvironment(row.getString("env"));
                topic.setTeamname(teamName);
                topic.setRemarks(row.getString("remarks"));
                topic.setTopicstatus(row.getString("topicstatus"));
                topic.setApprovingtime("" + row.getTimestamp("exectime"));
                topic.setRequesttime("" + row.getTimestamp("requesttime"));

                topicList.add(topic);
            }
        }

        return topicList;
    }

    public Topic selectTopicRequestsForTopic(String topicName){
        Topic topic = null;
        ResultSet results = null;

            Clause eqclause = QueryBuilder.eq("topicname", topicName);
            Select selectQuery = QueryBuilder.select().from(keyspace,"topic_requests").where(eqclause).allowFiltering();
            results = session.execute(selectQuery);


        for (Row row : results) {

                topic = new Topic();
                topic.setUsername(row.getString("requestor"));
                topic.setAcl_ip(row.getString("acl_ip"));
                topic.setAcl_ssl(row.getString("acl_ssl"));
                topic.setTopicName(row.getString("topicname"));
                topic.setTopicpartitions(row.getString("partitions"));
                topic.setReplicationfactor(row.getString("replicationfactor"));
                topic.setAppname(row.getString("appname"));
                topic.setEnvironment(row.getString("env"));
                topic.setTeamname(row.getString("teamname"));
                topic.setRemarks(row.getString("remarks"));
                topic.setTopicstatus(row.getString("topicstatus"));
                topic.setApprovingtime("" + row.getTimestamp("exectime"));
                topic.setRequesttime("" + row.getTimestamp("requesttime"));

            }

        return topic;
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

        List<AclReq> aclReqList = new ArrayList<>();
        for (Row row1 : results1) {
            AclReq aclReq = new AclReq();
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

            for (AclReq row1 : aclReqList) {
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

    public AclReq selectAcl(String req_no){
        AclReq aclReq = new AclReq();
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
            env.setKeystorelocation(row.getString("keystorelocation"));
            env.setTruststorelocation(row.getString("truststorelocation"));
            env.setKeystorepwd(row.getString("keystorepwd"));
            env.setKeypwd(row.getString("keypwd"));
            env.setTruststorepwd(row.getString("truststorepwd"));

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
            env.setKeystorelocation(row.getString("keystorelocation"));
            env.setTruststorelocation(row.getString("truststorelocation"));
            env.setKeystorepwd(row.getString("keystorepwd"));
            env.setKeypwd(row.getString("keypwd"));
            env.setTruststorepwd(row.getString("truststorepwd"));

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
            activityLog.setActivityTime(""+row.getTimestamp("activitytime"));
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
                    team.setTeamname(teamName);
                    teamList.add(team);
                }
            }
        }

        if(isSuperUser) {
            List<String> listWithoutDuplicates = superUserTeamListStr.stream()
                    .distinct().collect(Collectors.toList());
            listWithoutDuplicates.stream().forEach(teamNameNew->{
                Team suTeam = new Team();
                suTeam.setTeamname(teamNameNew);
                teamListSU.add(suTeam);
            });
            return teamListSU;
        }
        else
            return teamList;
    }


}
