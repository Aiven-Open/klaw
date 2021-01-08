package com.kafkamgt.uiapi.helpers.db.cassandra;


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Ordering;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kafkamgt.uiapi.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SelectData{

    private static Logger LOG = LoggerFactory.getLogger(SelectData.class);

    Session session;

    @Value("${kafkawize.cassandradb.keyspace:@null}")
    String keyspace;

    public SelectData(){}

    public SelectData(Session session){
        this.session = session;
    }

    public HashMap<String, String> getAllRequestsToBeApproved(String requestor, String role){

        HashMap<String, String> countList = new HashMap<>();
        List<AclRequests> allAclReqs = selectAclRequests(true, requestor, role);
        List<SchemaRequest> allSchemaReqs = selectSchemaRequests(true, requestor);
        List<TopicRequest> allTopicRequestReqs = selectTopicRequests(true, requestor);

        countList.put("topics",allTopicRequestReqs.size()+"");
        countList.put("acls",allAclReqs.size()+"");
        countList.put("schemas",allSchemaReqs.size()+"");

        return countList;
    }

    List<AclRequests> selectAclRequests(boolean allReqs, String requestor, String role){
        AclRequests aclReq ;
        List<AclRequests> aclList = new ArrayList();
        ResultSet results ;
        if(allReqs) {
            Clause eqclause = QueryBuilder.eq("topicstatus", "created");
            Select selectQuery = QueryBuilder.select().from(keyspace,"acl_requests").where(eqclause).allowFiltering();
            results = session.execute(selectQuery);
        }else{
            Select selectQuery = QueryBuilder.select().all().from(keyspace,"acl_requests");
            results = session.execute(selectQuery);
        }

        for (Row row : results) {
            String teamName;
            String aclType = row.getString("acltype"); // Create / Delete
            String requestingTeam = row.getString("requestingteam");
            String topicOwnerTeam = row.getString("teamname");

            if(allReqs) {
                if(role.equals("ROLE_USER"))
                    teamName = requestingTeam;
                else
                    teamName = topicOwnerTeam;

                if(aclType.equals("Delete"))
                    teamName = requestingTeam;
            }
            else
                teamName = requestingTeam;

            String teamSelected = null;

            List<Map<String, String>> userList = selectAllUsers();
            for (Map<String, String> stringStringMap : userList) {
                teamSelected = stringStringMap.get(requestor);
                if (teamSelected != null)
                    break;
            }

            if (teamSelected != null && teamSelected.equals(teamName)) {

                aclReq = new AclRequests();
                aclReq.setUsername(row.getString("requestor"));
                aclReq.setAcl_ip(row.getString("acl_ip"));
                aclReq.setAcl_ssl(row.getString("acl_ssl"));
                aclReq.setTopicname(row.getString("topicname"));
                aclReq.setAppname(row.getString("appname"));
                aclReq.setEnvironment(row.getString("env"));
                aclReq.setTeamname(topicOwnerTeam);
                aclReq.setRequestingteam(requestingTeam);
                aclReq.setRemarks(row.getString("remarks"));
                aclReq.setAclstatus(row.getString("topicstatus"));
                aclReq.setAclType(aclType);
                try {
                     aclReq.setRequesttime(new java.sql.Timestamp((row.getTimestamp("requesttime").getTime())));
                     aclReq.setRequesttimestring((new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                            .format((row.getTimestamp("requesttime")).getTime()));
                }catch (Exception ignored){}

                try {
                    aclReq.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
                }catch (Exception ignored){}

                aclReq.setConsumergroup("" + row.getString("consumergroup"));
                aclReq.setTopictype("" + row.getString("topictype"));
                aclReq.setReq_no(row.getString("req_no"));
                aclList.add(aclReq);
            }
        }

        return aclList;
    }

    public List<SchemaRequest> selectSchemaRequests(boolean allReqs, String requestor){
        SchemaRequest schemaRequest ;
        List<SchemaRequest> schemaList = new ArrayList();
        ResultSet results ;
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
                    schemaRequest.setRequesttime(new java.sql.Timestamp((row.getTimestamp("requesttime")).getTime()));
                }catch (Exception e){}

                try {
                    schemaRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
                }catch (Exception e){}

                schemaList.add(schemaRequest);
            }
        }

        return schemaList;
    }

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env){
        SchemaRequest schemaRequest = null;
        ResultSet results ;
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
                schemaRequest.setRequesttime(new java.sql.Timestamp((row.getTimestamp("requesttime")).getTime()));
            }catch (Exception e){}

            try {
                schemaRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
            }catch (Exception e){}
        }

        return schemaRequest;
    }

    public List<Topic> selectTopicDetails(String topic){
        List<Topic> topicList = new ArrayList<>();
        Topic topicObj = null;

        ResultSet results = null;
        Clause eqclause1 = QueryBuilder.eq("topicname", topic);
        Select selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause1)
                .allowFiltering();
        results = session.execute(selectQuery);

        for (Row row : results) {
                String teamName = row.getString("teamname");
                String env = row.getString("env");

                topicObj = new Topic();
                TopicPK topicPK = new TopicPK();
                topicPK.setTopicname(topic);
                topicPK.setEnvironment(env);
                topicObj.setEnvironment(env);
                topicObj.setTopicPK(topicPK);
                topicObj.setTeamname(teamName);
                topicObj.setTopicname(topic);
                topicObj.setNoOfPartitions(row.getString("partitions"));
                topicObj.setNoOfReplcias(row.getString("replicationfactor"));
            topicList.add(topicObj);
            }

        if(topicList.size()>0)
            return topicList;
        else return null;
    }

    public List<Topic> selectSyncTopics(String env, String teamNameSearch){
        Topic topicRequest = null;
        List<Topic> topicRequestList = new ArrayList<>();
        ResultSet results = null;
        Select selectQuery;
        if(env !=null && !env.equals("ALL")){
            Clause eqclause = QueryBuilder.eq("env", env);
            selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause).allowFiltering();
        }else
            selectQuery = QueryBuilder.select().from(keyspace,"topics");

        results = session.execute(selectQuery);

        TopicPK topicPK ;
        String teamName;

        for (Row row : results) {
            topicRequest = new Topic();
            topicPK = new TopicPK();
            teamName = row.getString("teamname");
            topicPK.setTopicname(row.getString("topicname"));
            topicPK.setEnvironment(row.getString("env"));

            topicRequest.setTopicPK(topicPK);
            topicRequest.setTopicname(row.getString("topicname"));
            topicRequest.setNoOfPartitions(row.getString("partitions"));
            topicRequest.setNoOfReplcias(row.getString("replicationfactor"));
            topicRequest.setAppname(row.getString("appname"));
            topicRequest.setTeamname(teamName);

            if(teamNameSearch==null || teamNameSearch.equals("null") || teamNameSearch.equals("All teams"))
            {
                topicRequestList.add(topicRequest);
            }
            else if(teamNameSearch.equals(teamName))
                    topicRequestList.add(topicRequest);

        }
        return topicRequestList;
    }

    List<Acl> selectSyncAcls(String env){
        List<Acl> aclList = new ArrayList<>();
        ResultSet results = null;
        Select selectQuery;
        if(!env.equals("ALL")){
            Clause eqclause = QueryBuilder.eq("env", env);
            selectQuery = QueryBuilder.select().from(keyspace,"acls").where(eqclause).allowFiltering();
        }
        else
            selectQuery = QueryBuilder.select().from(keyspace,"acls");

        updateAclList(aclList, selectQuery);

        return aclList;
    }

    private void updateAclList(List<Acl> aclList, Select selectQuery) {
        ResultSet results;
        Acl aclReq;
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
            aclReq.setEnvironment(row.getString("env"));

            aclList.add(aclReq);
        }
    }

    Acl selectSyncAclsFromReqNo(String reqNo){
        Acl acl = null;
        ResultSet results ;
        Clause eqclause = QueryBuilder.eq("req_no", reqNo);
        Select selectQuery = QueryBuilder.select().from(keyspace,"acls").where(eqclause).allowFiltering();
        results = session.execute(selectQuery);

        for (Row row : results) {
            acl = new Acl();
            acl.setReq_no(row.getString("req_no"));
            acl.setTopicname(row.getString("topicname"));
            acl.setTeamname(row.getString("teamname"));
            acl.setAclip(row.getString("acl_ip"));
            acl.setAclssl(row.getString("acl_ssl"));
            acl.setEnvironment(row.getString("env"));
            acl.setConsumergroup(row.getString("consumergroup"));
            acl.setTopictype(row.getString("topictype"));
        }

        return acl;
    }

    List<TopicRequest> selectTopicRequests(boolean allReqs, String requestor){
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

            String teamSelected = selectUserInfo(requestor).getTeam();

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
                topicRequest.setRequestor(row.getString("requestor"));
                topicRequest.setTopictype(row.getString("topictype"));
                try {
                    topicRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
                }catch (Exception ignored) {
                }
                try {
                    topicRequest.setRequesttime(new java.sql.Timestamp((row.getTimestamp("requesttime")).getTime()));
                    topicRequest.setRequesttimestring((new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                            .format((row.getTimestamp("requesttime")).getTime()));
                }catch (Exception ignored){}

                topicRequestList.add(topicRequest);
            }
        }

        return topicRequestList;
    }

    public TopicRequest selectTopicRequestsForTopic(String topicName, String env){
        TopicRequest topicRequest = null;
        ResultSet results ;

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
            topicRequest.setTopictype(row.getString("topictype"));
            topicRequest.setRequestor(row.getString("requestor"));
            try {
                topicRequest.setApprovingtime(new java.sql.Timestamp((row.getTimestamp("exectime")).getTime()));
            }catch (Exception e){}
            try {
                topicRequest.setRequesttime(new java.sql.Timestamp((row.getTimestamp("requesttime")).getTime()));
                topicRequest.setRequesttimestring((new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                        .format((row.getTimestamp("requesttime")).getTime()));
            }catch (Exception e){}
        }

        return topicRequest;
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

            TeamPK teamPK = new TeamPK();
            teamPK.setTeamname(row.getString("team"));
            team.setTeamPK(teamPK);

            teamList.add(team);
        }

        return teamList;
    }

    public AclRequests selectAcl(String req_no){
        AclRequests aclReq = new AclRequests();
        Clause eqclause = QueryBuilder.eq("req_no",req_no);
        ResultSet results ;
        Select.Where selectQuery = QueryBuilder.select().from(keyspace,"acl_requests").where(eqclause);
        results = session.execute(selectQuery);

        for (Row row : results) {
            aclReq.setReq_no(req_no);
            aclReq.setUsername(row.getString("requestor"));
            aclReq.setEnvironment(row.getString("env"));
            aclReq.setAcl_ip(row.getString("acl_ip"));
            aclReq.setAcl_ssl(row.getString("acl_ssl"));
            aclReq.setConsumergroup(row.getString("consumergroup"));
            aclReq.setTopictype(row.getString("topictype"));
            aclReq.setTopicname(row.getString("topicname"));
            aclReq.setAclstatus(row.getString("topicstatus"));
            aclReq.setRequestingteam(row.getString("requestingteam"));
            aclReq.setAclType(row.getString("acltype"));
        }

        return aclReq;
    }

    List<Map<String,String>> selectAllUsers(){

        List<Map<String,String>> userList = new ArrayList();
        ResultSet results ;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"users");
        results = session.execute(selectQuery);

        Map<String,String> userMap ;

        for (Row row : results) {
            userMap = new HashMap<>();
            userMap.put(row.getString("userid"), row.getString("team"));
            userList.add(userMap);
        }

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
            userMap.setMailid(row.getString("mailid"));

            userList.add(userMap);
        }

        return userList;
    }

    public List<Env> selectAllEnvs(String type){

        List<Env> envList = new ArrayList();
        ResultSet results ;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"env");
        results = session.execute(selectQuery);

        Env env ;

        for (Row row : results) {
            env = new Env();
            env.setName(row.getString("name"));
            env.setHost(row.getString("host"));
            env.setProtocol(row.getString("protocol"));
            env.setType(row.getString("type"));
            env.setKeyStoreLocation(row.getString("keystorelocation"));
            env.setTrustStoreLocation(row.getString("truststorelocation"));
            env.setKeyStorePwd(row.getString("keystorepwd"));
            env.setKeyPwd(row.getString("keypwd"));
            env.setTrustStorePwd(row.getString("truststorepwd"));
            env.setOtherParams(row.getString("other_params"));

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
            env.setProtocol(row.getString("protocol"));
            env.setType(row.getString("type"));
            env.setKeyStoreLocation(row.getString("keystorelocation"));
            env.setTrustStoreLocation(row.getString("truststorelocation"));
            env.setKeyStorePwd(row.getString("keystorepwd"));
            env.setKeyPwd(row.getString("keypwd"));
            env.setTrustStorePwd(row.getString("truststorepwd"));
            env.setOtherParams(row.getString("other_params"));
        }
        return env;
    }

    public UserInfo selectUserInfo(String username){
        UserInfo userMap = null;

        ResultSet results ;
        Clause eqclause = QueryBuilder.eq("userid",username);
        Select.Where selectQuery = QueryBuilder.select().all().from(keyspace,"users").where(eqclause);
        results = session.execute(selectQuery);

        for (Row row : results) {
            userMap = new UserInfo();
            userMap.setUsername(username);
            userMap.setTeam(row.getString("team"));
            userMap.setRole(row.getString("roleid"));
            userMap.setFullname(row.getString("fullname"));
            userMap.setMailid(row.getString("mailid"));
        }

        return userMap;
    }

    public List<ActivityLog> selectActivityLog(String username, String env){
        List<ActivityLog> activityList = new ArrayList();
        ActivityLog activityLog ;
        String tableName="activitylog";
        ResultSet results ;
        Select selectQuery ;
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
                activityLog.setActivityTimeString((new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss"))
                        .format((row.getTimestamp("activitytime")).getTime()));
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

        List<Team> allTeams = selectAllTeams();

        List<Team> teamList = new ArrayList();
        List<Team> teamListSU = new ArrayList();
        List<String> superUserTeamListStr = new ArrayList();
        ResultSet results ;
        Select selectQuery = QueryBuilder.select().all().from(keyspace,"users");
        results = session.execute(selectQuery);

        Team team ;

        String teamName ;
        boolean isSuperUser = false;

        for (Row row : results) {
            team = new Team();

            teamName = row.getString("team");

            superUserTeamListStr.add(teamName);

            String finalTeamName = teamName;
            Optional<Team> teamSel = allTeams.stream().filter(a->a.getTeamname().equals(finalTeamName)).findFirst();

            if(username.equals(row.getString("userid"))) {
                String role = row.getString("roleid");

                if(role.equals("SUPERUSER")){
                    isSuperUser = true;
                }else {
                    teamList.add(teamSel.get());
                }
            }
        }

        if(isSuperUser) {
            List<String> listWithoutDuplicates = superUserTeamListStr.stream()
                    .distinct().collect(Collectors.toList());
            listWithoutDuplicates.forEach(teamNameNew->{
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

    public HashMap<String, String> getDashboardInfo(String teamName){
        HashMap<String, String> dashboardInfo = new HashMap<>();

        Select selectQuery = QueryBuilder.select().countAll().from(keyspace,"teams");
        ResultSet results = session.execute(selectQuery);
        dashboardInfo.put("teamsize", ""+results.one().getObject("count"));

        selectQuery = QueryBuilder.select().countAll().from(keyspace,"users");
        results = session.execute(selectQuery);
        dashboardInfo.put("users_count", ""+results.one().getObject("count"));

        Clause eqclause1 = QueryBuilder.eq("type","schemaregistry");
        Clause eqclause2 = QueryBuilder.eq("type","kafka");
        Select selectQuery1 = QueryBuilder.select().countAll().from(keyspace,"env").where(eqclause1).allowFiltering();
        results = session.execute(selectQuery1);
        dashboardInfo.put("schema_clusters_count", ""+results.one().getObject("count"));

        selectQuery1 = QueryBuilder.select().countAll().from(keyspace,"env").where(eqclause2).allowFiltering();
        results = session.execute(selectQuery1);
        dashboardInfo.put("kafka_clusters_count", ""+results.one().getObject("count"));

        Clause eqclause = QueryBuilder.eq("teamname", teamName);
        Select selectQuery2 = QueryBuilder.select().from(keyspace,"topics").where(eqclause).allowFiltering();
        results = session.execute(selectQuery2);
        List<String> topics = new ArrayList<>();
        results.all().forEach(row -> topics.add(row.getString("topicname")));
        List<String> newList =  topics.stream().distinct().collect(Collectors.toList());
        dashboardInfo.put("myteamtopics", "" + newList.size());

        return dashboardInfo;
    }

    public List<Topic> getTopics(String topicName) {
        Topic topicRequest = null;
        List<Topic> topicRequestList = new ArrayList<>();
        ResultSet results = null;
        Select selectQuery;
        Clause eqclause1 = QueryBuilder.eq("topicname", topicName);
        selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause1)
                .allowFiltering();

        results = session.execute(selectQuery);

        TopicPK topicPK ;
        String teamName;

        for (Row row : results) {
            topicRequest = new Topic();
            topicPK = new TopicPK();
            teamName = row.getString("teamname");
            topicPK.setTopicname(row.getString("topicname"));
            topicPK.setEnvironment(row.getString("env"));

            topicRequest.setTopicPK(topicPK);
            topicRequest.setTopicname(row.getString("topicname"));
            topicRequest.setNoOfPartitions(row.getString("partitions"));
            topicRequest.setNoOfReplcias(row.getString("replicationfactor"));
            topicRequest.setAppname(row.getString("appname"));
            topicRequest.setTeamname(teamName);

            topicRequestList.add(topicRequest);

        }
        return topicRequestList;
    }

    public List<Acl> selectSyncAcls(String env, String topic) {
            List<Acl> aclList = new ArrayList<>();
            Select selectQuery;
            if(!env.equals("ALL")){
                Clause eqclause = QueryBuilder.eq("env", env);
                Clause eqclause1 = QueryBuilder.eq("topicname", topic);
                selectQuery = QueryBuilder.select().from(keyspace,"acls").where(eqclause)
                        .and(eqclause1)
                        .allowFiltering();
            }
            else {
                Clause eqclause1 = QueryBuilder.eq("topicname", topic);
                selectQuery = QueryBuilder.select().from(keyspace, "acls").where(eqclause1)
                        .allowFiltering();;
            }
            updateAclList(aclList, selectQuery);

            return aclList;
    }

}
