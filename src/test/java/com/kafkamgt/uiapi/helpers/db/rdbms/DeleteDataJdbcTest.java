package com.kafkamgt.uiapi.helpers.db.rdbms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.EnvID;
import com.kafkamgt.uiapi.repository.*;
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
  public void deleteTopicRequest() {
    String result = deleteDataJdbc.deleteTopicRequest(1001, 1);
    assertEquals("success", result);
  }

  @Test
  public void deleteSchemaRequest() {
    String result = deleteDataJdbc.deleteSchemaRequest(1001, 1);
    assertEquals("success", result);
  }

  @Test
  public void deleteAclRequest() {
    String result = deleteDataJdbc.deleteAclRequest(1001, 1);
    assertEquals("success", result);
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
    assertEquals("success", result);
  }

  @Test
  public void deleteUserRequest() {
    String result = deleteDataJdbc.deleteUserRequest("uiuser1");
    assertEquals("success", result);
  }

  @Test
  public void deleteTeamRequest() {
    String result = deleteDataJdbc.deleteTeamRequest(1, 1);
    assertEquals("success", result);
  }

  @Test
  public void deletePrevAclRecs() {
    when(aclRepo.findAllByTenantId(101)).thenReturn(utilMethods.getAllAcls());

    String result = deleteDataJdbc.deletePrevAclRecs(utilMethods.getAclRequest("testtopic"));
    assertEquals("success", result);
  }
}
