package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.repository.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
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

    private SelectDataJdbc selectData;

    private UtilMethods utilMethods;

    @Before
    public void setUp() {
        selectData = new SelectDataJdbc(userInfoRepo, teamRepo,
                envRepo, activityLogRepo,
                topicRepo, aclRepo,
                topicRequestsRepo, schemaRequestRepo, aclRequestsRepo);
        utilMethods = new UtilMethods();
    }

    @Test
    public void getAllRequestsToBeApproved() {
        String requestor = "uiuser1";

        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");

        List<AclRequests> aclRequests = utilMethods.getAclRequests();
        List<SchemaRequest> schemaList = utilMethods.getSchemaRequests();
        List<TopicRequest> allTopicReqs = utilMethods.getTopicRequests();

        when(aclRequestsRepo.findAllByAclstatus("created")).thenReturn(aclRequests);
        when(userInfoRepo.findByUsername(requestor)).thenReturn(java.util.Optional.of(userInfo),
                java.util.Optional.of(userInfo), java.util.Optional.of(userInfo));
        when(schemaRequestRepo.findAllByTopicstatus("created")).thenReturn(schemaList);
        when(topicRequestsRepo.findAllByTopicstatus("created")).thenReturn(allTopicReqs);

        HashMap<String, String> hashMap = selectData.getAllRequestsToBeApproved(requestor,"");

        assertEquals("1", hashMap.get("acls"));
        assertEquals("1", hashMap.get("schemas") );
        assertEquals("1", hashMap.get("topics"));
    }

    @Test
    public void selectAclRequests() {
        String requestor = "uiuser1";
        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");

        List<AclRequests> aclRequests = utilMethods.getAclRequests();

        when(aclRequestsRepo.findAll()).thenReturn(aclRequests);
        when(userInfoRepo.findByUsername(requestor)).thenReturn(java.util.Optional.of(userInfo));

        List<AclRequests> aclRequestsActual = selectData.selectAclRequests(false, requestor,"");
        assertEquals(1, aclRequestsActual.size());
    }

    @Test
    public void selectSchemaRequests() {
        String requestor = "uiuser1";
        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");

        List<SchemaRequest> schemaRequests = utilMethods.getSchemaRequests();

        when(schemaRequestRepo.findAll()).thenReturn(schemaRequests);
        when(userInfoRepo.findByUsername(requestor)).thenReturn(java.util.Optional.of(userInfo));

        List<SchemaRequest> schemaRequestsActual = selectData.selectSchemaRequests(false, requestor);
        assertEquals(1, schemaRequestsActual.size());
    }

    @Test
    public void selectSchemaRequest() {
        SchemaRequest schemaRequest = new SchemaRequest();
        schemaRequest.setTopicname("testtopic");
        when(schemaRequestRepo.findById(utilMethods.getSchemaRequestPk())).thenReturn(java.util.Optional.of(schemaRequest));

        SchemaRequest schemaRequestActual =  selectData.selectSchemaRequest("testtopic",
                "1.0", "DEV");

        assertEquals("testtopic", schemaRequestActual.getTopicname());
    }

    @Test
    public void selectTopicDetailsSuccess() {
        String topicName = "testtopic", env = "DEV";
        List<Topic> topicList = new ArrayList<>();
        topicList.add(utilMethods.getTopic(topicName));
        when(topicRepo.findAllByTopicPKTopicname(topicName)).
                thenReturn(topicList);
        List<Topic> topic = selectData.selectTopicDetails(topicName);

        assertEquals(topicName, topic.get(0).getTopicname());
    }

    @Test
    public void selectTopicDetailsFailure() {
        String topicName = "testtopic";
        List<Topic> topicList = new ArrayList<>();
        when(topicRepo.findAllByTopicPKTopicname(topicName)).thenReturn(topicList);
        List<Topic> topic = selectData.selectTopicDetails(topicName);

        assertNull(topic);
    }

    @Test
    public void selectSyncTopics() {
        String env = "DEV";

        when(topicRepo.findAllByTopicPKEnvironment(env)).thenReturn(utilMethods.getTopics());

        List<Topic> topicList = selectData.selectSyncTopics(env,null);

        assertEquals(1, topicList.size());
    }

    @Test
    public void selectSyncAcls() {
        String env = "DEV";

        when(aclRepo.findAllByEnvironment(env)).thenReturn(utilMethods.getAcls());

        List<Acl> topicList = selectData.selectSyncAcls(env);

        assertEquals(1, topicList.size());
    }

    @Test
    public void selectTopicRequests() {
        String requestor = "uiuser1";
        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");

        List<TopicRequest> schemaRequests = utilMethods.getTopicRequests();

        when(topicRequestsRepo.findAll()).thenReturn(schemaRequests);
        when(userInfoRepo.findByUsername(requestor)).thenReturn(java.util.Optional.of(userInfo));

        List<TopicRequest> topicRequestsActual = selectData.selectTopicRequests(false, requestor);
        assertEquals(1, topicRequestsActual.size());
    }

    @Test
    public void selectTopicRequestsForTopic() {
        String topicName = "testtopic", env = "DEV";
        when(topicRequestsRepo.findByTopicRequestPKTopicnameAndTopicRequestPKEnvironment(topicName,
                env)).thenReturn(java.util.Optional.ofNullable(utilMethods.getTopicRequest(topicName)));

        TopicRequest topicRequest = selectData.selectTopicRequestsForTopic(topicName, env);
        assertEquals(topicName, topicRequest.getTopicname());
    }

    @Test
    public void selectAllTeams() {
        when(teamRepo.findAll()).thenReturn(utilMethods.getTeams());
        List<Team> teamList = selectData.selectAllTeams();

        assertEquals(1, teamList.size());
    }

    @Test
    public void selectAcl() {
        String req_no = "Fss45UG";
        when(aclRequestsRepo.findById(req_no)).thenReturn(java.util.Optional.ofNullable(utilMethods.getAclRequest("")));

        AclRequests aclRequests = selectData.selectAcl(req_no);

        assertEquals("Team1", aclRequests.getTeamname());
    }

    @Test
    public void selectAllUsersInfo() {
        String username = "uiuser1";
        when(userInfoRepo.findAll()).thenReturn(utilMethods.getUserInfoList(username, "ADMIN"));
        List<UserInfo> userInfoList = selectData.selectAllUsersInfo();

        assertEquals(1, userInfoList.size());
    }

    @Test
    public void selectActivityLog1() {
        String username = "uuser1", env = "DEV";
        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");
        userInfo.setUsername(username);
        userInfo.setRole("ADMIN");
        when(userInfoRepo.findByUsername(username)).thenReturn(java.util.Optional.of(userInfo));
        when(activityLogRepo.findAllByEnvAndTeam(env,userInfo.getTeam())).thenReturn(utilMethods.getLogs());

        List<ActivityLog> activityLogs = selectData.selectActivityLog(username, env);

        assertEquals(1, activityLogs.size());
    }

    @Test
    public void selectActivityLog2() {
        String username = "uuser1", env = "DEV";
        UserInfo userInfo = new UserInfo();
        userInfo.setTeam("Team1");
        userInfo.setUsername(username);
        userInfo.setRole("SUPERUSER");
        when(userInfoRepo.findByUsername(username)).thenReturn(java.util.Optional.of(userInfo));
        when(activityLogRepo.findAllByEnv(env)).thenReturn(utilMethods.getLogs());

        List<ActivityLog> activityLogs = selectData.selectActivityLog(username, env);

        assertEquals(1, activityLogs.size());
    }

    @Test
    public void selectTeamsOfUsers() {
        String username = "uiuser1";

        when(userInfoRepo.findAll()).thenReturn(utilMethods.getUserInfoList(username, "ADMIN"));
        when(teamRepo.findAll()).thenReturn(utilMethods.getTeams());

        List<Team> teamList = selectData.selectTeamsOfUsers(username);
        assertEquals(1, teamList.size());

        when(userInfoRepo.findAll()).thenReturn(utilMethods.getUserInfoList(username, "SUPERUSER"));

        teamList = selectData.selectTeamsOfUsers(username);
        assertEquals(1, teamList.size());
    }


}