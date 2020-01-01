package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDataJdbcTest {

    private DeleteDataJdbc deleteDataJdbc;

    @Mock
    private TopicRequestsRepo topicRequestsRepo;

    @Mock
    SchemaRequestRepo schemaRequestRepo;

    @Mock
    EnvRepo envRepo;

    @Mock
    TeamRepo teamRepo;

    @Mock
    AclRequestsRepo aclRequestsRepo;

    @Mock
    AclRepo aclRepo;

    @Mock
    UserInfoRepo userInfoRepo;

    private UtilMethods utilMethods;

    @Before
    public void setUp() throws Exception {
        deleteDataJdbc = new DeleteDataJdbc(topicRequestsRepo, schemaRequestRepo,
            envRepo, teamRepo, aclRequestsRepo,
            aclRepo, userInfoRepo);
        utilMethods = new UtilMethods();
    }

    @Test
    public void deleteTopicRequest() {
        String result = deleteDataJdbc.deleteTopicRequest("testtopic", "DEV");
        assertEquals("success", result);
    }

    @Test
    public void deleteSchemaRequest() {
        String result = deleteDataJdbc.deleteSchemaRequest("testtopic",
                "1.0", "DEV");
        assertEquals("success", result);
    }

    @Test
    public void deleteAclRequest() {
        String result = deleteDataJdbc.deleteAclRequest("das321SSRr");
        assertEquals("success", result);
    }

    @Test
    public void deleteClusterRequest() {
        String result = deleteDataJdbc.deleteClusterRequest("DEV_ID");
        assertEquals("success", result);
    }

    @Test
    public void deleteUserRequest() {
        String result = deleteDataJdbc.deleteUserRequest("uiuser1");
        assertEquals("success", result);
    }

    @Test
    public void deleteTeamRequest() {
        String result = deleteDataJdbc.deleteTeamRequest("Team1");
        assertEquals("success", result);
    }

    @Test
    public void deletePrevAclRecs() {
        List<Acl> acls = utilMethods.getAclsForDelete();
        when(aclRepo.findAll()).thenReturn(utilMethods.getAllAcls());

        String result = deleteDataJdbc.deletePrevAclRecs(acls);
        assertEquals("success", result);

    }
}