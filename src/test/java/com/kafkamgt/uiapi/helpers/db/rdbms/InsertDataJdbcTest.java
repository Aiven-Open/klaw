package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InsertDataJdbcTest {

    @Mock
    private UserInfoRepo userInfoRepo;

    @Mock
    private TeamRepo teamRepo;

    @Mock
    private EnvRepo envRepo;

    @Mock
    private ActivityLogRepo activityLogRepo;

    @Mock
    private AclRequestsRepo aclRequestsRepo;

    @Mock
    private TopicRepo topicRepo;

    @Mock
    private AclRepo aclRepo;

    @Mock
    MessageSchemaRepo messageSchemaRepo;

    @Mock
    private TopicRequestsRepo topicRequestsRepo;

    @Mock
    private SchemaRequestRepo schemaRequestRepo;

    @Mock
    SelectDataJdbc jdbcSelectHelper;

    private InsertDataJdbc insertData;

    private UtilMethods utilMethods;

    @Before
    public void setUp() {
        insertData = new InsertDataJdbc(userInfoRepo, teamRepo,
                envRepo, activityLogRepo,
                topicRepo, aclRepo,
                topicRequestsRepo, schemaRequestRepo,
                aclRequestsRepo, messageSchemaRepo, jdbcSelectHelper);
        utilMethods = new UtilMethods();
    }

    @Test
    public void insertIntoRequestTopic() {
        String topicName = "testtopic";
        UserInfo userInfo = utilMethods.getUserInfoMock();
        TopicRequest topicRequest = utilMethods.getTopicRequest(topicName);
        when(jdbcSelectHelper.selectUserInfo(topicRequest.getUsername())).thenReturn(userInfo);

        String result = insertData.insertIntoRequestTopic(topicRequest);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoTopicSOT() {
        List<Topic> topics = utilMethods.getTopics();

        String result = insertData.insertIntoTopicSOT(topics, true);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoRequestAcl() {
        when(jdbcSelectHelper.selectUserInfo("uiuser1")).thenReturn(utilMethods.getUserInfoMock());
        String result = insertData.insertIntoRequestAcl(utilMethods.getAclRequest("testtopic"));
        assertEquals("success", result);
    }

    @Test
    public void insertIntoAclsSOT() {
        List<Acl> acls = utilMethods.getAcls();
        String result = insertData.insertIntoAclsSOT(acls, true);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoRequestSchema() {
        SchemaRequest schemaRequest = utilMethods.getSchemaRequests().get(0);
        when(jdbcSelectHelper.selectUserInfo("uiuser1"))
                .thenReturn(utilMethods.getUserInfoMock(), utilMethods.getUserInfoMock());

        String result = insertData.insertIntoRequestSchema(schemaRequest);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoMessageSchemaSOT() {
        List<MessageSchema> schemas = utilMethods.getMSchemas();
        String result = insertData.insertIntoMessageSchemaSOT(schemas);
        assertEquals("success", result);
    }

    @Test
    public void insertIntoUsers() {
        String result = insertData.insertIntoUsers(utilMethods.getUserInfoMock());
        assertEquals("success", result);
    }

    @Test
    public void insertIntoTeams() {
        String result = insertData.insertIntoTeams(utilMethods.getTeams().get(0));
        assertEquals("success", result);
    }

    @Test
    public void insertIntoEnvs() {
        String result = insertData.insertIntoEnvs(new Env());
        assertEquals("success", result);
    }
}