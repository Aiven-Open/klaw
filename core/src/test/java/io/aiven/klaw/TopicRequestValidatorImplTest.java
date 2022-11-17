package io.aiven.klaw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.model.ApiResultStatus;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.service.CommonUtilsService;
import io.aiven.klaw.service.MailUtils;
import io.aiven.klaw.service.TopicControllerService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
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
@TestPropertySource(locations = "classpath:test-application-rdbms2.properties")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class TopicRequestValidatorImplTest {

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
    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(true);
    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString()).contains(ApiResultStatus.NOT_AUTHORIZED.value);
  }

  @Test
  @Order(2)
  public void isValidTestTenantFiltering() {
    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn(KWUSER);
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("2"));
    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString())
        .contains("Failure. Not authorized to request topic for this environment.");
  }

  @Test
  @Order(3)
  public void isValidTestTopicName() {
    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn(KWUSER);
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("1"));

    addTopicRequest.setTopicname("");
    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(2);
    assertThat(violations.toString()).contains("Failure. Please fill in a valid topic name.");
    assertThat(violations.toString()).contains("Invalid topic name");

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
    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn(KWUSER);
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getSyncCluster(anyInt()))
        .thenThrow(new RuntimeException("Sync cluster not configured"));

    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString())
        .contains("Failure. Tenant configuration in Server config is missing. Please configure.");
  }

  @Test
  @Order(5)
  public void isValidTestTopicOwnedByDifferentTeam() {
    Integer tenantId = 101;
    Topic topic = utilMethods.getTopic("testtopic");

    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getTopicFromName(anyString(), anyInt())).thenReturn(List.of(topic));

    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
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

    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    addTopicRequest.setEnvironment("2");
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(topicControllerService.getTeamId(anyString())).thenReturn(teamId);
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("1", "2"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getTopicFromName(anyString(), anyInt()))
        .thenReturn(Collections.emptyList());
    when(topicControllerService.getSyncCluster(anyInt())).thenReturn("1");
    when(mailService.getEnvProperty(anyInt(), anyString())).thenReturn("1,2");
    when(topicControllerService.getEnvDetails(anyString())).thenReturn(env);

    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString())
        .contains("Failure. Please request for a topic first in " + env.getName() + " cluster.");

    when(topicControllerService.getTopicFromName(anyString(), anyInt())).thenReturn(List.of(topic));
    when(topicControllerService.getEnvDetails(anyString())).thenReturn(null);
    violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString()).contains("Failure. Base cluster is not configured.");

    when(topicControllerService.getEnvDetails(anyString())).thenReturn(env);
    violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString())
        .contains("Failure. This topic does not exist in " + env.getName() + " cluster.");
  }

  @Test
  @Order(7)
  public void isValidTestValidateTopicConfigParams() {
    Integer tenantId = 101;
    Env env = utilMethods.getEnvLists().get(0);
    String topicPrefixSuffix = "devkafka";
    env.setOtherParams("topic.prefix=" + topicPrefixSuffix);

    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString())).thenReturn(env);

    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString())
        .contains("Topic prefix does not match. " + addTopicRequest.getTopicname());

    env.setOtherParams("topic.suffix=" + topicPrefixSuffix);
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

    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(1001);
    TopicRequest topicRequest = utilMethods.getTopicRequest(1001);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString()))
        .thenReturn(utilMethods.getEnvLists().get(0));
    when(topicControllerService.getExistingTopicRequests(addTopicRequest, tenantId))
        .thenReturn(List.of(topicRequest));

    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString()).contains("Failure. A topic request already exists.");
  }

  @Test
  @Order(9)
  public void isValidTestVerifyIfTopicAlreadyExists() {
    int tenantId = 101;
    Topic topic = utilMethods.getTopic("testtopic1001");
    int teamId = 1001;
    topic.setTeamId(teamId);
    topic.setEnvironment("1");

    TopicRequestModel addTopicRequest = utilMethods.getTopicRequestModel(teamId);
    when(commonUtilsService.isNotAuthorizedUser(any(), any())).thenReturn(false);
    when(topicControllerService.getUserName()).thenReturn("superadmin");
    when(topicControllerService.getTeamId(anyString())).thenReturn(teamId);
    when(topicControllerService.getEnvsFromUserId(any())).thenReturn(List.of("1"));
    when(commonUtilsService.getTenantId(any())).thenReturn(tenantId);
    when(topicControllerService.getEnvDetails(anyString()))
        .thenReturn(utilMethods.getEnvLists().get(0));
    when(topicControllerService.getTopicFromName(anyString(), anyInt())).thenReturn(List.of(topic));
    when(topicControllerService.getExistingTopicRequests(any(), anyInt()))
        .thenReturn(Collections.emptyList());

    Set<ConstraintViolation<TopicRequestModel>> violations = validator.validate(addTopicRequest);
    assertThat(violations.size()).isEqualTo(1);
    assertThat(violations.toString())
        .contains("Failure. This topic already exists in the selected cluster.");
  }

  private void loginMock() {
    Authentication authentication = Mockito.mock(Authentication.class);
    SecurityContext securityContext = Mockito.mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(userDetails);
    SecurityContextHolder.setContext(securityContext);
  }
}
