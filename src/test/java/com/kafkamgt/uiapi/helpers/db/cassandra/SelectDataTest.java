package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Select;
import com.kafkamgt.uiapi.dao.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SelectDataTest {

    @Mock
    Session session;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Row row;

    SelectData selectData;

    @Before
    public void setUp() throws Exception {
        selectData = new SelectData(session);
    }

    @Test
    public void getAllRequestsToBeApproved() {
        String requestor = "uiuser1";

        List<Row> rowList = new ArrayList<>();

        rowList.add(row);
        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet,
                resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator(),
                rowList.iterator(), rowList.iterator(), rowList.iterator(), rowList.iterator());
        when(row.getString("teamname")).thenReturn("Team1", "Team1", "Team1");
        when(row.getString("team")).thenReturn("Team1", "Team1");
        when(row.getString("requestingteam")).thenReturn("Team1", "Team1", "Team1");
        when(row.getString("userid")).thenReturn(requestor, requestor, requestor);

        when(row.getString("acltype")).thenReturn("Delete", "Create", "Delete");
        when(row.getTimestamp("exectime")).thenReturn(new Date(), new Date());
        when(row.getTimestamp("requesttime")).thenReturn(new Date(), new Date());

        HashMap<String, String> hashMap = selectData.getAllRequestsToBeApproved(requestor,"");

        assertEquals("1", hashMap.get("acls"));
        assertEquals("1", hashMap.get("schemas") );
        assertEquals("1", hashMap.get("topics"));
    }

    @Test
    public void selectAclRequests() {
        String requestor = "uiuser1";

        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator(),
                rowList.iterator(), rowList.iterator());
        when(row.getString("requestingteam")).thenReturn("Team1", "Team1");
        when(row.getString("userid")).thenReturn(requestor, requestor);
        when(row.getString("team")).thenReturn("Team1", "Team1");
        when(row.getTimestamp("exectime")).thenReturn(new Date(), new Date());
        when(row.getTimestamp("requesttime")).thenReturn(new Date(), new Date());

        List<AclRequests> listReqs = selectData.selectAclRequests(false, requestor,"");
        assertEquals(2, listReqs.size());
    }

    @Test
    public void selectSchemaRequests() {
        String requestor = "uiuser1";

        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator(),
                rowList.iterator(), rowList.iterator());
        when(row.getString("teamname")).thenReturn("Team1", "Team1");
        when(row.getString("userid")).thenReturn(requestor, requestor);
        when(row.getString("team")).thenReturn("Team1", "Team1");
        when(row.getTimestamp("exectime")).thenReturn(new Date(), new Date());
        when(row.getTimestamp("requesttime")).thenReturn(new Date(), new Date());

        List<SchemaRequest> listReqs = selectData.selectSchemaRequests(false, requestor);
        assertEquals(2, listReqs.size());
    }

    @Test
    public void selectSchemaRequest() {
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        String topicName = "testtopic";
        String schemaVersion = "1.0";
        String envSel = "DEV";

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("topicname")).thenReturn(topicName);
        when(row.getTimestamp("exectime")).thenReturn(new Date());
        when(row.getTimestamp("requesttime")).thenReturn(new Date());

        SchemaRequest schemaRequest = selectData.selectSchemaRequest(topicName, schemaVersion, envSel);
        assertEquals(topicName, schemaRequest.getTopicname());
    }

    @Test
    public void selectTopicDetails() {
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        String topicName = "testtopic";
        String envSel = "DEV";

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("teamname")).thenReturn("Team1");

        List<Topic> topic = selectData.selectTopicDetails(topicName);
        assertEquals("Team1", topic.get(0).getTeamname());
    }

    @Test
    public void selectSyncTopics() {
        String topicName = "testtopic";
        String envSel = "DEV";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("topicname")).thenReturn(topicName, topicName);
        when(row.getString("teamname")).thenReturn("Team1");

        List<Topic> topicList = selectData.selectSyncTopics(envSel, null);
        assertEquals(1, topicList.size());
        assertEquals(topicName, topicList.get(0).getTopicname());
        assertEquals("Team1", topicList.get(0).getTeamname());
    }

    @Test
    public void selectSyncAcls() {
        String topicName = "testtopic";
        String envSel = "DEV";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("topicname")).thenReturn(topicName);
        when(row.getString("teamname")).thenReturn("Team1");
        when(row.getString("consumergroup")).thenReturn("testgroup");
        when(row.getString("acl_ip")).thenReturn("12.133.122.21");

        List<Acl> topicList = selectData.selectSyncAcls(envSel);
        assertEquals(1, topicList.size());
        assertEquals(topicName, topicList.get(0).getTopicname());
        assertEquals("Team1", topicList.get(0).getTeamname());
        assertEquals("testgroup", topicList.get(0).getConsumergroup());
        assertEquals("12.133.122.21", topicList.get(0).getAclip());
    }

    @Test
    public void selectTopicRequests() {
        String requestor = "uiuser1";

        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator(),
                rowList.iterator(), rowList.iterator());
        when(row.getString("teamname")).thenReturn("Team1", "Team1");

        when(row.getString("team")).thenReturn("Team1", "Team1");

        List<TopicRequest> listReqs = selectData.selectTopicRequests(false, requestor);
        assertEquals(2, listReqs.size());
    }

    @Test
    public void selectTopicRequestsForTopic() {
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        String topicName = "testtopic";
        String envSel = "DEV";

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("topicname")).thenReturn(topicName);
        when(row.getString("teamname")).thenReturn("Team1");
        when(row.getString("acl_ip")).thenReturn("12.133.122.21");
        when(row.getTimestamp("exectime")).thenReturn(new Date());
        when(row.getTimestamp("requesttime")).thenReturn(new Date());

        TopicRequest topicRequest = selectData.selectTopicRequestsForTopic(topicName, envSel);
        assertEquals(topicName, topicRequest.getTopicname());
        assertEquals("Team1", topicRequest.getTeamname());
        assertEquals("12.133.122.21", topicRequest.getAcl_ip());
    }

    @Test
    public void selectAllTeams() {
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("team")).thenReturn("Team1", "Team2");
        when(row.getString("contactperson")).thenReturn("John", "Wilson");

        List<Team> listTeams = selectData.selectAllTeams();
        assertEquals(2, listTeams.size());
        assertEquals("Team2", listTeams.get(1).getTeamname());
    }

    @Test
    public void selectAcl() {
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        String topicName = "testtopic";
        String reqNo = "sf34FFu";

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("topicname")).thenReturn(topicName);
        when(row.getString("consumergroup")).thenReturn("testgroup");
        when(row.getString("acl_ip")).thenReturn("12.133.122.21");

        AclRequests aclRequests = selectData.selectAcl(reqNo);
        assertEquals(topicName, aclRequests.getTopicname());
        assertEquals("testgroup", aclRequests.getConsumergroup());
        assertEquals("12.133.122.21", aclRequests.getAcl_ip());
    }

    @Test
    public void selectAllUsers() {
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("userid")).thenReturn("uiuser1", "uiuser2");
        when(row.getString("team")).thenReturn("Team1", "Team2");

        List<Map<String,String>> listUsers = selectData.selectAllUsers();
        assertEquals(2, listUsers.size());
        assertEquals("Team1", listUsers.get(0).get("uiuser1"));
        assertEquals("Team2", listUsers.get(1).get("uiuser2"));
    }

    @Test
    public void selectAllUsersInfo() {
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("team")).thenReturn("Team1", "Team2");
        when(row.getString("userid")).thenReturn("uiuser1", "uiuser2");
        when(row.getString("pwd")).thenReturn("pwd1", "pwd2");

        List<UserInfo> listUserInfo = selectData.selectAllUsersInfo();
        assertEquals(2, listUserInfo.size());
        assertEquals("uiuser2", listUserInfo.get(1).getUsername());
        assertEquals("pwd1", listUserInfo.get(0).getPwd());
    }

    @Test
    public void selectAllEnvs() {
        String clusterType = "kafka";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet, resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("name")).thenReturn("DEV", "TST");
        when(row.getString("host")).thenReturn("localhost1", "localhost2");
        when(row.getString("type")).thenReturn(clusterType, clusterType, "schemaregistry", "schemaregistry");

        List<Env> listEnv = selectData.selectAllEnvs(clusterType);
        assertEquals(1, listEnv.size());
        assertEquals("localhost1", listEnv.get(0).getHost());
        assertEquals("DEV", listEnv.get(0).getName());
    }

    @Test
    public void selectEnvDetails() {
        String envSel = "DEV";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        String topicName = "testtopic";

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("name")).thenReturn("DEV" );
        when(row.getString("host")).thenReturn("localhost1");
        when(row.getString("type")).thenReturn("kafka");

        Env env = selectData.selectEnvDetails(envSel);
        assertEquals(envSel, env.getName());
        assertEquals("localhost1", env.getHost());
        assertEquals("kafka", env.getType());
    }

    @Test
    public void selectUserInfo() {
        String username = "uiuser1";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("team")).thenReturn("Team1" );
        when(row.getString("roleid")).thenReturn("ADMIN");
        when(row.getString("fullname")).thenReturn("Firstname lastname");

        UserInfo userInfo = selectData.selectUserInfo(username);
        assertEquals(username, userInfo.getUsername());
        assertEquals("Team1", userInfo.getTeam());
        assertEquals("ADMIN", userInfo.getRole());
    }

    @Test
    public void selectTeamsOfUsersOtherUser() {
        String username = "uiuser1";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("team")).thenReturn("Team1");
        when(row.getString("userid")).thenReturn(username);
        when(row.getString("roleid")).thenReturn("USER");

        List<Team> listTeams = selectData.selectTeamsOfUsers(username);
        assertEquals(2, listTeams.size());
        assertEquals("Team1", listTeams.get(0).getTeamname());
    }

    @Test
    public void selectTeamsOfUsersSuperUser1() {
        String username = "uiuser1";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("team")).thenReturn("Team1","Team2","Team1", "Team2");
        when(row.getString("userid")).thenReturn(username, username);
        when(row.getString("roleid")).thenReturn("SUPERUSER", "SUPERUSER");

        List<Team> listTeams = selectData.selectTeamsOfUsers(username);
        assertEquals(1, listTeams.size());
        assertEquals("Team2", listTeams.get(0).getTeamname());
    }

    @Test
    public void selectTeamsOfUsersSuperUser2() {
        String username = "uiuser1";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("team")).thenReturn("Team1", "Team1");
        when(row.getString("userid")).thenReturn(username, username);
        when(row.getString("roleid")).thenReturn("SUPERUSER", "SUPERUSER");

        List<Team> listTeams = selectData.selectTeamsOfUsers(username);
        assertEquals(1, listTeams.size());
        assertEquals("Team1", listTeams.get(0).getTeamname());
    }

    @Test
    public void selectActivityLog() {
        String username = "uiuser1", envSel = "DEV";
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet, resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator(), rowList.iterator());
        when(row.getString("team")).thenReturn("Team1");
        when(row.getString("roleid")).thenReturn("SUPERUSER");

        when(row.getString("activityname")).thenReturn("topic");
        when(row.getString("activitytype")).thenReturn("new");

        List<ActivityLog> listActivityLog = selectData.selectActivityLog(username, envSel);
        assertEquals(2, listActivityLog.size());
    }
}