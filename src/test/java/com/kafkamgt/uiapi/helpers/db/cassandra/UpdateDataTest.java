package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Update;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.dao.TopicRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;


import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataTest {

    @Mock
    private Session session;

    @Mock
    private InsertData insertDataHelper;

    @Mock
    private DeleteData deleteDataHelper;

    @Mock
    private ResultSet resultSet;

    private UpdateData updateData;

    private UtilMethods utilMethods;

    @Before
    public void setUp() {
        updateData = new UpdateData(session, insertDataHelper);
        utilMethods = new UtilMethods();
        ReflectionTestUtils.setField(updateData, "deleteDataHelper", deleteDataHelper);
    }

    @Test
    public void declineTopicRequest() {
        String approver = "uiuser2";
        TopicRequest topicRequest = utilMethods.getTopicRequest("testtopic");
        when(session.execute((Update.Where) any())).thenReturn(resultSet);

        String result = updateData.declineTopicRequest(topicRequest, approver);
        assertEquals("success", result);
    }

    @Test
    public void updateTopicRequestSuccess() {
        String approver = "uiuser2";

        TopicRequest topicRequest = utilMethods.getTopicRequest("testtopic");
        when(insertDataHelper.insertIntoTopicSOT(any(), eq(false)))
                .thenReturn("success");

        String result = updateData.updateTopicRequest(topicRequest, approver);
        assertEquals("success", result);
    }

    @Test
    public void updateTopicRequestFailure() {
        String approver = "uiuser2";

        TopicRequest topicRequest = utilMethods.getTopicRequest("testtopic");
        when(insertDataHelper.insertIntoTopicSOT(any(), eq(false)))
                .thenReturn("failure");

        String result = updateData.updateTopicRequest(topicRequest, approver);
        assertEquals("success", result);
    }

    @Test
    public void updateAclRequest() {
        String approver = "uiuser2";
        AclRequests aclReq = utilMethods.getAclRequest("testtopic");

        when(deleteDataHelper.deletePrevAclRecs(any()))
                .thenReturn("success");
        String result = updateData.updateAclRequest(aclReq, approver);
        assertEquals("success", result);
    }

    @Test
    public void updateAclRequest1() {
        String approver = "uiuser2";
        AclRequests aclReq = utilMethods.getAclRequestCreate("testtopic");
        when(insertDataHelper.insertIntoAclsSOT(any(), eq(false)))
                .thenReturn("success");

        String result = updateData.updateAclRequest(aclReq, approver);
        assertEquals("success", result);
    }

    @Test
    public void declineAclRequest() {
        String approver = "uiuser2";
        AclRequests aclReq = utilMethods.getAclRequest("testtopic");
        String result = updateData.declineAclRequest(aclReq, approver);
        assertEquals("success", result);
    }

    @Test
    public void updatePassword() {
        String result = updateData.updatePassword("uiuser1", "passwd");
        assertEquals("success", result);
    }

    @Test
    public void updateSchemaRequest() {
        String approver = "uiuser2";
        SchemaRequest schemaRequest = utilMethods.getSchemaRequests().get(0);
        when(insertDataHelper.insertIntoMessageSchemaSOT(any()))
                .thenReturn("success");

        String result = updateData.updateSchemaRequest(schemaRequest, approver);
        assertEquals("success", result);
    }
}