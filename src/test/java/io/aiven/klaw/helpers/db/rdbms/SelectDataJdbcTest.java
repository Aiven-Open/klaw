package io.aiven.klaw.helpers.db.rdbms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.repository.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class SelectDataJdbcTest {

  @Mock private UserInfoRepo userInfoRepo;

  @Mock private TeamRepo teamRepo;

  @Mock private EnvRepo envRepo;

  @Mock private ActivityLogRepo activityLogRepo;

  @Mock private AclRequestsRepo aclRequestsRepo;

  @Mock private TopicRepo topicRepo;

  @Mock private AclRepo aclRepo;

  @Mock private RegisterInfoRepo registerInfoRepo;

  @Mock private TopicRequestsRepo topicRequestsRepo;

  @Mock private SchemaRequestRepo schemaRequestRepo;

  @Mock private KwPropertiesRepo kwPropertiesRepo;

  private SelectDataJdbc selectData;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    selectData = new SelectDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(selectData, "userInfoRepo", userInfoRepo);
    ReflectionTestUtils.setField(selectData, "topicRepo", topicRepo);
    ReflectionTestUtils.setField(selectData, "activityLogRepo", activityLogRepo);
    ReflectionTestUtils.setField(selectData, "aclRequestsRepo", aclRequestsRepo);
    ReflectionTestUtils.setField(selectData, "teamRepo", teamRepo);
    ReflectionTestUtils.setField(selectData, "aclRepo", aclRepo);
    ReflectionTestUtils.setField(selectData, "topicRequestsRepo", topicRequestsRepo);
    ReflectionTestUtils.setField(selectData, "schemaRequestRepo", schemaRequestRepo);
  }

  @Test
  public void selectAclRequests() {
    String requestor = "uiuser1";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);

    List<AclRequests> aclRequests = utilMethods.getAclRequests();

    when(aclRequestsRepo.findAllByTenantId(anyInt())).thenReturn(aclRequests);
    when(userInfoRepo.findByUsername(requestor)).thenReturn(java.util.Optional.of(userInfo));

    List<AclRequests> aclRequestsActual =
        selectData.selectAclRequests(false, requestor, "", "all", false, 1);
    assertEquals(0, aclRequestsActual.size());
  }

  @Test
  public void selectSchemaRequests() {
    String requestor = "uiuser1";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);

    List<SchemaRequest> schemaRequests = utilMethods.getSchemaRequestsDao();

    when(schemaRequestRepo.findAllByTenantId(1)).thenReturn(schemaRequests);
    when(userInfoRepo.findByUsername(requestor)).thenReturn(java.util.Optional.of(userInfo));

    List<SchemaRequest> schemaRequestsActual = selectData.selectSchemaRequests(false, requestor, 1);
    assertEquals(0, schemaRequestsActual.size());
  }

  @Test
  public void selectSchemaRequest() {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setReq_no(1001);
    schemaRequest.setTopicname("testtopic");
    when(schemaRequestRepo.findById(any())).thenReturn(java.util.Optional.of(schemaRequest));
    SchemaRequest schemaRequestActual = selectData.selectSchemaRequest(1001, 1);

    assertEquals("testtopic", schemaRequestActual.getTopicname());
  }

  @Test
  public void selectTopicDetailsSuccess() {
    String topicName = "testtopic", env = "DEV";
    List<Topic> topicList = new ArrayList<>();
    topicList.add(utilMethods.getTopic(topicName));
    when(topicRepo.findAllByTopicnameAndTenantId(topicName, 1)).thenReturn(topicList);
    List<Topic> topic = selectData.selectTopicDetails(topicName, 1);

    assertEquals(topicName, topic.get(0).getTopicname());
  }

  @Test
  public void selectTopicDetailsFailure() {
    String topicName = "testtopic";
    List<Topic> topicList = new ArrayList<>();
    when(topicRepo.findAllByTopicnameAndTenantId(topicName, 1)).thenReturn(topicList);
    List<Topic> topic = selectData.selectTopicDetails(topicName, 1);

    assertEquals(0, topic.size());
  }

  @Test
  public void selectSyncTopics() {
    String env = "DEV";
    when(topicRepo.findAllByEnvironmentAndTenantId(env, 1)).thenReturn(utilMethods.getTopics());
    List<Topic> topicList = selectData.selectSyncTopics(env, null, 1);
    assertEquals(1, topicList.size());
  }

  @Test
  public void selectSyncAcls() {
    String env = "DEV";
    when(aclRepo.findAllByEnvironmentAndTenantId(env, 1)).thenReturn(utilMethods.getAcls());
    List<Acl> topicList = selectData.selectSyncAcls(env, 1);
    assertEquals(1, topicList.size());
  }

  @Test
  public void selectTopicRequests() {
    String requestor = "uiuser1";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);

    List<TopicRequest> schemaRequests = utilMethods.getTopicRequests();

    when(topicRequestsRepo.findAllByTenantId(anyInt())).thenReturn(schemaRequests);
    when(userInfoRepo.findByUsername(requestor)).thenReturn(java.util.Optional.of(userInfo));

    List<TopicRequest> topicRequestsActual =
        selectData.selectTopicRequestsByStatus(false, requestor, "", true, 1);
    assertEquals(1, topicRequestsActual.size());
  }

  @Test
  public void selectTopicRequestsForTopic() {
    int topicId = 1001;
    TopicRequestID topicRequestID = new TopicRequestID();
    topicRequestID.setTenantId(1);
    topicRequestID.setTopicid(topicId);
    when(topicRequestsRepo.findById(topicRequestID))
        .thenReturn(java.util.Optional.ofNullable(utilMethods.getTopicRequest(topicId)));

    TopicRequest topicRequest = selectData.selectTopicRequestsForTopic(topicId, 1);
    assertEquals("testtopic1001", topicRequest.getTopicname());
  }

  @Test
  public void selectAllTeams() {
    when(teamRepo.findAllByTenantId(1)).thenReturn(utilMethods.getTeams());
    List<Team> teamList = selectData.selectAllTeams(1);

    assertEquals(1, teamList.size());
  }

  @Test
  public void selectAcl() {
    when(aclRequestsRepo.findById(any()))
        .thenReturn(java.util.Optional.ofNullable(utilMethods.getAclRequest("")));

    AclRequests aclRequests = selectData.selectAcl(1, 1);
    assertEquals(Integer.valueOf(3), aclRequests.getTeamId());
  }

  @Test
  public void selectAllUsersInfo() {
    String username = "uiuser1";
    when(userInfoRepo.findAllByTenantId(1))
        .thenReturn(utilMethods.getUserInfoList(username, "ADMIN"));
    List<UserInfo> userInfoList = selectData.selectAllUsersInfo(1);

    assertEquals(1, userInfoList.size());
  }

  @Test
  public void selectActivityLog1() {
    String username = "uuser1", env = "DEV";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);
    userInfo.setUsername(username);
    userInfo.setRole("ADMIN");
    when(userInfoRepo.findByUsername(username)).thenReturn(java.util.Optional.of(userInfo));
    when(activityLogRepo.findAllByEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(utilMethods.getLogs());

    List<ActivityLog> activityLogs = selectData.selectActivityLog(username, env, true, 1);

    assertEquals(1, activityLogs.size());
  }

  @Test
  public void selectActivityLog2() {
    String username = "uuser1", env = "DEV";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);
    userInfo.setUsername(username);
    userInfo.setRole("SUPERUSER");
    when(userInfoRepo.findByUsername(username)).thenReturn(java.util.Optional.of(userInfo));
    when(activityLogRepo.findAllByEnvAndTenantId(env, 1)).thenReturn(utilMethods.getLogs());

    List<ActivityLog> activityLogs = selectData.selectActivityLog(username, env, true, 1);

    assertEquals(1, activityLogs.size());
  }

  @Test
  public void selectTeamsOfUsers() {
    String username = "kwusera";

    when(userInfoRepo.findAllByTenantId(anyInt()))
        .thenReturn(utilMethods.getUserInfoList(username, "ADMIN"));
    when(teamRepo.findAllByTenantId(anyInt())).thenReturn(utilMethods.getTeams());

    List<Team> teamList = selectData.selectTeamsOfUsers(username, 101);
    assertEquals(0, teamList.size());

    when(userInfoRepo.findAllByTenantId(anyInt()))
        .thenReturn(utilMethods.getUserInfoList(username, "SUPERUSER"));

    teamList = selectData.selectTeamsOfUsers(username, 1);
    assertEquals(0, teamList.size());
  }
}
