package com.kafkamgt.uiapi.helpers.db.cassandra;


import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Update;
import com.kafkamgt.uiapi.dao.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.beans.BeanUtils.copyProperties;

@Component
public class UpdateData {

    private static Logger LOG = LoggerFactory.getLogger(UpdateData.class);

    Session session;

    @Value("${custom.cassandradb.keyspace:@null}")
    String keyspace;

    @Autowired
    private InsertData insertDataHelper;

    public UpdateData(){}

    public UpdateData(Session session, InsertData insertDataHelper){
        this.session = session;
        this.insertDataHelper = insertDataHelper;
    }

    public String declineTopicRequest(TopicRequest topicRequest, String approver){
        Clause eqclause = QueryBuilder.eq("topicname",topicRequest.getTopicname());
        Clause eqclause1 = QueryBuilder.eq("env",topicRequest.getEnvironment());
        Update.Where updateQuery = QueryBuilder.update(keyspace,"topic_requests")
                .with(QueryBuilder.set("topicstatus", "declined"))
                .and(QueryBuilder.set("approver", approver))
                .and(QueryBuilder.set("exectime", new Date()))
                .where(eqclause)
                .and(eqclause1);
        session.execute(updateQuery);

        return "success";
    }

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

        if(insertDataHelper.insertIntoTopicSOT(topics,false).equals("success")){
            Acl aclReq = new Acl();
            aclReq.setTopictype("Producer");
            aclReq.setEnvironment(topicRequest.getEnvironment());
            aclReq.setTeamname(topicRequest.getTeamname());
            aclReq.setAclssl(topicRequest.getAcl_ssl());
            aclReq.setAclip(topicRequest.getAcl_ip());
            aclReq.setTopicname(topicRequest.getTopicname());

            List<Acl> acls = new ArrayList<>();
            acls.add(aclReq);
            insertDataHelper.insertIntoAclsSOT(acls,false);

            return "success";
        }
            return "failure";
    }

    public String updateAclRequest(AclRequests aclReq, String approver){
        Clause eqclause = QueryBuilder.eq("req_no",aclReq.getReq_no());
        Update.Where updateQuery = QueryBuilder.update(keyspace,"acl_requests")
                .with(QueryBuilder.set("topicstatus", "approved"))
                .and(QueryBuilder.set("approver", approver))
                .and(QueryBuilder.set("exectime", new Date()))
                .where(eqclause);
        session.execute(updateQuery);

        // Insert to SOT

        List<Acl> acls = new ArrayList<>();
        Acl aclObj = new Acl();
        copyProperties(aclReq,aclObj);
        aclObj.setTeamname(aclReq.getRequestingteam());
        aclObj.setAclip(aclReq.getAcl_ip());
        aclObj.setAclssl(aclReq.getAcl_ssl());
        acls.add(aclObj);
        if(insertDataHelper.insertIntoAclsSOT(acls,false).equals("success")){
            return "success";
        }else return "failure";
    }

    public String declineAclRequest(AclRequests aclReq, String approver){
        Clause eqclause = QueryBuilder.eq("req_no",aclReq.getReq_no());
        Update.Where updateQuery = QueryBuilder.update(keyspace,"acl_requests")
                .with(QueryBuilder.set("topicstatus", "declined"))
                .and(QueryBuilder.set("approver", approver))
                .and(QueryBuilder.set("exectime", new Date()))
                .where(eqclause);
        session.execute(updateQuery);
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

    public String updateSchemaRequest(SchemaRequest schemaRequest, String approver){
        Clause eqclause1 = QueryBuilder.eq("topicname",schemaRequest.getTopicname());
        Clause eqclause2 = QueryBuilder.eq("versionschema",schemaRequest.getSchemaversion());
        Clause eqclause3 = QueryBuilder.eq("env",schemaRequest.getEnvironment());
        Update.Where updateQuery = QueryBuilder.update(keyspace,"schema_requests")
                .with(QueryBuilder.set("topicstatus", "approved"))
                .and(QueryBuilder.set("approver", approver))
                .and(QueryBuilder.set("exectime", new Date()))
                .where(eqclause1)
                .and(eqclause2)
                .and(eqclause3);
        session.execute(updateQuery);

        // Insert to SOT

        List<MessageSchema> schemas = new ArrayList<>();
        MessageSchema schemaObj = new MessageSchema();
        copyProperties(schemaRequest,schemaObj);
        schemas.add(schemaObj);

        if(insertDataHelper.insertIntoMessageSchemaSOT(schemas).equals("success"))
            return "success";
        else return "failure";
    }


}
