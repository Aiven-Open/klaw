package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Optional;

@Configuration
public class DeleteDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(DeleteDataJdbc.class);

    @Autowired(required=false)
    TopicRequestsRepo topicRequestsRepo;

    @Autowired(required=false)
    private TopicRepo topicRepo;

    @Autowired(required=false)
    SchemaRequestRepo schemaRequestRepo;

    @Autowired(required = false)
    EnvRepo envRepo;

    @Autowired(required=false)
    TeamRepo teamRepo;

    @Autowired(required=false)
    AclRequestsRepo aclRequestsRepo;

    @Autowired(required=false)
    AclRepo aclRepo;

    @Autowired(required = false)
    UserInfoRepo userInfoRepo;

    public DeleteDataJdbc(){}

    public DeleteDataJdbc(TopicRequestsRepo topicRequestsRepo, SchemaRequestRepo schemaRequestRepo,
                          EnvRepo envRepo, TeamRepo teamRepo, AclRequestsRepo aclRequestsRepo,
                          AclRepo aclRepo, UserInfoRepo userInfoRepo){
        this.topicRequestsRepo = topicRequestsRepo;
        this.schemaRequestRepo = schemaRequestRepo;
        this.envRepo = envRepo;
        this.teamRepo = teamRepo;
        this.aclRepo = aclRepo;
        this.aclRequestsRepo = aclRequestsRepo;
        this.userInfoRepo = userInfoRepo;
    }

    public String deleteTopicRequest(String topicName, String env){
        TopicRequest topicRequest = new TopicRequest();
        TopicRequestPK topicRequestPK = new TopicRequestPK();

        topicRequestPK.setTopicname(topicName);
        topicRequestPK.setEnvironment(env);
        topicRequest.setTopicRequestPK(topicRequestPK);
        topicRequest.setTopicstatus("created");
        topicRequestsRepo.delete(topicRequest);

        return "success";
    }

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env){

        SchemaRequest schemaRequest = new SchemaRequest();
        SchemaRequestPK messageSchemaPK = new SchemaRequestPK();

        messageSchemaPK.setEnvironment(env);
        messageSchemaPK.setSchemaversion(schemaVersion);
        messageSchemaPK.setTopicname(topicName);

        schemaRequest.setSchemaRequestPK(messageSchemaPK);

        schemaRequestRepo.delete(schemaRequest);

        return "success";
    }

    public String deleteAclRequest(String req_no){
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no(req_no);
        aclRequestsRepo.delete(aclRequests);

        return "success";
    }

    public String deleteClusterRequest(String clusterId){
        Env env = new Env();
        env.setName(clusterId);
        envRepo.delete(env);
        return "success";
    }

    public String deleteUserRequest(String userId){
        UserInfo user = new UserInfo();
        user.setUsername(userId);
        userInfoRepo.delete(user);
        return "success";
    }

    public String deleteTeamRequest(String teamId){
        Team team = new Team();
        TeamPK teamPK = new TeamPK();
        teamPK.setTeamname(teamId);
        team.setTeamPK(teamPK);
        team.setTeamname(teamId);

        teamRepo.delete(team);
        return "success";
    }

    public String deletePrevAclRecs(List<Acl> aclsToBeDeleted){

        for(Acl aclToBeDeleted :aclsToBeDeleted){
            Optional<Acl> acl = aclRepo.findById(aclToBeDeleted.getReq_no());
            if(acl.isPresent() && acl.get().getTopicname().equals(aclToBeDeleted.getTopicname())) {
                aclRepo.delete(acl.get());
            }
        }

        return "success";
    }

}
