package io.aiven.klaw;

import static io.aiven.klaw.error.KlawErrorMessages.TOPICS_VLD_ERR_122;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.TopicCreateRequestModel;
import io.aiven.klaw.model.requests.TopicUpdateRequestModel;
import io.aiven.klaw.model.response.EnvParams;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.service.TopicControllerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = UiapiApplication.class)
@TestPropertySource(locations = "classpath:test-application-rdbms.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class TopicRequestValidatorImplIT {

  public static final String KWUSER = "kwuser";
  @Autowired private Validator validator;

  @MockBean private CommonUtilsService commonUtilsService;

  @MockBean private TopicControllerService topicControllerService;

  @MockBean private MailUtils mailService;

  private UtilMethods utilMethods;

  @Mock private UserDetails userDetails;

  @BeforeEach
  public void mockDefaults() {
    utilMethods = new UtilMethods();
    loginMock();
  }

  @Test
  @Order(1)
  public void isValidTestNotAuthorizedUser() {
    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(true);
    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString()).contains(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(2)
  public void isValidTestTenantFiltering() {
    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn(KWUSER);
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("2"));
    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString())
        .contains("Failure. Not authorized to request topic for this environment.");
  }

  @Test
  @Order(3)
  public void isValidTestTopicName() {
    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn(KWUSER);
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));

    addTopicRequest.setTopicname("");
    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(2);
    assertThat(violations.toString())
        .contains("Failure. Please fill in a valid topic name.", "Invalid topic name");

    addTopicRequest.setTopicname("testtopic$@$@#$"); // with special characters
    when(topicControllerService.getTopicFromName(anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(topicControllerService.getEnvDetails(anyString()))
        .thenReturn(utilMethods.getEnvListsIncorrect1().get(0));
    violations = validator.validate(addTopicRequest);
    assertThat(violations.toString()).contains("Invalid topic name");
  }

  @Test
  @Order(4)
  public void isValidTestVerifyTenantConfigExists() {
    Integer tenantId = 1;
    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn(KWUSER);
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getSyncCluster(anyInt()))
        .thenThrow(new RuntimeException("Sync cluster not configured"));

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString())
        .contains("Failure. Tenant configuration in Server config is missing. Please configure.");
  }

  @Test
  @Order(5)
  public void isValidTestTopicOwnedByDifferentTeam() {
    Integer tenantId = 101;
    Topic topic = utilMethods.getTopic("testtopic");

    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getTopicFromName(anyString(), anyInt())).thenReturn(List.of(topic));
    when(commonUtilsService.getTeamId(anyString())).thenReturn(101);

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString()).contains("Failure. This topic is owned by a different team.");
  }

  @Test
  @Order(6)
  public void isValidTestPromotionOfTopic() {
    Integer tenantId = 101;
    int teamId = 1001;
    Topic topic = utilMethods.getTopic("testtopic");
    topic.setEnvironment("2");
    topic.setTeamId(teamId);
    Env env = utilMethods.getEnvLists().get(0);

    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    addTopicRequest.setRequestOperationType(RequestOperationType.PROMOTE);
    addTopicRequest.setEnvironment("2");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(commonUtilsService.getTeamId(anyString())).thenReturn(teamId);
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1", "2"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getTopicFromName(anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(topicControllerService.getSyncCluster(anyInt())).thenReturn("1");
    when(commonUtilsService.getEnvProperty(anyInt(), anyString())).thenReturn("1,2");
    when(topicControllerService.getEnvDetails(anyString())).thenReturn(env);

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString())
        .contains("Failure. Please request for a topic first in " + env.getName() + " cluster.");

    when(topicControllerService.getTopicFromName(anyString(), anyInt())).thenReturn(List.of(topic));
    when(topicControllerService.getEnvDetails(anyString())).thenReturn(null);
    violations = validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString()).contains("Failure. Base cluster is not configured.");

    when(topicControllerService.getEnvDetails(anyString())).thenReturn(env);
    violations = validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString())
        .contains("Failure. This topic does not exist in " + env.getName() + " cluster.");
  }

  @Test
  @Order(7)
  public void isValidTestValidateTopicConfigParams() {
    Integer tenantId = 101;
    Env env = utilMethods.getEnvLists().get(0);
    String topicPrefixSuffix = "devkafka";
    //    env.setOtherParams("topic.prefix=" + topicPrefixSuffix);
    env.setParams(new EnvParams());
    env.getParams().setTopicPrefix(List.of(topicPrefixSuffix));
    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString())).thenReturn(env);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(101);

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString())
        .contains("Topic prefix does not match. " + addTopicRequest.getTopicname());

    env.setOtherParams("topic.suffix=" + topicPrefixSuffix);
    env.getParams().setTopicPrefix(null);
    env.getParams().setTopicSuffix(List.of(topicPrefixSuffix));
    when(topicControllerService.getEnvDetails(anyString())).thenReturn(env);

    violations = validator.validate(addTopicRequest);
    assertThat(violations.toString())
        .contains("Topic suffix does not match. " + addTopicRequest.getTopicname());
  }

  @Test
  @Order(8)
  public void isValidTestVerifyIfTopicRequestAlreadyExists() {
    int tenantId = 101;
    Topic topic = utilMethods.getTopic("testtopic");
    topic.setTeamId(1001);

    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    TopicRequest topicRequest = utilMethods.getTopicRequest(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString()))
        .thenReturn(utilMethods.getEnvLists().get(0));
    when(topicControllerService.getExistingTopicRequests(addTopicRequest, tenantId))
        .thenReturn(List.of(topicRequest));
    when(commonUtilsService.getTeamId(anyString())).thenReturn(101);

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString()).contains("Failure. A topic request already exists.");
  }

  @Test
  @Order(9)
  public void isValidTestVerifyIfTopicRequestAlreadyExistsAndIsEditRequest() {
    int tenantId = 101;
    Topic topic = utilMethods.getTopic("testtopic");
    topic.setTeamId(1001);

    TopicCreateRequestModel editTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    editTopicRequest.setRequestId(1010);
    TopicRequest topicRequest = utilMethods.getTopicRequest(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString()))
        .thenReturn(utilMethods.getEnvLists().get(0));
    when(topicControllerService.getExistingTopicRequests(editTopicRequest, tenantId))
        .thenReturn(List.of(topicRequest));
    TopicRequest topicRequest1 = List.of(topicRequest).get(0);
    topicRequest1.setRequestStatus(RequestStatus.CREATED.value);
    when(topicControllerService.getTopicRequestFromTopicId(
            editTopicRequest.getRequestId(), tenantId))
        .thenReturn(topicRequest1);
    when(commonUtilsService.getTeamId(anyString())).thenReturn(101);

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(editTopicRequest);
    assertThat(violations).hasSize(0);
  }

  @Test
  @Order(10)
  public void submitEditTopicRequestForDeleteTypeFailure() {
    int tenantId = 101;
    Topic topic = utilMethods.getTopic("testtopic");
    topic.setTeamId(1001);

    TopicCreateRequestModel editTopicRequest = utilMethods.getTopicCreateRequestModel(1001);
    editTopicRequest.setRequestId(1010);
    editTopicRequest.setRequestOperationType(RequestOperationType.DELETE);
    TopicRequest topicRequest = utilMethods.getTopicRequest(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString()))
        .thenReturn(utilMethods.getEnvLists().get(0));
    when(topicControllerService.getExistingTopicRequests(editTopicRequest, tenantId))
        .thenReturn(List.of(topicRequest));
    when(commonUtilsService.getTeamId(anyString())).thenReturn(101);

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(editTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString()).contains(TOPICS_VLD_ERR_122);
  }

  @Test
  @Order(11)
  public void isValidTestVerifyIfTopicAlreadyExists() {
    int tenantId = 101;
    Topic topic = utilMethods.getTopic("testtopic1001");
    int teamId = 1001;
    topic.setTeamId(teamId);
    topic.setEnvironment("1");

    TopicCreateRequestModel addTopicRequest = utilMethods.getTopicCreateRequestModel(teamId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(commonUtilsService.getTeamId(anyString())).thenReturn(teamId);
    when(commonUtilsService.getEnvsFromUserId(any())).thenReturn(Set.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString()))
        .thenReturn(utilMethods.getEnvLists().get(0));
    when(topicControllerService.getTopicFromName(anyString(), anyInt())).thenReturn(List.of(topic));
    when(topicControllerService.getExistingTopicRequests(any(), anyInt()))
        .thenReturn(Collections.emptyList());

    Set<ConstraintViolation<TopicCreateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString())
        .contains("Failure. This topic already exists in the selected cluster.");
  }

  @Test
  @Order(12)
  public void isValidUpdateRequestTestNotAuthorizedUser() {
    TopicUpdateRequestModel addTopicRequest = utilMethods.getTopicUpdateRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), eq(PermissionType.REQUEST_EDIT_TOPICS)))
        .thenReturn(true);
    Set<ConstraintViolation<TopicUpdateRequestModel>> violations =
        validator.validate(addTopicRequest);
    assertThat(violations).hasSize(1);
    assertThat(violations.toString()).contains(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }
}
