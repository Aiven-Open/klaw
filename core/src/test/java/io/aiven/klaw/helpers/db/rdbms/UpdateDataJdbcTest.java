package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.KwKafkaConnector;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.KwKafkaConnectorRepo;
import io.aiven.klaw.repository.MessageSchemaRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
import io.aiven.klaw.repository.TopicRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class UpdateDataJdbcTest {

  @Mock private TopicRequestsRepo topicRequestsRepo;

  @Mock private KwKafkaConnectorRepo kafkaConnectorRepo;

  @Mock private AclRequestsRepo aclRequestsRepo;

  @Mock private UserInfoRepo userInfoRepo;

  @Mock private SchemaRequestRepo schemaRequestRepo;

  @Mock private InsertDataJdbc insertDataJdbcHelper;
  @Mock private TopicRepo topicRepo;

  @Mock MessageSchemaRepo messageSchemaRepo;

  @Mock private DeleteDataJdbc deleteDataJdbcHelper;

  private UpdateDataJdbc updateData;

  private UtilMethods utilMethods;

  @Mock UserInfo userInfo;

  @BeforeEach
  public void setUp() throws Exception {
    updateData =
        new UpdateDataJdbc(
            topicRequestsRepo,
            aclRequestsRepo,
            userInfoRepo,
            schemaRequestRepo,
            insertDataJdbcHelper);
    utilMethods = new UtilMethods();
    ReflectionTestUtils.setField(updateData, "deleteDataJdbcHelper", deleteDataJdbcHelper);
    ReflectionTestUtils.setField(updateData, "topicRepo", topicRepo);
    ReflectionTestUtils.setField(updateData, "kafkaConnectorRepo", kafkaConnectorRepo);
    ReflectionTestUtils.setField(updateData, "messageSchemaRepo", messageSchemaRepo);
  }

  @Test
  public void declineTopicRequest() {
    String result = updateData.declineTopicRequest(utilMethods.getTopicRequest(1001), "uiuser2");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  // parameters from RequestOperationType
  @Test
  public void updateUpdateTopicRequest() {
    int reqNum = 1001;
    String requestOperationType = "Update";
    when(insertDataJdbcHelper.insertIntoTopicSOT(any(), eq(false)))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(insertDataJdbcHelper.getNextTopicRequestId(anyString(), anyInt())).thenReturn(reqNum);
    TopicRequest req = utilMethods.getTopicRequest(1001);
    req.setOtherParams("1001");
    req.setRequestOperationType(requestOperationType);
    String result = updateData.updateTopicRequest(req, "uiuser2");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(topicRepo, times(1)).save(any());
  }

  @Test
  public void updateDeleteTopicRequest() {
    int reqNum = 1001;
    String requestOperationType = "Delete";
    when(insertDataJdbcHelper.insertIntoTopicSOT(any(), eq(false)))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(insertDataJdbcHelper.getNextTopicRequestId(anyString(), anyInt())).thenReturn(reqNum);
    TopicRequest req = utilMethods.getTopicRequest(1001);

    req.setRequestOperationType(requestOperationType);
    req.setDeleteAssociatedSchema(true);
    String result = updateData.updateTopicRequest(req, "uiuser2");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(deleteDataJdbcHelper, times(1)).deleteTopics(any());
    verify(deleteDataJdbcHelper, times(1)).deleteSchemas(any());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Create", "Promote"})
  public void updateCreateAndPromoteTopicRequest(String requestOperationType) {
    int reqNum = 1001;
    when(insertDataJdbcHelper.insertIntoTopicSOT(any(), anyBoolean()))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(insertDataJdbcHelper.getNextTopicRequestId(anyString(), anyInt())).thenReturn(reqNum);
    TopicRequest req = utilMethods.getTopicRequest(1001);
    req.setRequestOperationType(requestOperationType);
    String result = updateData.updateTopicRequest(req, "uiuser2");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
    verify(insertDataJdbcHelper, times(1)).insertIntoTopicSOT(any(), anyBoolean());
  }

  @Test
  public void updateAclRequest() {
    when(insertDataJdbcHelper.insertIntoAclsSOT(any(), eq(false)))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    String result =
        updateData.updateAclRequest(utilMethods.getAclRequestCreate("testtopic"), "uiuser2", "{}");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updateAclRequest1() {
    when(deleteDataJdbcHelper.deletePrevAclRecs(any())).thenReturn(ApiResultStatus.SUCCESS.value);
    String result =
        updateData.updateAclRequest(utilMethods.getAclRequest("testtopic"), "uiuser2", "{}");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void declineAclRequest() {
    String result = updateData.declineAclRequest(utilMethods.getAclRequest("testtopic"), "uiuser2");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updatePassword() {
    String user = "uiuser1";
    when(userInfoRepo.findById(user)).thenReturn(Optional.of(userInfo));
    String result = updateData.updatePassword(user, "pwd");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updateSchemaRequest() {
    when(messageSchemaRepo.findAllByTenantIdAndTopicnameAndSchemaversionAndEnvironment(
            anyInt(), anyString(), anyString(), anyString()))
        .thenReturn(Collections.emptyList());
    String result =
        updateData.updateSchemaRequest(utilMethods.getSchemaRequestsDao().get(0), "uiuser1");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updateConnectorDocumentation() {
    KwKafkaConnector kwKafkaConnector = utilMethods.getKwKafkaConnector();
    when(kafkaConnectorRepo.findById(any())).thenReturn(Optional.of(kwKafkaConnector));
    kwKafkaConnector.setDocumentation("new docs");

    String status = updateData.updateConnectorDocumentation(kwKafkaConnector);
    assertThat(status).isEqualTo(ApiResultStatus.SUCCESS.value);
  }
}
