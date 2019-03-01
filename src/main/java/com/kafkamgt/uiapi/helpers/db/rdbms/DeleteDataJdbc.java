package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.google.common.collect.Lists;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.AclRepo;
import com.kafkamgt.uiapi.repository.AclRequestsRepo;
import com.kafkamgt.uiapi.repository.SchemaRequestRepo;
import com.kafkamgt.uiapi.repository.TopicRequestsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

@Configuration
public class DeleteDataJdbc {

    private static Logger LOG = LoggerFactory.getLogger(DeleteDataJdbc.class);

    @Autowired(required=false)
    TopicRequestsRepo topicRequestsRepo;

    @Autowired(required=false)
    SchemaRequestRepo schemaRequestRepo;

    @Autowired(required=false)
    AclRequestsRepo aclRequestsRepo;

    @Autowired(required=false)
    AclRepo aclRepo;

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

    public String deletePrevAclRecs(List<Acl> aclsToBeDeleted){

        List<Acl> allAcls = Lists.newArrayList(aclRepo.findAll());

        for(Acl aclToBeDeleted :aclsToBeDeleted){
            for(Acl allAcl: allAcls) {
                if (aclToBeDeleted.getTopicname().equals(allAcl.getTopicname()) &&
                        aclToBeDeleted.getTopictype().equals(allAcl.getTopictype()) &&
                        aclToBeDeleted.getConsumergroup().equals(allAcl.getConsumergroup()) &&
                        aclToBeDeleted.getEnvironment().equals(allAcl.getEnvironment()) &&
                        aclToBeDeleted.getAclip().equals(allAcl.getAclip()) &&
                        aclToBeDeleted.getAclssl().equals(allAcl.getAclssl()))
                {
                    LOG.info("acl to be deleted" + allAcl);
                    aclRepo.delete(allAcl);
                    break;
                }
            }
        }


        return "success";
    }

}
