package com.kafkamgt.uiapi.helpers.db.cassandra;


import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Clause;
import com.datastax.driver.core.querybuilder.Delete;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteData {

    private static Logger LOG = LoggerFactory.getLogger(DeleteData.class);

    Session session;

    @Value("${custom.cassandradb.keyspace:@null}")
    String keyspace;

    public DeleteData(){}

    public DeleteData(Session session){
        this.session = session;
    }

    public String deleteTopicRequest(String topicName, String env){
        Clause eqclause = QueryBuilder.eq("topicname",topicName);
        Clause eqclause2 = QueryBuilder.eq("env",env);
        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"topic_requests")
                .where(eqclause)
                .and(eqclause2);
        session.execute(deleteQuery);
        return "success";
    }

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env){
        Clause eqclause1 = QueryBuilder.eq("topicname",topicName);
        Clause eqclause2 = QueryBuilder.eq("versionschema",schemaVersion);
        Clause eqclause3 = QueryBuilder.eq("env",env);
        //Clause eqclause4 = QueryBuilder.eq("topicstatus","created");
        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"schema_requests").where(eqclause1)
                .and(eqclause2)
                .and(eqclause3);
                //.and(eqclause4);
        session.execute(deleteQuery);
        return "success";
    }

    public String deleteAclRequest(String req_no){
        Clause eqclause = QueryBuilder.eq("req_no",req_no);
        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"acl_requests")
                .where(eqclause);
        session.execute(deleteQuery);
        return "success";
    }

    public String deleteClusterRequest(String clusterId){
        Clause eqclause = QueryBuilder.eq("name",clusterId);
        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"env")
                .where(eqclause);
        session.execute(deleteQuery);
        return "success";
    }

    public String deleteUserRequest(String userId){
        Clause eqclause = QueryBuilder.eq("userid",userId);
        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"users")
                .where(eqclause);
        session.execute(deleteQuery);
        return "success";
    }

    public String deleteTeamRequest(String teamId){

        Clause eqclause = QueryBuilder.eq("team",teamId);
        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"teams")
                .where(eqclause);
        session.execute(deleteQuery);
        return "success";
    }

    public String deletePrevAclRecs(List<Acl> aclReqs){

        for(Acl aclReq: aclReqs){
                String reqNo = aclReq.getReq_no();
                LOG.info("SELECT Query done.."+reqNo);
                Clause eqclause6 = QueryBuilder.eq("req_no", reqNo);
                Delete.Where delQuery = QueryBuilder.delete().all().from(keyspace,"acls")
                        .where(eqclause6);
                session.execute(delQuery);
            }

        return "success";
    }
}
