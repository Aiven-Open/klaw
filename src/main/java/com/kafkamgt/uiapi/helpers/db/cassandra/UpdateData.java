package com.kafkamgt.uiapi.helpers.db.cassandra;


import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.*;
import com.kafkamgt.uiapi.entities.Acl;
import com.kafkamgt.uiapi.entities.AclRequests;
import com.kafkamgt.uiapi.entities.Topic;
import com.kafkamgt.uiapi.entities.TopicRequest;
import com.kafkamgt.uiapi.helpers.db.jdbc.repo.AclRequestsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
public class UpdateData {

    private static Logger LOG = LoggerFactory.getLogger(UpdateData.class);

    Session session;

    @Value("${cassandradb.keyspace}")
    String keyspace;

    @Autowired
    InsertData insertDataHelper;

    @Autowired
    AclRequestsRepo aclRequestsRepo;

    public String updateTopicRequest(TopicRequest topicRequest, String approver){
        Clause eqclause = QueryBuilder.eq("topicname",topicRequest.getTopicname());
        Clause eqclause1 = QueryBuilder.eq("env",topicRequest.getEnvironment());
        Update.Where updateQuery = QueryBuilder.update(keyspace,"topic_requests")
                .with(QueryBuilder.set("topicstatus", "approved"))
                .and(QueryBuilder.set("approver", approver))
                .and(QueryBuilder.set("exectime", new Date()))
                .where(eqclause)
                .and(eqclause1);
        session.execute(updateQuery);

        // insert into SOT
        List<Topic> topics = new ArrayList<>();
        Topic topicObj = new Topic();
        copyProperties(topicRequest,topicObj);
        topics.add(topicObj);
        insertDataHelper.insertIntoTopicSOT(topics);

        Acl aclReq = new Acl();
        aclReq.setTopictype("Producer");
        aclReq.setEnvironment(topicRequest.getEnvironment());
        aclReq.setTeamname(topicRequest.getTeamname());
        aclReq.setAclssl(topicRequest.getAcl_ssl());
        aclReq.setAclip(topicRequest.getAcl_ip());
        aclReq.setTopicname(topicRequest.getTopicname());

        List<Acl> acls = new ArrayList<>();
        acls.add(aclReq);
        insertDataHelper.insertIntoAclsSOT(acls);

        return "success";
    }

    public String updateAclRequest(String req_no, String approver){
        Clause eqclause = QueryBuilder.eq("req_no",req_no);
        Update.Where updateQuery = QueryBuilder.update(keyspace,"acl_requests")
                .with(QueryBuilder.set("topicstatus", "approved"))
                .and(QueryBuilder.set("approver", approver))
                .and(QueryBuilder.set("exectime", new Date()))
                .where(eqclause);
        session.execute(updateQuery);

        // Insert to SOT

        AclRequests aclReq = aclRequestsRepo.findById(req_no).get();
        List<Acl> acls = new ArrayList<>();
        Acl aclObj = new Acl();
        copyProperties(aclReq,aclObj);
        aclObj.setTeamname(aclReq.getRequestingteam());
        acls.add(aclObj);
        insertDataHelper.insertIntoAclsSOT(acls);

        return "success";
    }

    public String updatePassword(String username, String password){
        Clause eqclause = QueryBuilder.eq("userid",username);
        Update.Where updateQuery = QueryBuilder.update(keyspace,"users")
                .with(QueryBuilder.set("pwd", password))
                .where(eqclause);
        session.execute(updateQuery);
        return "success";
    }

    public String updateSchemaRequest(String topicName, String schemaVersion, String env, String approver){
        Clause eqclause1 = QueryBuilder.eq("topicname",topicName);
        Clause eqclause2 = QueryBuilder.eq("versionschema",schemaVersion);
        Clause eqclause3 = QueryBuilder.eq("env",env);
        Update.Where updateQuery = QueryBuilder.update(keyspace,"schema_requests")
                .with(QueryBuilder.set("topicstatus", "approved"))
                .and(QueryBuilder.set("approver", approver))
                .and(QueryBuilder.set("exectime", new Date()))
                .where(eqclause1)
                .and(eqclause2)
                .and(eqclause3);
        session.execute(updateQuery);
        return "success";
    }


}
