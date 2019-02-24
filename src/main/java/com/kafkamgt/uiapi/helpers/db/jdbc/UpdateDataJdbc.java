package com.kafkamgt.uiapi.helpers.db.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UpdateDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(UpdateDataJdbc.class);

    public String updateTopicRequest(String topicName, String approver, String env){
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
//        Clause eqclause = QueryBuilder.eq("userid",username);
//        Update.Where updateQuery = QueryBuilder.update(keyspace,"users")
//                .with(QueryBuilder.set("pwd", password))
//                .where(eqclause);
//        session.execute(updateQuery);
        return "success";
    }

    public String updateSchemaRequest(String topicName, String schemaVersion, String env, String approver){
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
