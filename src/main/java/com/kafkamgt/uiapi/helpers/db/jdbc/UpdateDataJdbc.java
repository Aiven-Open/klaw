package com.kafkamgt.uiapi.helpers.db.jdbc;

import com.kafkamgt.uiapi.entities.AclRequests;
import com.kafkamgt.uiapi.entities.SchemaRequest;
import com.kafkamgt.uiapi.entities.TopicRequest;
import com.kafkamgt.uiapi.entities.UserInfo;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.AclRequestsRepo;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.SchemaRequestRepo;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.TopicRequestsRepo;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.UserInfoRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

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

    public String updateTopicRequest(String topicName, String approver, String env){
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setTopicname(topicName);
        topicRequest.setApprover(approver);
        topicRequest.setTopicstatus("approved");
        topicRequest.setEnvironment(env);
        topicRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));
        topicRequestsRepo.save(topicRequest);
//        Clause eqclause = QueryBuilder.eq("topicname",topicName);
//        Update.Where updateQuery = QueryBuilder.update(keyspace,"topic_requests")
//                .with(QueryBuilder.set("topicstatus", "approved"))
//                .and(QueryBuilder.set("approver", approver))
//                .and(QueryBuilder.set("exectime", new Date()))
//                .where(eqclause);
//        session.execute(updateQuery);
        return "success";
    }

    public String updateAclRequest(String req_no, String approver){
        AclRequests aclRequests = new AclRequests();
        aclRequests.setReq_no(req_no);
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

        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setTopicname(topicName);
        schemaRequest.setSchemaversion(schemaVersion);
        schemaRequest.setEnvironment(env);
        schemaRequest.setApprover(approver);
        schemaRequest.setTopicstatus("approved");
        schemaRequest.setApprovingtime(new Timestamp(System.currentTimeMillis()));

        schemaRequestRepo.save(schemaRequest);
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
