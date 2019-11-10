package com.kafkamgt.uiapi.model;

import com.kafkamgt.uiapi.dao.*;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.assertNotNull;

public class ValidationTest {

    @Test
    public void testNewTopicRequest(){
        TopicRequest topicRequest = new TopicRequest();
        topicRequest.setAcl_ip("10.1.1.1");
        topicRequest.setAppname("newapp");
        topicRequest.setEnvironment("dev");
        topicRequest.setReplicationfactor("1");
        topicRequest.setPossibleTeams(new ArrayList<String>());
        topicRequest.setTotalNoPages("1");
        ArrayList<String> pageList = new ArrayList();
        pageList.add("1");
        topicRequest.setAllPageNos(pageList);

        TopicRequestPK topicRequestPK = new TopicRequestPK();
        topicRequestPK.setTopicname("newtopic");
        topicRequestPK.setEnvironment("dev");
        topicRequest.setTopicRequestPK(topicRequestPK);

        assertNotNull(topicRequest);
    }

    @Test
    public void testNewAclRequest(){
        AclRequests aclRequests = new AclRequests();
        aclRequests.setAcl_ip("10.1.1.1");
        aclRequests.setAppname("newapp");
        aclRequests.setEnvironment("dev");
        aclRequests.setReq_no("101");
        aclRequests.setRequestingteam("team1");
        aclRequests.setApprover("user1");
        aclRequests.setTopicname("newtopic");
        aclRequests.setTopictype("producer");

        assertNotNull(aclRequests);
    }

    @Test
    public void testNewSchemaRequest(){
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setAppname("newapp");
        schemaRequest.setEnvironment("dev");
        schemaRequest.setApprover("user1");
        schemaRequest.setTopicname("newtopic");
        schemaRequest.setTeamname("team1");
        schemaRequest.setSchemafull("{type:string}");
        SchemaRequestPK schemaRequestPK = new SchemaRequestPK();
        schemaRequestPK.setTopicname("newtopic");
        schemaRequestPK.setSchemaversion("1.0");
        schemaRequestPK.setEnvironment("dev");

        schemaRequest.setSchemaRequestPK(schemaRequestPK);

        assertNotNull(schemaRequest);
    }
}
