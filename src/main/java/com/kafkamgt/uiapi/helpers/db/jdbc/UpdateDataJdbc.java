package com.kafkamgt.uiapi.helpers.db.jdbc;

import com.kafkamgt.uiapi.entities.*;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.AclRequestsRepo;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.SchemaRequestRepo;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.TopicRequestsRepo;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.UserInfoRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
public class UpdateDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(UpdateDataJdbc.class);

    @Autowired
    TopicRequestsRepo topicRequestsRepo;

    @Autowired
    AclRequestsRepo aclRequestsRepo;

    @Autowired
    UserInfoRepo userInfoRepo;

    @Autowired
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
//        Clause eqclause = QueryBuilder.eq("topicname",topicName);
//        Update.Where updateQuery = QueryBuilder.update(keyspace,"topic_requests")
//                .with(QueryBuilder.set("topicstatus", "approved"))
//                .and(QueryBuilder.set("approver", approver))
//                .and(QueryBuilder.set("exectime", new Date()))
//                .where(eqclause);
//        session.execute(updateQuery);


        //            // insert into SOT
        List<Topic> topics = new ArrayList<>();

        Topic topicObj = new Topic();
        copyProperties(topicRequest,topicObj);
        TopicPK topicPK = new TopicPK();
        topicPK.setTopicname(topicObj.getTopicname());
        topicPK.setEnvironment(topicObj.getEnvironment());
        topicObj.setTopicPK(topicPK);
        topics.add(topicObj);
        insertDataJdbcHelper.insertIntoTopicSOT(topics);

        Acl aclReq = new Acl();
        aclReq.setTopictype("Producer");
        aclReq.setEnvironment(topicRequest.getEnvironment());
        aclReq.setTeamname(topicRequest.getTeamname());
        aclReq.setAclssl(topicRequest.getAcl_ssl());
        aclReq.setAclip(topicRequest.getAcl_ip());
        aclReq.setTopicname(topicRequest.getTopicname());

        List<Acl> acls = new ArrayList<>();
        acls.add(aclReq);
        insertDataJdbcHelper.insertIntoAclsSOT(acls);

        return "success";
    }

    public String updateAclRequest(AclRequests aclRequests, String approver){
        aclRequests.setApprover(approver);
        aclRequests.setAclstatus("approved");
        aclRequests.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        aclRequestsRepo.save(aclRequests);
//        Clause eqclause = QueryBuilder.eq("req_no",req_no);
//        Update.Where updateQuery = QueryBuilder.update(keyspace,"acl_requests")
//                .with(QueryBuilder.set("topicstatus", "approved"))
//                .and(QueryBuilder.set("approver", approver))
//                .and(QueryBuilder.set("exectime", new Date()))
//                .where(eqclause);
//        session.execute(updateQuery);

        // Insert to SOT

        //AclRequests aclReq = aclRequestsRepo.findById(req_no).get();
        List<Acl> acls = new ArrayList<>();
        Acl aclObj = new Acl();
        copyProperties(aclRequests,aclObj);
        aclObj.setTeamname(aclRequests.getRequestingteam());
        aclObj.setAclip(aclRequests.getAcl_ip());
        aclObj.setAclssl(aclRequests.getAcl_ssl());
        acls.add(aclObj);
        insertDataJdbcHelper.insertIntoAclsSOT(acls);

        return "success";
    }

    public String updatePassword(String username, String password){

        UserInfo userInfo = new UserInfo();
        userInfo.setUsername(username);
        userInfo.setPwd(password);
        userInfoRepo.save(userInfo);
//        Clause eqclause = QueryBuilder.eq("userid",username);
//        Update.Where updateQuery = QueryBuilder.update(keyspace,"users")
//                .with(QueryBuilder.set("pwd", password))
//                .where(eqclause);
//        session.execute(updateQuery);
        return "success";
    }

    public String updateSchemaRequest(String topicName, String schemaVersion, String env, String approver){

        SchemaRequestPK schemaRequestPK = new SchemaRequestPK();
        schemaRequestPK.setTopicname(topicName);
        schemaRequestPK.setEnvironment(env);
        schemaRequestPK.setSchemaversion(schemaVersion);

        Optional<SchemaRequest> schemaRequestOpt = schemaRequestRepo.findById(schemaRequestPK);

        if(schemaRequestOpt.isPresent()) {

            schemaRequestOpt.get().setTopicname(topicName);
            schemaRequestOpt.get().setSchemaversion(schemaVersion);
            schemaRequestOpt.get().setEnvironment(env);
            schemaRequestOpt.get().setApprover(approver);
            schemaRequestOpt.get().setTopicstatus("approved");
            schemaRequestOpt.get().setApprovingtime(new Timestamp(System.currentTimeMillis()));

            schemaRequestRepo.save(schemaRequestOpt.get());
        }
//        Clause eqclause1 = QueryBuilder.eq("topicname",topicName);
//        Clause eqclause2 = QueryBuilder.eq("versionschema",schemaVersion);
//        Clause eqclause3 = QueryBuilder.eq("env",env);
//        Update.Where updateQuery = QueryBuilder.update(keyspace,"schema_requests")
//                .with(QueryBuilder.set("topicstatus", "approved"))
//                .and(QueryBuilder.set("approver", approver))
//                .and(QueryBuilder.set("exectime", new Date()))
//                .where(eqclause1)
//                .and(eqclause2)
//                .and(eqclause3);
//        session.execute(updateQuery);
        return "success";
    }


}
