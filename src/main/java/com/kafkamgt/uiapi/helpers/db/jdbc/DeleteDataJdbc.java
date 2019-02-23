package com.kafkamgt.uiapi.helpers.db.jdbc;

import com.kafkamgt.uiapi.entities.AclRequests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(DeleteDataJdbc.class);

    public String deleteTopicRequest(String topicName){
//        Clause eqclause = QueryBuilder.eq("topicname",topicName);
//        Clause eqclause2 = QueryBuilder.eq("topicstatus","created");
//        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"topic_requests")
//                .where(eqclause)
//                .and(eqclause2);
//        session.execute(deleteQuery);
        return "success";
    }

    public String deleteSchemaRequest(String topicName, String schemaVersion, String env){
//        Clause eqclause1 = QueryBuilder.eq("topicname",topicName);
//        Clause eqclause2 = QueryBuilder.eq("versionschema",schemaVersion);
//        Clause eqclause3 = QueryBuilder.eq("env",env);
//        Clause eqclause4 = QueryBuilder.eq("topicstatus","created");
//        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"schema_requests").where(eqclause1)
//                .and(eqclause2)
//                .and(eqclause3)
//                .and(eqclause4);
//        session.execute(deleteQuery);
        return "success";
    }

    public String deleteAclRequest(String req_no){
//        LOG.info("In delete acl req "+req_no);
//        Clause eqclause = QueryBuilder.eq("req_no",req_no);
//        Clause eqclause2 = QueryBuilder.eq("topicstatus","created");
//        Delete.Where deleteQuery = QueryBuilder.delete().all().from(keyspace,"acl_requests")
//                .where(eqclause)
//                .and(eqclause2);
//        session.execute(deleteQuery);
        return "success";
    }

    public String deletePrevAclRecs(List<AclRequests> aclReqs){


//        for(Acls aclReq:aclReqs){
//            String aclType = aclReq.getTopictype();
//            String host = aclReq.getAcl_ip();
//            String principle = aclReq.getAcl_ssl();
//            String consumergroup = aclReq.getConsumergroup();
//            String topicName = aclReq.getTopicname();
//
//
//            Clause eqclause = QueryBuilder.eq("topictype",aclType);
//            Clause eqclause2 = QueryBuilder.eq("acl_ip",host);
//            Clause eqclause3 = QueryBuilder.eq("consumergroup",consumergroup);
//            Clause eqclause4 = QueryBuilder.eq("acl_ssl",principle);
//            Clause eqclause5 = QueryBuilder.eq("topicname",topicName);
//            Select selQuery = QueryBuilder.select("req_no").from(keyspace,"acls")
//                    .where(eqclause)
//                    .and(eqclause2)
//                    .and(eqclause3)
//                    .and(eqclause4)
//                    .and(eqclause5).allowFiltering();
//
//            ResultSet res = session.execute(selQuery);
//
//
//            for (Row row : res) {
//                String reqNo = row.getString("req_no");
//                LOG.info("SELECT Query done.."+reqNo);
//                Clause eqclause6 = QueryBuilder.eq("req_no",reqNo);
//                Delete.Where delQuery = QueryBuilder.delete().all().from(keyspace,"acls")
//                        .where(eqclause6);
//                session.execute(delQuery);
//            }
//
//
//
//        }

        return "success";
    }

}
