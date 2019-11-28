package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component
public class InsertDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(InsertDataJdbc.class);

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

    @Autowired(required=false)
    MessageSchemaRepo messageSchemaRepo;

    @Autowired
    SelectDataJdbc jdbcSelectHelper;

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

        TopicRequestPK topicRequestPK = new TopicRequestPK();
        topicRequestPK.setTopicname(topicRequest.getTopicname());
        topicRequestPK.setEnvironment(topicRequest.getEnvironment());
        topicRequest.setTopicRequestPK(topicRequestPK);
        topicRequest.setTopicstatus("created");
        topicRequest.setRequesttime((new Timestamp(System.currentTimeMillis())).toString());
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

        insertIntoActivityLog(activityLog);

        return "success";
    }

    public String insertIntoTopicSOT(List<Topic> topics, boolean isSyncTopics){

        topics.forEach(topic->
                {
                    topicRepo.save(topic);
                    if(isSyncTopics) {
                        TopicRequestPK topicRequestPK = new TopicRequestPK();
                        topicRequestPK.setEnvironment(topic.getEnvironment());
                        topicRequestPK.setTopicname(topic.getTopicname());
                        Optional<TopicRequest> topicRequest = topicRequestsRepo.findById(topicRequestPK);

                            Acl acl = new Acl();
                            acl.setReq_no(getRandom());
                            acl.setTopictype("Producer");
                            acl.setTopicname(topic.getTopicname());
                            if(topicRequest.isPresent()) {
                                acl.setAclip(topicRequest.get().getAcl_ip());
                                acl.setAclssl(topicRequest.get().getAcl_ssl());
                            }
                            acl.setEnvironment(topic.getEnvironment());
                            acl.setTeamname(topic.getTeamname());

                            aclRepo.save(acl);

                    }
                }
        );

        return "success";
    }

    public String insertIntoActivityLog(ActivityLog activityLog){

        activityLogRepo.save(activityLog);

        return "success";
    }

    public String insertIntoRequestAcl(AclRequests aclReq){

        aclReq.setReq_no(getRandom());
        aclReq.setAclstatus("created");
        aclReq.setRequesttime(new Timestamp(System.currentTimeMillis()));
        aclReq.setRequestingteam(jdbcSelectHelper.selectUserInfo(aclReq.getUsername()).getTeam());
        aclRequestsRepo.save(aclReq);

        UserInfo userInfo = jdbcSelectHelper.selectUserInfo(aclReq.getUsername());

        ActivityLog activityLog = new ActivityLog();
        activityLog.setReq_no(getRandom());
        activityLog.setActivityName("AclRequest");
        activityLog.setActivityType("new");
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
            acl.setReq_no(getRandom());
            aclRepo.save(acl);

            if(isSyncAcls && acl.getTopictype().equals("Producer")){
                Topic topic = new Topic();
                TopicPK topicPK = new TopicPK();
                topicPK.setEnvironment(acl.getEnvironment());
                topicPK.setTopicname(acl.getTopicname());
                topic.setTopicPK(topicPK);
                topic.setTeamname(acl.getTeamname());

                topicRepo.save(topic);
            }
        });
        return "success";
    }

    public String insertIntoRequestSchema(SchemaRequest schemaRequest){

        SchemaRequestPK schemaRequestPK = new SchemaRequestPK();
        schemaRequestPK.setEnvironment(schemaRequest.getEnvironment());
        schemaRequestPK.setSchemaversion(schemaRequest.getSchemaversion());
        schemaRequestPK.setTopicname(schemaRequest.getTopicname());
        schemaRequest.setSchemaRequestPK(schemaRequestPK);

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
        userInfoRepo.save(userInfo);
        return "success";
    }

    public String insertIntoTeams(Team team){
        teamRepo.save(team);
        return "success";
    }

    public String insertIntoEnvs(Env env){
        envRepo.save(env);
        return "success";
    }

}
