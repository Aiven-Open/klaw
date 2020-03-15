package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Lists;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SelectDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(SelectDataJdbc.class);

    @Autowired(required=false)
    private UserInfoRepo userInfoRepo;

    @Autowired(required=false)
    private TeamRepo teamRepo;

    @Autowired(required=false)
    private EnvRepo envRepo;

    @Autowired(required=false)
    private ActivityLogRepo activityLogRepo;

    @Autowired(required=false)
    private AclRequestsRepo aclRequestsRepo;

    @Autowired(required=false)
    private TopicRepo topicRepo;

    @Autowired(required=false)
    private AclRepo aclRepo;

    @Autowired(required=false)
    private TopicRequestsRepo topicRequestsRepo;

    @Autowired(required=false)
    private SchemaRequestRepo schemaRequestRepo;

    public SelectDataJdbc(){}
    public SelectDataJdbc(UserInfoRepo userInfoRepo, TeamRepo teamRepo,
                          EnvRepo envRepo, ActivityLogRepo activityLogRepo,
                          TopicRepo topicRepo, AclRepo aclRepo,
                          TopicRequestsRepo topicRequestsRepo, SchemaRequestRepo schemaRequestRepo,
                          AclRequestsRepo aclRequestsRepo){
        this.userInfoRepo = userInfoRepo;
        this.teamRepo = teamRepo;
        this.envRepo = envRepo;
        this.activityLogRepo = activityLogRepo;
        this.topicRepo = topicRepo;
        this.aclRepo = aclRepo;
        this.topicRequestsRepo = topicRequestsRepo;
        this.schemaRequestRepo = schemaRequestRepo;
        this.aclRequestsRepo = aclRequestsRepo;
    }

    public HashMap<String, String> getAllRequestsToBeApproved(String requestor){

        HashMap<String, String> countList = new HashMap<>();
        List<AclRequests> allAclReqs = selectAclRequests(true,requestor);
        List<SchemaRequest> allSchemaReqs = selectSchemaRequests(true,requestor);
        List<TopicRequest> allTopicReqs = selectTopicRequests(true,requestor);

        countList.put("topics",allTopicReqs.size()+"");
        countList.put("acls",allAclReqs.size()+"");
        countList.put("schemas",allSchemaReqs.size()+"");

        return countList;
    }

    public List<AclRequests> selectAclRequests(boolean allReqs, String requestor){
        List<AclRequests> aclList = new ArrayList<>();
        List<AclRequests> aclListSub ;
        if(allReqs) {
            aclListSub = aclRequestsRepo.findAllByAclstatus("created");
        }else{
            aclListSub = Lists.newArrayList(aclRequestsRepo.findAll());
        }

        for (AclRequests row : aclListSub) {
            String teamName ;
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

        List<SchemaRequest> schemaList = new ArrayList<>();
        List<SchemaRequest> schemaListSub ;

        if(allReqs) {
            schemaListSub = schemaRequestRepo.findAllByTopicstatus("created");
        }else{
            schemaListSub = Lists.newArrayList(schemaRequestRepo.findAll());
        }

        for (SchemaRequest row : schemaListSub) {
            String teamName = row.getTeamname();

            String teamSelected = selectUserInfo(requestor).getTeam();

            if (teamSelected != null && teamSelected.equals(teamName))
                schemaList.add(row);
        }

        return schemaList;
    }

    public SchemaRequest selectSchemaRequest(String topicName, String schemaVersion, String env){
        SchemaRequestPK schemaPK = new SchemaRequestPK();
        schemaPK.setEnvironment(env);
        schemaPK.setSchemaversion(schemaVersion);
        schemaPK.setTopicname(topicName);
        return schemaRequestRepo.findById(schemaPK).get();
    }

    public Topic selectTopicDetails(String topic, String env){

        Optional<Topic> topicOpt =  topicRepo.findByTopicPKEnvironmentAndTopicPKTopicname(env,topic);

        if(topicOpt.isPresent())
            return topicRepo.findByTopicPKEnvironmentAndTopicPKTopicname(env,topic).get();
        else
            return null;

    }

    public List<Topic> selectSyncTopics(String env){

        return topicRepo.findAllByTopicPKEnvironment(env);
    }

    public List<Acl> selectSyncAcls(String env){

        return aclRepo.findAllByEnvironment(env);
    }

    public List<TopicRequest> selectTopicRequests(boolean allReqs, String requestor){
        List<TopicRequest> topicRequestList = new ArrayList<>();

        List<TopicRequest> topicRequestListSub ;

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
        if(topicRequestsRepo.findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(topicName,
                env).isPresent())
            return topicRequestsRepo.findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(topicName,env).get();
        else
            return null;
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

        List<Team> teamList = new ArrayList<>();
        List<UserInfo> userInfoList = Lists.newArrayList(userInfoRepo.findAll());

        List<Team> teamListSU = new ArrayList<>();
        List<String> superUserTeamListStr = new ArrayList<>();

        Team team ;

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

    public HashMap<String, String> getDashboardInfo(){
        HashMap<String, String> dashboardInfo = new HashMap<>();

        dashboardInfo.put("teamsize", ""+teamRepo.count());
        dashboardInfo.put("users_count", ""+userInfoRepo.count());
        dashboardInfo.put("schema_clusters_count", ""+envRepo.findAllByType("schemaregistry").size());
        dashboardInfo.put("kafka_clusters_count", ""+envRepo.findAllByType("kafka").size());

        return dashboardInfo;
    }
}
