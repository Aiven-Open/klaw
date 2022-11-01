package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.repository.AclRequestsRepo;
import io.aiven.klaw.repository.SchemaRequestRepo;
import io.aiven.klaw.repository.TopicRequestsRepo;
import io.aiven.klaw.repository.UserInfoRepo;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(SpringExtension.class)
public class UpdateDataJdbcTest {

  @Mock private TopicRequestsRepo topicRequestsRepo;

  @Mock private AclRequestsRepo aclRequestsRepo;

  @Mock private UserInfoRepo userInfoRepo;

  @Mock private SchemaRequestRepo schemaRequestRepo;

  @Mock private InsertDataJdbc insertDataJdbcHelper;

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
  }

  @Test
  public void declineTopicRequest() {
    String result = updateData.declineTopicRequest(utilMethods.getTopicRequest(1001), "uiuser2");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void updateTopicRequest() {
    when(insertDataJdbcHelper.insertIntoTopicSOT(any(), eq(false)))
        .thenReturn(ApiResultStatus.SUCCESS.value);
    when(insertDataJdbcHelper.getNextTopicRequestId(anyString(), anyInt())).thenReturn(1001);

    String result = updateData.updateTopicRequest(utilMethods.getTopicRequest(1001), "uiuser2");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
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
    String result =
        updateData.updateSchemaRequest(utilMethods.getSchemaRequestsDao().get(0), "uiuser1");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }
}
