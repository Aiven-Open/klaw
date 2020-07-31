package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class InsertDataJdbc {

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

    @Autowired(required=false)
    private MessageSchemaRepo messageSchemaRepo;

    @Autowired(required=false)
    private ProductDetailsRepo productDetailsRepo;

    @Autowired
    private SelectDataJdbc jdbcSelectHelper;

    public InsertDataJdbc(){}
    public InsertDataJdbc(UserInfoRepo userInfoRepo, TeamRepo teamRepo,
                          EnvRepo envRepo, ActivityLogRepo activityLogRepo,
                          TopicRepo topicRepo, AclRepo aclRepo,
                          TopicRequestsRepo topicRequestsRepo, SchemaRequestRepo schemaRequestRepo,
                          AclRequestsRepo aclRequestsRepo, MessageSchemaRepo messageSchemaRepo,
                          SelectDataJdbc jdbcSelectHelper){
        this.userInfoRepo = userInfoRepo;
        this.teamRepo = teamRepo;
        this.envRepo = envRepo;
        this.activityLogRepo = activityLogRepo;
        this.topicRepo = topicRepo;
        this.aclRepo = aclRepo;
        this.topicRequestsRepo = topicRequestsRepo;
        this.schemaRequestRepo = schemaRequestRepo;
        this.aclRequestsRepo = aclRequestsRepo;
        this.messageSchemaRepo = messageSchemaRepo;
        this.jdbcSelectHelper = jdbcSelectHelper;
    }

    private String getRandom(){
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
        topicRequest.setRequesttime((new Timestamp(System.currentTimeMillis())));
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

        if(insertIntoActivityLog(activityLog).equals("success"))
            return "success";
        else
            return "failure";
    }

    public String insertIntoTopicSOT(List<Topic> topics, boolean isSyncTopics){

        topics.forEach(topic->
                {
                    topicRepo.save(topic);
                }
        );

        return "success";
    }

    private String insertIntoActivityLog(ActivityLog activityLog){

        activityLogRepo.save(activityLog);

        return "success";
    }

    String insertIntoRequestAcl(AclRequests aclReq){

        if(aclReq.getAclType() !=null && aclReq.getAclType().equals("Create")){
            aclReq.setReq_no(getRandom());
        }

        aclReq.setAclstatus("created");
        aclReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
        aclReq.setRequestingteam(jdbcSelectHelper.selectUserInfo(aclReq.getUsername()).getTeam());
        aclRequestsRepo.save(aclReq);

        UserInfo userInfo = jdbcSelectHelper.selectUserInfo(aclReq.getUsername());

        ActivityLog activityLog = new ActivityLog();
        activityLog.setReq_no(getRandom());
        activityLog.setActivityName("AclRequest");
        activityLog.setActivityType(aclReq.getAclType());
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

    public String insertIntoAclsSOT(List<Acl> acls, boolean isSyncAcls){

        acls.forEach(acl->{

            if(acl.getReq_no() == null || acl.getReq_no().equals("null"))
                acl.setReq_no(getRandom());
            aclRepo.save(acl);
        });
        return "success";
    }

    public String insertIntoRequestSchema(SchemaRequest schemaRequest){

        SchemaRequestPK schemaRequestPK = new SchemaRequestPK();
        schemaRequestPK.setEnvironment(schemaRequest.getEnvironment());
        schemaRequestPK.setSchemaversion(schemaRequest.getSchemaversion());
        schemaRequestPK.setTopicname(schemaRequest.getTopicname());
        schemaRequest.setSchemaRequestPK(schemaRequestPK);

        schemaRequest.setSchemafull(schemaRequest.getSchemafull().trim());

        schemaRequest.setTopicstatus("created");
        schemaRequest.setRequesttime(new Timestamp(System.currentTimeMillis()));
        schemaRequest.setTeamname(jdbcSelectHelper.selectUserInfo(schemaRequest.getUsername()).getTeam());

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

        return "success";
    }

    public String insertIntoMessageSchemaSOT(List<MessageSchema> schemas){

        for(MessageSchema mSchema : schemas){
            MessageSchemaPK messageSchemaPK = new MessageSchemaPK();
            messageSchemaPK.setEnvironment(mSchema.getEnvironment());
            messageSchemaPK.setSchemaversion(mSchema.getSchemaversion());
            messageSchemaPK.setTopicname(mSchema.getTopicname());


            mSchema.setMessageSchemaPK(messageSchemaPK);
            messageSchemaRepo.save(mSchema);
        };
        return "success";
    }

    public String insertIntoUsers(UserInfo userInfo){
        Optional<UserInfo> userExists = userInfoRepo.findById(userInfo.getUsername());
        if(userExists.isPresent())
            return "Failure. User already exists";

        userInfoRepo.save(userInfo);
        return "success";
    }

    public String insertIntoTeams(Team team){
        TeamPK teamPK = new TeamPK();
        teamPK.setTeamname(team.getTeamname());
        team.setTeamPK(teamPK);

        Optional<Team> teamExists = teamRepo.findById(teamPK);
        if(teamExists.isPresent())
            return "Failure. Team already exists";

        team.setApp("");
        teamRepo.save(team);
        return "success";
    }

    public String insertIntoEnvs(Env env){
        Optional<Env> envExists = envRepo.findById(env.getName());
        if(envExists.isPresent())
            return "Failure. Cluster already exists";

        envRepo.save(env);
        return "success";
    }

}
