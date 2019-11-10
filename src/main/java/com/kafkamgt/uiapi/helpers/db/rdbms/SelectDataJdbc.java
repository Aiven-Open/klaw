package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.google.common.collect.Lists;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SelectDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(SelectDataJdbc.class);

    @Autowired(required=false)
    UserInfoRepo userInfoRepo;

    @Autowired(required=false)
    TeamRepo teamRepo;

    @Autowired(required=false)
    EnvRepo envRepo;

    @Autowired(required=false)
    ActivityLogRepo activityLogRepo;

    @Autowired(required=false)
    AclRequestsRepo aclRequestsRepo;

    @Autowired(required=false)
    TopicRepo topicRepo;

    @Autowired(required=false)
    AclRepo aclRepo;

    @Autowired(required=false)
    TopicRequestsRepo topicRequestsRepo;

    @Autowired(required=false)
    SchemaRequestRepo schemaRequestRepo;

    public HashMap<String, String> getAllRequestsToBeApproved(String requestor){

        HashMap<String, String> countList = new HashMap<>();
        List<AclRequests> allAclReqs = selectAclRequests(true,requestor);
        List<SchemaRequest> allSchemaReqs = selectSchemaRequests(true,requestor);
        List<TopicRequest> allTopicReqs = selectTopicRequests(true,requestor);

        countList.put("topics",allTopicReqs.size()+"");
        countList.put("acls",allAclReqs.size()+"");
        countList.put("schemas",allSchemaReqs.size()+"");

        //int allOutstanding = allAclReqs.size() + allSchemaReqs.size() + allTopicReqs.size();

        return countList;
    }

    public List<AclRequests> selectAclRequests(boolean allReqs, String requestor){
        AclRequests aclReq = null;
        List<AclRequests> aclList = new ArrayList();
        List<AclRequests> aclListSub = new ArrayList();
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

        List<SchemaRequest> schemaList = new ArrayList();
        List<SchemaRequest> schemaListSub = new ArrayList();

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
        SchemaRequest schemaRequest = null;

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
        TopicRequest topicRequest = null;
        List<TopicRequest> topicRequestList = new ArrayList();

        List<TopicRequest> topicRequestListSub = new ArrayList();

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
        if(topicRequestsRepo.findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(topicName,env).isPresent())
            return topicRequestsRepo.findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(topicName,env).get();
        else
            return null;
    }

    public List<PCStream> selectTopicStreams(String envSelected){
        PCStream pcStream = null;
        List<PCStream> pcStreams = new ArrayList();

        List<Topic> topicList = topicRepo.findAllByTopicPKEnvironment(envSelected);
        List<Acl> aclList = aclRepo.findAllByEnvironment(envSelected);

        for (Topic row : topicList) {

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
                    String teamName1 = row1.getTeamname();

                    String topicName1 = row1.getTopicname();
                    String aclType = row1.getTopictype();
                    //  LOG.info("***-----------"+topicName1);
                    if (topicName.equals(topicName1)) {
            //            LOG.info(topicName + "---" + aclType + "---" + teamName1 + "---" + teamName);
                        if (aclType!=null && aclType.equals("Producer"))
                            prodTeams.add(teamName1);
                        else if (aclType!=null && aclType.equals("Consumer"))
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
