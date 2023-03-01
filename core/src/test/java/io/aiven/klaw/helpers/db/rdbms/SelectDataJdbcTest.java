package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.AclRequests;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.TopicRequestID;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.cluster.ClusterSchemaRequest;
import io.aiven.klaw.repository.AclRepo;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.ActivityLogRepo;
import io.aiven.klaw.repository.EnvRepo;
import io.aiven.klaw.repository.KwPropertiesRepo;
import io.aiven.klaw.repository.RegisterInfoRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.TopicRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class SelectDataJdbcTest {

  public static final String TESTTOPIC = "testtopic";
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

  @Captor
  private ArgumentCaptor<Example<SchemaRequest>> schemaRequestCaptor;

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
    when(userInfoRepo.findByUsernameIgnoreCase(requestor))
        .thenReturn(java.util.Optional.of(userInfo));

    List<AclRequests> aclRequestsActual =
        selectData.selectAclRequests(
            false, requestor, "", "all", false, null, null, null, false, 1);
    assertThat(aclRequestsActual).isEmpty();
  }

  @Test
  public void selectSchemaRequests() {
    String requestor = "uiuser1";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);

    List<SchemaRequest> schemaRequests = utilMethods.getSchemaRequestsDao();

    when(schemaRequestRepo.findAllByTenantId(1)).thenReturn(schemaRequests);
    when(userInfoRepo.findByUsernameIgnoreCase(requestor))
            .thenReturn(java.util.Optional.of(userInfo));

    List<SchemaRequest> schemaRequestsActual =
            selectData.selectFilteredSchemaRequests(false, requestor, 1, null, null, null, null, false);
    verify(schemaRequestRepo,times(1)).findAll(schemaRequestCaptor.capture());
    Example<SchemaRequest> value = schemaRequestCaptor.getValue();
    assertThat(schemaRequestsActual).isEmpty();
    assertThat(value.getProbe().getForceRegister()).isNull();
    assertThat(value.getProbe().getUsername()).isEqualTo(null);
  }

  @Test
  public void selectSchemaRequestsIsMyRequest() {
    String requestor = "uiuser1";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);

    List<SchemaRequest> schemaRequests = utilMethods.getSchemaRequestsDao();

    when(schemaRequestRepo.findAllByTenantId(1)).thenReturn(schemaRequests);
    when(userInfoRepo.findByUsernameIgnoreCase(requestor))
            .thenReturn(java.util.Optional.of(userInfo));

    List<SchemaRequest> schemaRequestsActual =
            selectData.selectFilteredSchemaRequests(false, requestor, 1, null, null, null, null, true);
    verify(schemaRequestRepo,times(1)).findAll(schemaRequestCaptor.capture());
    Example<SchemaRequest> value = schemaRequestCaptor.getValue();
    assertThat(schemaRequestsActual).isEmpty();
    assertThat(value.getProbe().getForceRegister()).isNull();
    assertThat(value.getProbe().getUsername()).isEqualTo("uiuser1");
  }

  @Test
  public void selectSchemaRequest() {
    SchemaRequest schemaRequest = new SchemaRequest();
    schemaRequest.setReq_no(1001);
    schemaRequest.setTopicname(TESTTOPIC);
    when(schemaRequestRepo.findById(any())).thenReturn(java.util.Optional.of(schemaRequest));
    SchemaRequest schemaRequestActual = selectData.selectSchemaRequest(1001, 1);
    assertThat(schemaRequestActual.getTopicname()).isEqualTo(TESTTOPIC);

  }

  @Test
  public void selectTopicDetailsSuccess() {
    String topicName = TESTTOPIC, env = "DEV";
    List<Topic> topicList = new ArrayList<>();
    topicList.add(utilMethods.getTopic(topicName));
    when(topicRepo.findAllByTopicnameAndTenantId(topicName, 1)).thenReturn(topicList);
    List<Topic> topic = selectData.selectTopicDetails(topicName, 1);

    assertThat(topic.get(0).getTopicname()).isEqualTo(topicName);
  }

  @Test
  public void selectTopicDetailsFailure() {
    String topicName = TESTTOPIC;
    List<Topic> topicList = new ArrayList<>();
    when(topicRepo.findAllByTopicnameAndTenantId(topicName, 1)).thenReturn(topicList);
    List<Topic> topic = selectData.selectTopicDetails(topicName, 1);

    assertThat(topic).isEmpty();
  }

  @Test
  public void selectSyncTopics() {
    String env = "DEV";
    when(topicRepo.findAllByEnvironmentAndTenantId(env, 1)).thenReturn(utilMethods.getTopics());
    List<Topic> topicList = selectData.selectSyncTopics(env, null, 1);
    assertThat(topicList).hasSize(1);
  }

  @Test
  public void selectSyncAcls() {
    String env = "DEV";
    when(aclRepo.findAllByEnvironmentAndTenantId(env, 1)).thenReturn(utilMethods.getAcls());
    List<Acl> topicList = selectData.selectSyncAcls(env, 1);
    assertThat(topicList).hasSize(1);
  }

  @Test
  public void selectTopicRequests() {
    String requestor = "uiuser1";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);

    List<TopicRequest> schemaRequests = utilMethods.getTopicRequests();

    when(topicRequestsRepo.findAll(any(Example.class))).thenReturn(schemaRequests);
    when(userInfoRepo.findByUsernameIgnoreCase(requestor))
        .thenReturn(java.util.Optional.of(userInfo));

    List<TopicRequest> topicRequestsActual =
        selectData.getFilteredTopicRequests(
            false, requestor, "created", true, 1, null, null, null, false);

    assertThat(topicRequestsActual).hasSize(1);
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
    assertThat(topicRequest.getTopicname()).isEqualTo("testtopic1001");
  }

  @Test
  public void selectAllTeams() {
    when(teamRepo.findAllByTenantId(1)).thenReturn(utilMethods.getTeams());
    List<Team> teamList = selectData.selectAllTeams(1);

    assertThat(teamList).hasSize(1);
  }

  @Test
  public void selectAcl() {
    when(aclRequestsRepo.findById(any()))
        .thenReturn(java.util.Optional.ofNullable(utilMethods.getAclRequest("")));

    AclRequests aclRequests = selectData.selectAcl(1, 1);
    assertThat(aclRequests.getTeamId()).isEqualTo(3);
  }

  @Test
  public void selectAllUsersInfo() {
    String username = "uiuser1";
    when(userInfoRepo.findAllByTenantId(1))
        .thenReturn(utilMethods.getUserInfoList(username, "ADMIN"));
    List<UserInfo> userInfoList = selectData.selectAllUsersInfo(1);

    assertThat(userInfoList).hasSize(1);
  }

  @Test
  public void selectActivityLog1() {
    String username = "uuser1", env = "DEV";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);
    userInfo.setUsername(username);
    userInfo.setRole("ADMIN");
    when(userInfoRepo.findByUsernameIgnoreCase(username))
        .thenReturn(java.util.Optional.of(userInfo));
    when(activityLogRepo.findAllByEnvAndTenantId(anyString(), anyInt()))
        .thenReturn(utilMethods.getLogs());

    List<ActivityLog> activityLogs = selectData.selectActivityLog(username, env, true, 1);

    assertThat(activityLogs).hasSize(1);
  }

  @Test
  public void selectActivityLog2() {
    String username = "uuser1", env = "DEV";
    UserInfo userInfo = new UserInfo();
    userInfo.setTeamId(1);
    userInfo.setUsername(username);
    userInfo.setRole("SUPERUSER");
    when(userInfoRepo.findByUsernameIgnoreCase(username))
        .thenReturn(java.util.Optional.of(userInfo));
    when(activityLogRepo.findAllByEnvAndTenantId(env, 1)).thenReturn(utilMethods.getLogs());

    List<ActivityLog> activityLogs = selectData.selectActivityLog(username, env, true, 1);

    assertThat(activityLogs).hasSize(1);
  }

  @Test
  public void selectTeamsOfUsers() {
    String username = "kwusera";

    when(userInfoRepo.findAllByTenantId(anyInt()))
        .thenReturn(utilMethods.getUserInfoList(username, "ADMIN"));
    when(teamRepo.findAllByTenantId(anyInt())).thenReturn(utilMethods.getTeams());

    List<Team> teamList = selectData.selectTeamsOfUsers(username, 101);
    assertThat(teamList).isEmpty();

    when(userInfoRepo.findAllByTenantId(anyInt()))
        .thenReturn(utilMethods.getUserInfoList(username, "SUPERUSER"));

    teamList = selectData.selectTeamsOfUsers(username, 1);
    assertThat(teamList).isEmpty();
  }
}
