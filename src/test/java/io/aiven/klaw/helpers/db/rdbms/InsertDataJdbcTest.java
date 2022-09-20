package io.aiven.klaw.helpers.db.rdbms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.repository.AclRepo;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.ActivityLogRepo;
import io.aiven.klaw.repository.EnvRepo;
import io.aiven.klaw.repository.MessageSchemaRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.TopicRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class InsertDataJdbcTest {

  @Mock private UserInfoRepo userInfoRepo;

  @Mock private TeamRepo teamRepo;

  @Mock private EnvRepo envRepo;

  @Mock private ActivityLogRepo activityLogRepo;

  @Mock private AclRequestsRepo aclRequestsRepo;

  @Mock private TopicRepo topicRepo;

  @Mock private AclRepo aclRepo;

  @Mock UserInfo userInfo;

  @Mock MessageSchemaRepo messageSchemaRepo;

  @Mock private TopicRequestsRepo topicRequestsRepo;

  @Mock private SchemaRequestRepo schemaRequestRepo;

  @Mock SelectDataJdbc jdbcSelectHelper;

  private InsertDataJdbc insertData;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    insertData = new InsertDataJdbc();
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(insertData, "messageSchemaRepo", messageSchemaRepo);
    ReflectionTestUtils.setField(insertData, "topicRequestsRepo", topicRequestsRepo);
    ReflectionTestUtils.setField(insertData, "topicRepo", topicRepo);
    ReflectionTestUtils.setField(insertData, "teamRepo", teamRepo);
    ReflectionTestUtils.setField(insertData, "userInfoRepo", userInfoRepo);
    ReflectionTestUtils.setField(insertData, "activityLogRepo", activityLogRepo);
    ReflectionTestUtils.setField(insertData, "jdbcSelectHelper", jdbcSelectHelper);
    ReflectionTestUtils.setField(insertData, "aclRepo", aclRepo);
    ReflectionTestUtils.setField(insertData, "schemaRequestRepo", schemaRequestRepo);
    ReflectionTestUtils.setField(insertData, "aclRequestsRepo", aclRequestsRepo);
    ReflectionTestUtils.setField(insertData, "envRepo", envRepo);
  }

  @Test
  public void insertIntoRequestTopic() {
    int topicName = 1001;
    UserInfo userInfo = utilMethods.getUserInfoMockDao();
    TopicRequest topicRequest = utilMethods.getTopicRequest(topicName);
    when(jdbcSelectHelper.selectUserInfo(topicRequest.getUsername())).thenReturn(userInfo);
    when(topicRequestsRepo.getNextTopicRequestId(anyInt())).thenReturn(101);
    when(activityLogRepo.getNextActivityLogRequestId(anyInt())).thenReturn(101);

    Map<String, String> result = insertData.insertIntoRequestTopic(topicRequest);
    assertEquals("success", result.get("result"));
  }

  @Test
  public void insertIntoTopicSOT() {
    List<Topic> topics = utilMethods.getTopics();
    when(topicRepo.getNextTopicRequestId(anyInt())).thenReturn(101);
    String result = insertData.insertIntoTopicSOT(topics, true);
    assertEquals("success", result);
  }

  @Test
  public void insertIntoRequestAcl() {
    when(jdbcSelectHelper.selectUserInfo("uiuser1")).thenReturn(utilMethods.getUserInfoMockDao());
    when(aclRequestsRepo.getNextAclRequestId(anyInt())).thenReturn(101);
    when(userInfo.getTeamId()).thenReturn(101);
    when(jdbcSelectHelper.selectUserInfo(anyString())).thenReturn(userInfo, userInfo);
    String result =
        insertData.insertIntoRequestAcl(utilMethods.getAclRequest("testtopic")).get("result");
    assertEquals("success", result);
  }

  @Test
  public void insertIntoAclsSOT() {
    List<Acl> acls = utilMethods.getAcls();
    when(aclRepo.getNextAclId(anyInt())).thenReturn(101);
    String result = insertData.insertIntoAclsSOT(acls, true);
    assertEquals("success", result);
  }

  @Test
  public void insertIntoRequestSchema() {
    SchemaRequest schemaRequest = utilMethods.getSchemaRequestsDao().get(0);

    when(schemaRequestRepo.getNextSchemaRequestId(anyInt())).thenReturn(101);
    when(userInfo.getTeamId()).thenReturn(101);
    when(jdbcSelectHelper.selectUserInfo(anyString())).thenReturn(userInfo, userInfo);

    String result = insertData.insertIntoRequestSchema(schemaRequest);
    assertEquals("success", result);
  }

  @Test
  public void insertIntoMessageSchemaSOT() {
    List<MessageSchema> schemas = utilMethods.getMSchemas();
    when(messageSchemaRepo.getNextSchemaId(anyInt())).thenReturn(101);
    String result = insertData.insertIntoMessageSchemaSOT(schemas);
    assertEquals("success", result);
  }

  @Test
  public void insertIntoUsers() {
    String result = insertData.insertIntoUsers(utilMethods.getUserInfoMockDao());
    when(userInfoRepo.findById(anyString())).thenReturn(java.util.Optional.of(new UserInfo()));
    assertEquals("success", result);
  }

  @Test
  public void insertIntoTeams() {
    String result = insertData.insertIntoTeams(utilMethods.getTeams().get(0));
    when(teamRepo.getNextTeamId(anyInt())).thenReturn(101);
    assertEquals("success", result);
  }

  @Test
  public void insertIntoEnvs() {
    String result = insertData.insertIntoEnvs(new Env());
    assertEquals("success", result);
  }
}
