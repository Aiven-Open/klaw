package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.*;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.service.UtilService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InsertDataTest {

    @Mock
    private Session session;

    @Mock
    private UtilService utilService;

    @Mock
    private SelectData selectData;

    @Mock
    private ResultSet resultSet;

    @Mock
    PreparedStatement preparedStatement;

    @Mock
    private BoundStatement boundStatement;

    private InsertData insertData;

    private UtilMethods utilMethods;

    @Before
    public void setUp() throws Exception {
        insertData = new InsertData(session, selectData, utilService);
        utilMethods = new UtilMethods();
    }

    @Test
    public void initializeBoundStatements() {
        insertData.initializeBoundStatements();
        assertTrue(true); // not required
    }

    @Test
    public void insertIntoRequestTopic() {
        String topicName = "testtopic";

        TopicRequest topicRequest = utilMethods.getTopicRequest(topicName);

        when(session.execute((BoundStatement) any())).thenReturn(resultSet);
        when(selectData.selectUserInfo(topicRequest.getUsername())).thenReturn(utilMethods.getUserInfoMock());
        ReflectionTestUtils.setField(insertData, "boundStatementInsertIntoRequestTopic", boundStatement);
        ReflectionTestUtils.setField(insertData, "boundStatementInsertIntoActivityLogAcl", boundStatement);

        String result = insertData.insertIntoRequestTopic(topicRequest);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoTopicSOT1() {
        List<Topic> topicRequests = utilMethods.getTopics();
        boolean isSyncTopics = false;
        ReflectionTestUtils.setField(insertData, "boundStatementInsertIntoTopicSOT", boundStatement);
        ReflectionTestUtils.setField(insertData, "boundStatementInsertIntoAclsSOT", boundStatement);

        String result = insertData.insertIntoTopicSOT(topicRequests, isSyncTopics);
        assertEquals("success", result);

        isSyncTopics = true;
        result = insertData.insertIntoTopicSOT(topicRequests, isSyncTopics);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoActivityLogTopic() {
        String topicName = "testtopic";
        UserInfo userInfo = utilMethods.getUserInfoMock();

        TopicRequest topicRequest = utilMethods.getTopicRequest(topicName);

        when(selectData.selectUserInfo(topicRequest.getUsername())).thenReturn(userInfo);
        String result = insertData.insertIntoActivityLogTopic(topicRequest);

        assertEquals("success", result);
    }

    @Test
    public void insertIntoRequestAcl() {
        AclRequests aclReq = utilMethods.getAclRequest("testtopic");
        ReflectionTestUtils.setField(insertData, "boundStatementAclRequest",
                boundStatement);
        when(selectData.selectUserInfo(aclReq.getUsername())).thenReturn(utilMethods.getUserInfoMock());
        when(selectData.selectTeamsOfUsers(aclReq.getUsername())).thenReturn(utilMethods.getTeams());

        String result = insertData.insertIntoRequestAcl(aclReq);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoAclsSOT() {
        List<Acl> acls = utilMethods.getAcls();
        ReflectionTestUtils.setField(insertData, "boundStatementInsertAclsSOT",
                boundStatement);
        ReflectionTestUtils.setField(insertData, "boundStatementTopicsSOT",
                boundStatement);

        String result = insertData.insertIntoAclsSOT(acls, true);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoRequestSchema() {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setSchemafull("schema");
        ReflectionTestUtils.setField(insertData, "boundStatementSchemaReqs",
                boundStatement);

        String result = insertData.insertIntoRequestSchema(schemaRequest);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoMessageSchemaSOT() {
        List<MessageSchema> messageSchemas = utilMethods.getMSchemas();
        ReflectionTestUtils.setField(insertData, "boundStatementSchemas",
                boundStatement);

        String result = insertData.insertIntoMessageSchemaSOT(messageSchemas);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoUsers() {
        UserInfo userInfo = utilMethods.getUserInfoMock();
        ReflectionTestUtils.setField(insertData, "boundStatementUsers",
                boundStatement);

        String result = insertData.insertIntoUsers(userInfo);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoTeams() {
        Team team = new Team();
        ReflectionTestUtils.setField(insertData, "boundStatementTeams",
                boundStatement);

        String result = insertData.insertIntoTeams(team);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoEnvs() {
        ReflectionTestUtils.setField(insertData, "boundStatementEnvs",
                boundStatement);
        String result = insertData.insertIntoEnvs(new Env());
        assertEquals("success", result);
    }

}