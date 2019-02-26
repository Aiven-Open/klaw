package com.kafkamgt.uiapi.helpers.db.jdbc;

import com.google.common.collect.Lists;
import com.kafkamgt.uiapi.entities.*;
import com.kafkamgt.uiapi.entities.UserInfo;
import com.kafkamgt.uiapi.entities.Topic;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SelectDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(SelectDataJdbc.class);

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

    public int getAllRequestsToBeApproved(String requestor){

        List<AclRequests> allAclReqs = selectAclRequests(true,requestor);
        List<SchemaRequest> allSchemaReqs = selectSchemaRequests(true,requestor);
        List<TopicRequest> allTopicReqs = selectTopicRequests(true,requestor);

        int allOutstanding = allAclReqs.size() + allSchemaReqs.size() + allTopicReqs.size();

        return allOutstanding;
    }

    public List<AclRequests> selectAclRequests(boolean allReqs, String requestor){
        AclRequests aclReq = null;
        List<AclRequests> aclList = new ArrayList();
        List<AclRequests> aclListSub = new ArrayList();
//        ResultSet results = null;
        if(allReqs) {
            aclListSub = aclRequestsRepo.findAllByAclstatus("created");
        }else{
            aclListSub = Lists.newArrayList(aclRequestsRepo.findAll());
        }

        for (AclRequests row : aclListSub) {
            String teamName = null;
            if(allReqs)
                teamName = row.getTeamname();
            else
                teamName = row.getRequestingteam();

            String teamSelected = selectUserInfo(requestor).getTeam();

            if (teamSelected != null && teamSelected.equals(teamName))
                aclList.add(row);
        }

        return aclList;
    }

    public List<SchemaRequest> selectSchemaRequests(boolean allReqs, String requestor){
        SchemaRequest schemaRequest = null;
        List<SchemaRequest> schemaList = new ArrayList();
        List<SchemaRequest> schemaListSub = new ArrayList();
//        ResultSet results = null;
        if(allReqs) {
            schemaListSub = schemaRequestRepo.findAllByTopicstatus("created");
        }else{
            schemaListSub = Lists.newArrayList(schemaRequestRepo.findAll());
        }

        for (SchemaRequest row : schemaListSub) {
            String teamName = row.getTeamname();

            String teamSelected = selectUserInfo(requestor).getTeam();

            LOG.info("teamSelected--" + teamSelected);

            if (teamSelected != null && teamSelected.equals(teamName))
                schemaList.add(row);
        }

        return schemaList;
    }

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env){
        SchemaRequest schemaRequest = null;

        SchemaRequestPK schemaPK = new SchemaRequestPK();
        schemaPK.setEnvironment(env);
        schemaPK.setSchemaversion(schemaVersion);
        schemaPK.setTopicname(topicName);
        return schemaRequestRepo.findById(schemaPK).get();
//        List<SchemaRequest> schemaList = new ArrayList();
//        ResultSet results = null;
//        Clause eqclause1 = QueryBuilder.eq("versionschema", schemaVersion);
//        Clause eqclause2 = QueryBuilder.eq("topicname", topicName);
//        Clause eqclause3 = QueryBuilder.eq("env", env);
//
//        Select selectQuery = QueryBuilder.select().from(keyspace,"schema_requests").where(eqclause1)
//                .and(eqclause2)
//                .and(eqclause3)
//                .allowFiltering();
//        results = session.execute(selectQuery);
//
//        for (Row row : results) {
//            schemaRequest = new SchemaRequest();
//            schemaRequest.setTopicname(row.getString("topicname"));
//            schemaRequest.setUsername(row.getString("requestor"));
//            schemaRequest.setSchemafull(row.getString("schemafull"));
//            schemaRequest.setSchemaversion(row.getString("versionschema"));
//            schemaRequest.setAppname(row.getString("appname"));
//            schemaRequest.setEnvironment(row.getString("env"));
//            schemaRequest.setTeamname(row.getString("teamname"));
//            schemaRequest.setRemarks(row.getString("remarks"));
//            schemaRequest.setTopicstatus(row.getString("topicstatus"));
//            schemaRequest.setApprovingtime(""+row.getTimestamp("exectime"));
//            schemaRequest.setRequesttime(""+row.getTimestamp("requesttime"));
//        }

        //return schemaRequest;
    }

    public Topic selectTopicDetails(String topic, String env){
        Topic topicRequestObj = null;

        Optional<Topic> topicOpt =  topicRepo.findByTopicPKEnvironmentAndTopicPKTopicname(env,topic);

        if(topicOpt.isPresent())
            return topicRepo.findByTopicPKEnvironmentAndTopicPKTopicname(env,topic).get();
        else
            return null;

//        ResultSet results = null;
//        Clause eqclause1 = QueryBuilder.eq("topicname", topic);
//        Clause eqclause2 = QueryBuilder.eq("env", env);
//        Select selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause1)
//                .and(eqclause2)
//                .allowFiltering();
//        results = session.execute(selectQuery);
//
//        for (Row row : results) {
//
//            String teamName = row.getString("teamname");
//
//                topicRequestObj = new TopicRequest();
//                topicRequestObj.setTeamname(teamName);
//            }
//

        //return topicRequestObj;
    }

    public List<Topic> selectSyncTopics(String env){
        //Topic topicRequest = null;
      //  List<Topic> topicRequestList = new ArrayList();
        return topicRepo.findAllByTopicPKEnvironment(env);
//        ResultSet results = null;
//        Clause eqclause = QueryBuilder.eq("env", env);
//        Select selectQuery = QueryBuilder.select().from(keyspace,"topics").where(eqclause).allowFiltering();
//        results = session.execute(selectQuery);
//
//
//        for (Row row : results) {
//
//            topicRequest = new TopicRequest();
//            topicRequest.setTopicName(row.getString("topicname"));
//            topicRequest.setAppname(row.getString("appname"));
//            topicRequest.setTeamname(row.getString("teamname"));
//
//            topicRequestList.add(topicRequest);
//            }
//

        //return topicRequestList;
    }

    public List<Acl> selectSyncAcls(String env){

        return aclRepo.findAllByEnvironment(env);
//        ResultSet results = null;
//        Clause eqclause = QueryBuilder.eq("env", env);
//        Select selectQuery = QueryBuilder.select().from(keyspace,"acls").where(eqclause).allowFiltering();
//        results = session.execute(selectQuery);
//
//        for (Row row : results) {
//
//            aclReq = new Acl();
//            aclReq.setReq_no(row.getString("req_no"));
//            aclReq.setTopicname(row.getString("topicname"));
//            aclReq.setTeamname(row.getString("teamname"));
//            aclReq.setAcl_ip(row.getString("acl_ip"));
//            aclReq.setAcl_ssl(row.getString("acl_ssl"));
//            aclReq.setConsumergroup(row.getString("consumergroup"));
//            aclReq.setTopictype(row.getString("topictype"));
//
//            aclList.add(aclReq);
//        }
//

        //return aclList;
    }

    public List<TopicRequest> selectTopicRequests(boolean allReqs, String requestor){
        TopicRequest topicRequest = null;
        List<TopicRequest> topicRequestList = new ArrayList();

        List<TopicRequest> topicRequestListSub = new ArrayList();

//        ResultSet results = null;
        if(allReqs) {
            topicRequestListSub = topicRequestsRepo.findAllByTopicstatus("created");
        }else
            topicRequestListSub = Lists.newArrayList(topicRequestsRepo.findAll());

        for (TopicRequest row : topicRequestListSub) {

            String teamName = row.getTeamname();

            String teamSelected = selectUserInfo(requestor).getTeam();

            if(teamSelected!=null && teamSelected.equals(teamName)) {
                topicRequestList.add(row);
            }
        }

        return topicRequestList;
    }

    public TopicRequest selectTopicRequestsForTopic(String topicName, String env){
        //TopicRequest topicRequest = null;
        return topicRequestsRepo.findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(topicName,env).get();
//        ResultSet results = null;
//
//            Clause eqclause = QueryBuilder.eq("topicname", topicName);
//            Select selectQuery = QueryBuilder.select().from(keyspace,"topic_requests").where(eqclause).allowFiltering();
//            results = session.execute(selectQuery);
//
//
//        for (Row row : results) {
//
//                topicRequest = new TopicRequest();
//                topicRequest.setUsername(row.getString("requestor"));
//                topicRequest.setAcl_ip(row.getString("acl_ip"));
//                topicRequest.setAcl_ssl(row.getString("acl_ssl"));
//                topicRequest.setTopicName(row.getString("topicname"));
//                topicRequest.setTopicpartitions(row.getString("partitions"));
//                topicRequest.setReplicationfactor(row.getString("replicationfactor"));
//                topicRequest.setAppname(row.getString("appname"));
//                topicRequest.setEnvironment(row.getString("env"));
//                topicRequest.setTeamname(row.getString("teamname"));
//                topicRequest.setRemarks(row.getString("remarks"));
//                topicRequest.setTopicstatus(row.getString("topicstatus"));
//                topicRequest.setApprovingtime("" + row.getTimestamp("exectime"));
//                topicRequest.setRequesttime("" + row.getTimestamp("requesttime"));
//
//            }

        //return topicRequest;
    }

    public List<PCStream> selectTopicStreams(String envSelected){
        PCStream pcStream = null;
        List<PCStream> pcStreams = new ArrayList();

        List<Topic> topicList = topicRepo.findAllByTopicPKEnvironment(envSelected);
        List<Acl> aclList = aclRepo.findAllByEnvironment(envSelected);

        for (Topic row : topicList) {
            LOG.info(aclList.size()+"HHHHHHHHHHHHHHHHHH"+row);
            LOG.info("&&&&&&&&&"+row.toString());
            String teamName = row.getTeamname();
            String topicName = row.getTopicPK().getTopicname();

            pcStream = new PCStream();
            List<String> prodTeams = new ArrayList<>();
            List<String> consumerTeams = new ArrayList<>();
            pcStream.setTopicName(topicName);

            prodTeams.add(teamName);
           // LOG.info("-----------"+topicName);

            for (Acl row1 : aclList) {
                if(row1!=null) {
                    LOG.info("Here...." + row1);
                    LOG.info(row1.getTopicname() + "*************************" + row1.toString());
                    String teamName1 = row1.getTeamname();

                    String topicName1 = row1.getTopicname();
                    String aclType = row1.getTopictype();
                    //  LOG.info("***-----------"+topicName1);
                    if (topicName.equals(topicName1)) {
                        LOG.info(topicName + "---" + aclType + "---" + teamName1 + "---" + teamName);
                        if (aclType.equals("Producer"))
                            prodTeams.add(teamName1);
                        else if (aclType.equals("Consumer"))
                            consumerTeams.add(teamName1);
                    }
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
        return teamRepo.findAll();
    }

    public AclRequests selectAcl(String req_no){
        return aclRequestsRepo.findById(req_no).get();
    }

    public List<UserInfo> selectAllUsersInfo(){
        return Lists.newArrayList(userInfoRepo.findAll());
    }

    public List<Env> selectAllEnvs(String type){
        return Lists.newArrayList(envRepo.findAllByType(type));
    }

    public Env selectEnvDetails(String environment){
        return envRepo.findByName(environment).get();
    }

    public UserInfo selectUserInfo(String username){
        return userInfoRepo.findByUsername(username).get();
    }

    public List<ActivityLog> selectActivityLog(String username, String env){
        List<ActivityLog> activityList ;
        UserInfo userInfo = selectUserInfo(username);
        if(userInfo.getRole().equals("SUPERUSER"))
        {
            activityList = activityLogRepo.findAllByEnv(env);
        }
        else{
            activityList = activityLogRepo.findAllByEnvAndTeam(env,userInfo.getTeam());
        }

        return activityList;
    }


    public List<Team> selectTeamsOfUsers(String username){

        List<Team> teamList = new ArrayList();
        List<UserInfo> userInfoList = Lists.newArrayList(userInfoRepo.findAll());

        List<Team> teamListSU = new ArrayList();
        List<String> superUserTeamListStr = new ArrayList();

        Team team = null;

        String teamName ;
        boolean isSuperUser = false;

        for (UserInfo row : userInfoList) {
            team = new Team();

            teamName = row.getTeam();

            superUserTeamListStr.add(teamName);

            if(username.equals(row.getUsername())) {
                String role = row.getRole();
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
