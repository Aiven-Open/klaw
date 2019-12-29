package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SelectDataJdbcTest {

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
    private TopicRequestsRepo topicRequestsRepo;

    @Mock
    private SchemaRequestRepo schemaRequestRepo;

    SelectDataJdbc selectDataJdbc;

    @Before
    public void setUp() throws Exception {
        selectDataJdbc = new SelectDataJdbc(userInfoRepo, teamRepo,
                envRepo, activityLogRepo,
                topicRepo, aclRepo,
                topicRequestsRepo, schemaRequestRepo);
    }

    @Test
    public void getAllRequestsToBeApproved() {
    }

    @Test
    public void selectAclRequests() {
    }

    @Test
    public void selectSchemaRequests() {
    }

    @Test
    public void selectSchemaRequest() {
    }

    @Test
    public void selectTopicDetails() {
    }

    @Test
    public void selectSyncTopics() {
    }

    @Test
    public void selectSyncAcls() {
    }

    @Test
    public void selectTopicRequests() {
    }

    @Test
    public void selectTopicRequestsForTopic() {
    }

    @Test
    public void selectAllTeams() {
    }

    @Test
    public void selectAcl() {
    }

    @Test
    public void selectAllUsersInfo() {
    }

    @Test
    public void selectAllEnvs() {
    }

    @Test
    public void selectEnvDetails() {
    }

    @Test
    public void selectUserInfo() {
    }

    @Test
    public void selectActivityLog() {
    }

    @Test
    public void selectTeamsOfUsers() {
    }
}