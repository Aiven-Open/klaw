package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.repository.AclRequestsRepo;
import com.kafkamgt.uiapi.repository.SchemaRequestRepo;
import com.kafkamgt.uiapi.repository.TopicRequestsRepo;
import com.kafkamgt.uiapi.repository.UserInfoRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UpdateDataJdbcTest {

    @Mock
    private TopicRequestsRepo topicRequestsRepo;

    @Mock
    private AclRequestsRepo aclRequestsRepo;

    @Mock
    private UserInfoRepo userInfoRepo;

    @Mock
    private SchemaRequestRepo schemaRequestRepo;

    @Mock
    private InsertDataJdbc insertDataJdbcHelper;

    @Mock
    private DeleteDataJdbc deleteDataJdbcHelper;

    private UpdateDataJdbc updateData;

    private UtilMethods utilMethods;

    @Mock
    UserInfo userInfo;

    @Before
    public void setUp() throws Exception {
        updateData = new UpdateDataJdbc(topicRequestsRepo, aclRequestsRepo,
                userInfoRepo, schemaRequestRepo,
                insertDataJdbcHelper);
        utilMethods = new UtilMethods();
        ReflectionTestUtils.setField(updateData, "deleteDataJdbcHelper", deleteDataJdbcHelper);
    }

    @Test
    public void declineTopicRequest() {
        String result = updateData.declineTopicRequest(utilMethods.getTopicRequest("testtopic"),
                "uiuser2");
        assertEquals("success", result);
    }

    @Test
    public void updateTopicRequest() {
        when(insertDataJdbcHelper.insertIntoTopicSOT(any(), eq(false)))
                .thenReturn("success");

        String result = updateData.updateTopicRequest(utilMethods.getTopicRequest("testtopic"),
                "uiuser2");
        assertEquals("success", result);
    }

    @Test
    public void updateAclRequest() {
        when(insertDataJdbcHelper.insertIntoAclsSOT(any(), eq(false)))
                .thenReturn("success");
        String result = updateData.updateAclRequest(utilMethods.getAclRequestCreate("testtopic"),
                "uiuser2");
        assertEquals("success", result);
    }

    @Test
    public void updateAclRequest1() {
        when(deleteDataJdbcHelper.deletePrevAclRecs(any()))
                .thenReturn("success");
        String result = updateData.updateAclRequest(utilMethods.getAclRequest("testtopic"),
                "uiuser2");
        assertEquals("success", result);
    }

    @Test
    public void declineAclRequest() {
        String result = updateData.declineAclRequest(utilMethods.getAclRequest("testtopic"),
                "uiuser2");
        assertEquals("success", result);
    }

    @Test
    public void updatePassword() {
        String user = "uiuser1";
        when(userInfoRepo.findById(user)).thenReturn(Optional.of(userInfo));
        String result = updateData.updatePassword(user, "pwd");
        assertEquals("success", result);
    }

    @Test
    public void updateSchemaRequest() {
        String result = updateData.updateSchemaRequest(utilMethods.getSchemaRequests().get(0),
                "uiuser1");
        assertEquals("success", result);
    }
}