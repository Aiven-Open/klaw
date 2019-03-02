package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.AclRequestsRepo;
import com.kafkamgt.uiapi.repository.SchemaRequestRepo;
import com.kafkamgt.uiapi.repository.TopicRequestsRepo;
import com.kafkamgt.uiapi.repository.UserInfoRepo;
import com.kafkamgt.uiapi.dao.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
public class UpdateDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(UpdateDataJdbc.class);

    @Autowired(required=false)
    TopicRequestsRepo topicRequestsRepo;

    @Autowired(required=false)
    AclRequestsRepo aclRequestsRepo;

    @Autowired(required=false)
    UserInfoRepo userInfoRepo;

    @Autowired(required=false)
    SchemaRequestRepo schemaRequestRepo;

    @Autowired
    InsertDataJdbc insertDataJdbcHelper;

    @Autowired
    SelectDataJdbc selectDataJdbcHelper;

    public String updateTopicRequest(TopicRequest topicRequest, String approver){

        topicRequest.setApprover(approver);
        topicRequest.setTopicstatus("approved");
        topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        topicRequestsRepo.save(topicRequest);

        // insert into SOT
        List<Topic> topics = new ArrayList<>();

        Topic topicObj = new Topic();
        copyProperties(topicRequest,topicObj);
        TopicPK topicPK = new TopicPK();
        topicPK.setTopicname(topicObj.getTopicname());
        topicPK.setEnvironment(topicObj.getEnvironment());
        topicObj.setTopicPK(topicPK);
        topics.add(topicObj);
        insertDataJdbcHelper.insertIntoTopicSOT(topics,false);

        Acl aclReq = new Acl();
        aclReq.setTopictype("Producer");
        aclReq.setEnvironment(topicRequest.getEnvironment());
        aclReq.setTeamname(topicRequest.getTeamname());
        aclReq.setAclssl(topicRequest.getAcl_ssl());
        aclReq.setAclip(topicRequest.getAcl_ip());
        aclReq.setTopicname(topicRequest.getTopicname());

        List<Acl> acls = new ArrayList<>();
        acls.add(aclReq);
        insertDataJdbcHelper.insertIntoAclsSOT(acls,false);

        return "success";
    }

    public String updateAclRequest(AclRequests aclRequests, String approver){
        aclRequests.setApprover(approver);
        aclRequests.setAclstatus("approved");
        aclRequests.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        aclRequestsRepo.save(aclRequests);

        // Insert to SOT

        List<Acl> acls = new ArrayList<>();
        Acl aclObj = new Acl();
        copyProperties(aclRequests,aclObj);
        aclObj.setTeamname(aclRequests.getRequestingteam());
        aclObj.setAclip(aclRequests.getAcl_ip());
        aclObj.setAclssl(aclRequests.getAcl_ssl());
        acls.add(aclObj);
        insertDataJdbcHelper.insertIntoAclsSOT(acls,false);

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


}
