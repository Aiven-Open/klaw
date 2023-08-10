package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.*;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.repository.AclRepo;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.ActivityLogRepo;
import io.aiven.klaw.repository.EnvRepo;
import io.aiven.klaw.repository.KwEntitySequenceRepo;
import io.aiven.klaw.repository.MessageSchemaRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
import io.aiven.klaw.repository.TeamRepo;
import io.aiven.klaw.repository.TopicRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class InsertDataJdbcTest {

  @Mock private UserInfoRepo userInfoRepo;

  @Mock private TeamRepo teamRepo;

  @Mock private KwEntitySequenceRepo kwEntitySequenceRepo;

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
    ReflectionTestUtils.setField(insertData, "kwEntitySequenceRepo", kwEntitySequenceRepo);
  }

  @Test
  public void insertIntoRequestTopic() {
    int topicName = 1001;
    UserInfo userInfo = utilMethods.getUserInfoMockDao();
    TopicRequest topicRequest = utilMethods.getTopicRequest(topicName);
    when(jdbcSelectHelper.selectUserInfo(topicRequest.getRequestor())).thenReturn(userInfo);
    when(topicRequestsRepo.getNextTopicRequestId(anyInt())).thenReturn(101);
    when(activityLogRepo.getNextActivityLogRequestId(anyInt())).thenReturn(101);

    Map<String, String> result = insertData.insertIntoRequestTopic(topicRequest);
    assertThat(result).containsEntry("result", ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void insertIntoTopicSOT() {
    List<Topic> topics = utilMethods.getTopics();
    when(topicRepo.getNextTopicRequestId(anyInt())).thenReturn(101);
    String result = insertData.insertIntoTopicSOT(topics).getResultStatus();
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void insertIntoRequestAcl() {
    when(jdbcSelectHelper.selectUserInfo("uiuser1")).thenReturn(utilMethods.getUserInfoMockDao());
    when(aclRequestsRepo.getNextAclRequestId(anyInt())).thenReturn(101);
    when(userInfo.getTeamId()).thenReturn(101);
    when(jdbcSelectHelper.selectUserInfo(anyString())).thenReturn(userInfo, userInfo);
    String result =
        insertData.insertIntoRequestAcl(utilMethods.getAclRequest("testtopic")).get("result");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void insertIntoAclsSOT() {
    List<Acl> acls = utilMethods.getAcls();
    when(aclRepo.getNextAclId(anyInt())).thenReturn(101);
    String result = insertData.insertIntoAclsSOT(acls, true);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void insertIntoRequestSchema() {
    SchemaRequest schemaRequest = utilMethods.getSchemaRequestsDao().get(0);

    when(schemaRequestRepo.getNextSchemaRequestId(anyInt())).thenReturn(101);
    when(userInfo.getTeamId()).thenReturn(101);
    when(jdbcSelectHelper.selectUserInfo(anyString())).thenReturn(userInfo, userInfo);

    String result = insertData.insertIntoRequestSchema(schemaRequest);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void insertIntoMessageSchemaSOT() {
    List<MessageSchema> schemas = utilMethods.getMSchemas();
    when(messageSchemaRepo.getNextSchemaId(anyInt())).thenReturn(101);
    String result = insertData.insertIntoMessageSchemaSOT(schemas);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void insertIntoUsers() {
    String result = insertData.insertIntoUsers(utilMethods.getUserInfoMockDao());
    when(userInfoRepo.findById(anyString())).thenReturn(java.util.Optional.of(new UserInfo()));
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void insertIntoTeams() {
    List<KwEntitySequence> kwEntitySequenceList = new ArrayList<>();
    KwEntitySequence kwEntitySequence = new KwEntitySequence();
    kwEntitySequence.setEntityName("ENVIRONMENT");
    kwEntitySequence.setTenantId(101);
    kwEntitySequence.setSeqId(101);
    kwEntitySequenceList.add(kwEntitySequence);
    when(kwEntitySequenceRepo.findAllByEntityNameAndTenantId(anyString(), anyInt()))
        .thenReturn(kwEntitySequenceList);
    String result = insertData.insertIntoTeams(utilMethods.getTeams().get(0));
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @ParameterizedTest
  @MethodSource
  public void insertIntoTopicSOTAndReturnTopics(List<Topic> topics, List<Topic> existingIds) {
    when(topicRepo.findAllById(any())).thenReturn(existingIds);
    when(topicRepo.getNextTopicRequestId(anyInt())).thenReturn(31);
    CRUDResponse<Topic> result = insertData.insertIntoTopicSOT(topics);
    assertThat(result.getResultStatus()).isEqualTo(ApiResultStatus.SUCCESS.value);
    // check that every entry is returnedcorrectly to the calling method.
    assertEquals(topics.size(), result.getEntities().size());
    assertEquals(topics, result.getEntities());
  }

  private static Stream<Arguments> insertIntoTopicSOTAndReturnTopics() {

    return Stream.of(
        Arguments.of(generateTopics(10, false), generateTopics(3, false)),
        Arguments.of(generateTopics(1, false), generateTopics(0, false)),
        Arguments.of(generateTopics(1, false), generateTopics(1, false)),
        Arguments.of(generateTopics(22, false), generateTopics(0, false)));
  }

  private static List<Topic> generateTopics(int numberOfTopics, boolean alreadyExist) {
    List<Topic> topics = new ArrayList<>();

    for (int i = 0; i < numberOfTopics; i++) {
      Topic t = new Topic();
      t.setEnvironment("DEV");
      t.setTopicname("Generate" + i);
      t.setTeamId(8);
      t.setTenantId(101);
      t.setExistingTopic(alreadyExist);
      t.setTopicid(i);
      topics.add(t);
    }
    return topics;
  }
}
