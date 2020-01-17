package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.kafkamgt.uiapi.service.UtilService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HandleDbRequestsCassandraTest {

    @Mock
    SelectData cassandraSelectHelper;

    @Mock
    InsertData cassandraInsertHelper;

    @Mock
    UpdateData cassandraUpdateHelper;

    @Mock
    DeleteData cassandraDeleteHelper;

    @Mock
    UtilService utilService;

    @Mock
    LoadDb loadDb;

    @Mock
    Cluster cluster;

    @Mock
    Session session;

    @Mock
    Session.State sessionState;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    HandleDbRequestsCassandra handleDbRequestsCassandra;

    @Before
    public void setUp() {
        handleDbRequestsCassandra = new HandleDbRequestsCassandra(cassandraSelectHelper, cassandraInsertHelper,
                cassandraUpdateHelper, cassandraDeleteHelper, loadDb, cluster, utilService);

        ReflectionTestUtils.setField(handleDbRequestsCassandra, "clusterConnPort", 9042);
        ReflectionTestUtils.setField(handleDbRequestsCassandra, "keyspace", "kafkamanagementapi");
    }

    @Test
    public void connectToDbSuccess() {
        List connectedHosts = new ArrayList<>(Arrays.asList("localhosttest"));
        ReflectionTestUtils.setField(handleDbRequestsCassandra, "clusterConnHost", "localhosttest");
        ReflectionTestUtils.setField(handleDbRequestsCassandra, "dbScriptsExecution", "auto");
        ReflectionTestUtils.setField(handleDbRequestsCassandra, "dbScriptsDropAllRecreate", "true");
        when(utilService.getCluster(anyString(), anyInt(), any())).thenReturn(cluster);
        when(cluster.connect()).thenReturn(session);
        when(cluster.connect(any())).thenReturn(session);
        when(session.getState()).thenReturn(sessionState);
        doNothing().when(loadDb).dropTables();
        doNothing().when(loadDb).insertData();
        doNothing().when(loadDb).createTables();

        when(sessionState.getConnectedHosts()).thenReturn(connectedHosts);
        handleDbRequestsCassandra.connectToDb("testlicensekey");
    }

    @Test
    public void connectToDbFailure() {
        exit.expectSystemExitWithStatus(0);
        ReflectionTestUtils.setField(handleDbRequestsCassandra, "clusterConnHost", "localhosttest");
        handleDbRequestsCassandra.connectToDb("testlicensekey");
    }

    @Test
    public void requestForTopic() {
    }

    @Test
    public void requestForAcl() {
    }

    @Test
    public void addNewUser() {
    }

    @Test
    public void addNewTeam() {
    }

    @Test
    public void addNewEnv() {
    }

    @Test
    public void requestForSchema() {
    }

    @Test
    public void addToSynctopics() {
    }

    @Test
    public void addToSyncacls() {
    }

    @Test
    public void getAllRequestsToBeApproved() {
    }

    @Test
    public void getAllTopicRequests() {
    }

    @Test
    public void getCreatedTopicRequests() {
    }

    @Test
    public void selectTopicRequestsForTopic() {
    }

    @Test
    public void getSyncTopics() {
    }

    @Test
    public void getSyncAcls() {
    }

    @Test
    public void getAllAclRequests() {
    }

    @Test
    public void getCreatedAclRequests() {
    }

    @Test
    public void getAllSchemaRequests() {
    }

    @Test
    public void getCreatedSchemaRequests() {
    }

    @Test
    public void selectSchemaRequest() {
    }

    @Test
    public void selectAllTeamsOfUsers() {
    }

    @Test
    public void selectAllTeams() {
    }

    @Test
    public void selectAllUsersInfo() {
    }

    @Test
    public void getUsersInfo() {
    }

    @Test
    public void selectAcl() {
    }

    @Test
    public void getTopicTeam() {
    }

    @Test
    public void selectTopicStreams() {
    }

    @Test
    public void selectAllKafkaEnvs() {
    }

    @Test
    public void selectAllSchemaRegEnvs() {
    }

    @Test
    public void selectEnvDetails() {
    }

    @Test
    public void selectActivityLog() {
    }

    @Test
    public void updateTopicRequest() {
    }

    @Test
    public void declineTopicRequest() {
    }

    @Test
    public void updateAclRequest() {
    }

    @Test
    public void declineAclRequest() {
    }

    @Test
    public void updateSchemaRequest() {
    }

    @Test
    public void updatePassword() {
    }

    @Test
    public void deleteTopicRequest() {
    }

    @Test
    public void deleteAclRequest() {
    }

    @Test
    public void deleteClusterRequest() {
    }

    @Test
    public void deleteUserRequest() {
    }

    @Test
    public void deleteTeamRequest() {
    }

    @Test
    public void deleteSchemaRequest() {
    }

    @Test
    public void deletePrevAclRecs() {
    }
}