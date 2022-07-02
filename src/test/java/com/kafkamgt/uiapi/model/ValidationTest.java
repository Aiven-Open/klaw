package com.kafkamgt.uiapi.model;

import com.kafkamgt.uiapi.dao.*;
import org.junit.Test;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import static org.junit.Assert.assertNotNull;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ValidationTest {

    @Test
    public void testNewTopicRequest(){
        TopicRequest topicRequest = new TopicRequest();
//        topicRequest.setAcl_ip("10.1.1.1");
        topicRequest.setAppname("newapp");
        topicRequest.setEnvironment("dev");
        topicRequest.setReplicationfactor("1");
        topicRequest.setPossibleTeams(new ArrayList<String>());
        topicRequest.setTotalNoPages("1");
        ArrayList<String> pageList = new ArrayList();
        pageList.add("1");
        topicRequest.setAllPageNos(pageList);

        topicRequest.setTopicname("newtopic");
        topicRequest.setEnvironment("dev");

        assertNotNull(topicRequest);
    }

    @Test
    public void testNewAclRequest(){
        AclRequests aclRequests = new AclRequests();
        aclRequests.setAcl_ip("10.1.1.1");
        aclRequests.setAppname("newapp");
        aclRequests.setEnvironment("dev");
        aclRequests.setReq_no(1001);
        aclRequests.setRequestingteam(1);
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
        schemaRequest.setTeamId(3);
        schemaRequest.setSchemafull("{type:string}");
        schemaRequest.setSchemaversion("1.0");
        schemaRequest.setEnvironment("dev");

        assertNotNull(schemaRequest);
    }
}
