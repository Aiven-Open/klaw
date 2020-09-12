package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
public class UpdateDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(UpdateDataJdbc.class);

    @Autowired(required=false)
    private TopicRequestsRepo topicRequestsRepo;

    @Autowired(required=false)
    private AclRequestsRepo aclRequestsRepo;

    @Autowired(required=false)
    private UserInfoRepo userInfoRepo;

    @Autowired(required=false)
    private SchemaRequestRepo schemaRequestRepo;

    @Autowired(required=false)
    private InsertDataJdbc insertDataJdbcHelper;

    @Autowired(required=false)
    private DeleteDataJdbc deleteDataJdbcHelper;

    public UpdateDataJdbc(TopicRequestsRepo topicRequestsRepo, AclRequestsRepo aclRequestsRepo,
                          UserInfoRepo userInfoRepo, SchemaRequestRepo schemaRequestRepo,
                          InsertDataJdbc insertDataJdbcHelper){
        this.topicRequestsRepo = topicRequestsRepo;
        this.aclRequestsRepo = aclRequestsRepo;
        this.userInfoRepo = userInfoRepo;
        this.schemaRequestRepo = schemaRequestRepo;
        this.insertDataJdbcHelper = insertDataJdbcHelper;
    }

    public UpdateDataJdbc(){}

    public String declineTopicRequest(TopicRequest topicRequest, String approver){

        topicRequest.setApprover(approver);
        topicRequest.setTopicstatus("declined");
        topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        topicRequestsRepo.save(topicRequest);

        return "success";
    }

    public String updateTopicRequest(TopicRequest topicRequest, String approver){

        topicRequest.setApprover(approver);
        topicRequest.setTopicstatus("approved");
        topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        topicRequestsRepo.save(topicRequest);

        // insert into SOT
        List<Topic> topics = new ArrayList<>();

        Topic topicObj = new Topic();
        copyProperties(topicRequest,topicObj);
        topicObj.setNoOfReplcias(topicRequest.getReplicationfactor());
        topicObj.setNoOfPartitions(topicRequest.getTopicpartitions());
        TopicPK topicPK = new TopicPK();
        topicPK.setTopicname(topicObj.getTopicname());
        topicPK.setEnvironment(topicObj.getEnvironment());
        topicObj.setTopicPK(topicPK);
        topics.add(topicObj);

        insertDataJdbcHelper.insertIntoTopicSOT(topics,false);

        return "success";
    }

    public String updateAclRequest(AclRequests aclReq, String approver){
        aclReq.setApprover(approver);
        aclReq.setAclstatus("approved");
        aclReq.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        aclRequestsRepo.save(aclReq);

        List<Acl> acls = new ArrayList<>();
        Acl aclObj = new Acl();
        copyProperties(aclReq,aclObj);
        aclObj.setTeamname(aclReq.getRequestingteam());
        aclObj.setAclip(aclReq.getAcl_ip());
        aclObj.setAclssl(aclReq.getAcl_ssl());
        acls.add(aclObj);

        if(aclReq.getAclType() != null && aclReq.getAclType().equals("Create")){
            if(insertDataJdbcHelper.insertIntoAclsSOT(acls,false).equals("success"))
                return "success";
            else
                return "failure";
        }else{
            // Delete SOT
            if(deleteDataJdbcHelper.deletePrevAclRecs(acls).equals("success")){
                return "success";
            }
            else return "failure";
        }

    }

    public String declineAclRequest(AclRequests aclRequests, String approver){
        aclRequests.setApprover(approver);
        aclRequests.setAclstatus("declined");
        aclRequests.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        aclRequestsRepo.save(aclRequests);

        return "success";
    }

    public String updatePassword(String username, String password){

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPwd(password);
        userInfoRepo.save(userInfo);

        return "success";
    }

    public String updateSchemaRequest(SchemaRequest schemaRequest, String approver){

        schemaRequest.setApprover(approver);
        schemaRequest.setTopicstatus("approved");
        schemaRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));

        schemaRequestRepo.save(schemaRequest);

        // Insert to SOT
        List<MessageSchema> schemas = new ArrayList<>();
        MessageSchema schemaObj = new MessageSchema();
        copyProperties(schemaRequest,schemaObj);
        schemas.add(schemaObj);
        insertDataJdbcHelper.insertIntoMessageSchemaSOT(schemas);

        return "success";
    }


    public String updateSchemaRequestDecline(SchemaRequest schemaRequest, String approver) {
        schemaRequest.setApprover(approver);
        schemaRequest.setTopicstatus("declined");
        schemaRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));

        schemaRequestRepo.save(schemaRequest);

        return "success";
    }

}
