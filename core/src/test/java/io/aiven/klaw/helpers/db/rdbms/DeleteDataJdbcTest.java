package io.aiven.klaw.helpers.db.rdbms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.UtilMethods;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvID;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.TopicRequestID;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.repository.*;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class DeleteDataJdbcTest {

  private DeleteDataJdbc deleteDataJdbc;

  @Mock private TopicRequestsRepo topicRequestsRepo;

  @Mock SchemaRequestRepo schemaRequestRepo;

  @Mock EnvRepo envRepo;

  @Mock TeamRepo teamRepo;

  @Mock AclRequestsRepo aclRequestsRepo;

  @Mock AclRepo aclRepo;

  @Mock UserInfoRepo userInfoRepo;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    deleteDataJdbc =
        new DeleteDataJdbc(
            topicRequestsRepo,
            schemaRequestRepo,
            envRepo,
            teamRepo,
            aclRequestsRepo,
            aclRepo,
            userInfoRepo);
    utilMethods = new UtilMethods();
  }

  @Test
  public void deleteTopicRequest_Failure() {
    String result = deleteDataJdbc.deleteTopicRequest(1001, "uiuser1", 1);
    assertThat(result).contains(ApiResultStatus.FAILURE.value);
  }

  @Test
  public void deleteTopicRequest() {
    TopicRequestID id = new TopicRequestID(1010, 1);
    when(topicRequestsRepo.findById(eq(id)))
        .thenReturn(createTopicRequest("uiuser1", RequestStatus.CREATED));
    String result = deleteDataJdbc.deleteTopicRequest(1010, "uiuser1", 1);
    assertThat(result).contains(ApiResultStatus.SUCCESS.value);
  }

  private Optional<TopicRequest> createTopicRequest(String userName, RequestStatus status) {
    TopicRequest req = new TopicRequest();
    req.setUsername(userName);
    req.setRequestor(userName);
    req.setTopicstatus(status.value);
    return Optional.of(req);
  }

  @Test
  public void deleteSchemaRequest() {
    String result = deleteDataJdbc.deleteSchemaRequest(1001, 1);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteAclRequest() {
    String result = deleteDataJdbc.deleteAclRequest(1001, 1);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteEnvironmentRequest() {
    String clusterId = "1";
    Env envObj = new Env();

    EnvID env = new EnvID();
    env.setId(clusterId);
    env.setTenantId(101);

    when(envRepo.findById(env)).thenReturn(java.util.Optional.of(envObj));
    String result = deleteDataJdbc.deleteEnvironment("1", 101);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteUserRequest() {
    String result = deleteDataJdbc.deleteUserRequest("uiuser1");
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deleteTeamRequest() {
    String result = deleteDataJdbc.deleteTeamRequest(1, 1);
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }

  @Test
  public void deletePrevAclRecs() {
    when(aclRepo.findAllByTenantId(101)).thenReturn(utilMethods.getAllAcls());

    String result = deleteDataJdbc.deletePrevAclRecs(utilMethods.getAclRequest("testtopic"));
    assertThat(result).isEqualTo(ApiResultStatus.SUCCESS.value);
  }
}
