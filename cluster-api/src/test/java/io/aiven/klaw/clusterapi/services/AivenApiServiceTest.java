package io.aiven.klaw.clusterapi.services;

import static io.aiven.klaw.clusterapi.services.AivenApiService.OBJECT_MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import io.aiven.klaw.clusterapi.UtilMethods;
import io.aiven.klaw.clusterapi.models.AivenAclResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(SpringExtension.class)
public class AivenApiServiceTest {

  AivenApiService aivenApiService;
  @Mock RestTemplate restTemplate;

  private UtilMethods utilMethods;

  @BeforeEach
  public void setUp() {
    aivenApiService = new AivenApiService();
    ReflectionTestUtils.setField(aivenApiService, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(
        aivenApiService,
        "addAclsApiEndpoint",
        "https://api.aiven.io/v1/project/projectName/service/serviceName/acl");
    ReflectionTestUtils.setField(
        aivenApiService,
        "listAclsApiEndpoint",
        "https://api.aiven.io/v1/project/projectName/service/serviceName/acl");
    ReflectionTestUtils.setField(
        aivenApiService,
        "deleteAclsApiEndpoint",
        "https://api.aiven.io/v1/project/projectName/service/serviceName/acl/aclId");
    ReflectionTestUtils.setField(
        aivenApiService,
        "getServiceAccountApiEndpoint",
        "https://api.aiven.io/v1/project/projectName/service/serviceName/user/userName");
    ReflectionTestUtils.setField(
        aivenApiService,
        "addServiceAccountApiEndpoint",
        "https://api.aiven.io/v1/project/projectName/service/serviceName/user");
    ReflectionTestUtils.setField(
        aivenApiService,
        "serviceDetailsApiEndpoint",
        "https://api.aiven.io/v1/project/projectName/service/serviceName");
    ReflectionTestUtils.setField(aivenApiService, "clusterAccessToken", "testtoken");
    ReflectionTestUtils.setField(aivenApiService, "restTemplate", restTemplate);
    utilMethods = new UtilMethods();
  }

  // Create Acls (adds service account)
  @Test
  public void createAclsServiceAccountDoesNotExist() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAivenAclRequest("Producer");
    String createAclsUri =
        "https://api.aiven.io/v1/project/"
            + clusterAclRequest.getProjectName()
            + "/service/"
            + clusterAclRequest.getServiceName()
            + "/acl";

    AivenAclResponse aivenAclResponse = utilMethods.getAivenAclResponse();
    String aivenAclResponseString = OBJECT_MAPPER.writeValueAsString(aivenAclResponse);

    // create acl stubs
    ResponseEntity<String> responseEntity =
        new ResponseEntity<>(aivenAclResponseString, HttpStatus.OK);
    when(restTemplate.postForEntity(eq(createAclsUri), any(), eq(String.class)))
        .thenReturn(responseEntity);

    // get service account stubs
    String getServiceAccountUri =
        "https://api.aiven.io/v1/project/"
            + clusterAclRequest.getProjectName()
            + "/service/"
            + clusterAclRequest.getServiceName()
            + "/user/"
            + clusterAclRequest.getUsername();
    // no record exists for this user, throw error by api
    when(restTemplate.exchange(
            eq(getServiceAccountUri),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<Object>) any()))
        .thenThrow(new RuntimeException("No user found"));

    // create service account stubs
    String createServiceAccountUri =
        "https://api.aiven.io/v1/project/"
            + clusterAclRequest.getProjectName()
            + "/service/"
            + clusterAclRequest.getServiceName()
            + "/user";
    ResponseEntity<String> responseEntityAddAccount =
        new ResponseEntity<>("success", HttpStatus.OK);
    when(restTemplate.postForEntity(eq(createServiceAccountUri), any(), eq(String.class)))
        .thenReturn(responseEntityAddAccount);

    Map<String, String> response = aivenApiService.createAcls(clusterAclRequest);
    assertThat(response.get("result")).isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(response.get("aivenaclid")).isEqualTo("testid");
  }

  // Create Acls (service account already exists)
  @Test
  public void createAclsServiceAccountExists() throws Exception {
    ClusterAclRequest clusterAclRequest = utilMethods.getAivenAclRequest("Producer");
    String createAclsUri =
        "https://api.aiven.io/v1/project/"
            + clusterAclRequest.getProjectName()
            + "/service/"
            + clusterAclRequest.getServiceName()
            + "/acl";

    AivenAclResponse aivenAclResponse = utilMethods.getAivenAclResponse();
    String aivenAclResponseString = OBJECT_MAPPER.writeValueAsString(aivenAclResponse);

    // create acl stubs
    ResponseEntity<String> responseEntity =
        new ResponseEntity<>(aivenAclResponseString, HttpStatus.OK);
    when(restTemplate.postForEntity(eq(createAclsUri), any(), eq(String.class)))
        .thenReturn(responseEntity);

    // get service account stubs
    String getServiceAccountUri =
        "https://api.aiven.io/v1/project/"
            + clusterAclRequest.getProjectName()
            + "/service/"
            + clusterAclRequest.getServiceName()
            + "/user/"
            + clusterAclRequest.getUsername();
    Map<String, Map<String, String>> serviceAccountResponse = new HashMap<>();
    Map<String, String> userNameMap = new HashMap<>();
    userNameMap.put("username", "testuser");
    serviceAccountResponse.put("user", userNameMap);
    ResponseEntity<Map<String, Map<String, String>>> responseEntityServiceAccount =
        new ResponseEntity<>(serviceAccountResponse, HttpStatus.OK);
    when(restTemplate.exchange(
            eq(getServiceAccountUri),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<Map<String, Map<String, String>>>) any()))
        .thenReturn(responseEntityServiceAccount);

    Map<String, String> response = aivenApiService.createAcls(clusterAclRequest);
    assertThat(response.get("result")).isEqualTo(ApiResultStatus.SUCCESS.value);
    assertThat(response.get("aivenaclid")).isEqualTo(aivenAclResponse.getAcl()[0].getId());
  }

  // Create Acls fails with acl already exists
  @Test
  public void createAclsServiceAccountExistsFailure() {
    ClusterAclRequest clusterAclRequest = utilMethods.getAivenAclRequest("Producer");
    String createAclsUri =
        "https://api.aiven.io/v1/project/"
            + clusterAclRequest.getProjectName()
            + "/service/"
            + clusterAclRequest.getServiceName()
            + "/acl";

    // create acl stubs throw error
    when(restTemplate.postForEntity(eq(createAclsUri), any(), eq(String.class)))
        .thenThrow(new RuntimeException("Acl ID already exists"));

    Map<String, String> response = aivenApiService.createAcls(clusterAclRequest);
    assertThat(response.get("result")).contains("Failure");
  }

  // Get service accounts
  @Test
  public void getServiceAccounts() {
    // get service account stubs
    String getServiceAccountUri =
        "https://api.aiven.io/v1/project/" + "testproject" + "/service/" + "testservice";

    Map<String, Map<String, Object>> serviceAccountsResponse = new HashMap<>();
    Map<String, Object> userNameMap = new HashMap<>();
    ArrayList<HashMap<String, Object>> userList = new ArrayList<>();
    HashMap<String, Object> userNameMapObj1 = new HashMap<>();
    userNameMapObj1.put("username", "user1");

    HashMap<String, Object> userNameMapObj2 = new HashMap<>();
    userNameMapObj2.put("username", "user2");

    userList.add(userNameMapObj1);
    userList.add(userNameMapObj2);

    userNameMap.put("users", userList);
    serviceAccountsResponse.put("service", userNameMap);
    ResponseEntity responseEntityServiceAccount =
        new ResponseEntity<>(serviceAccountsResponse, HttpStatus.OK);

    when(restTemplate.exchange(
            eq(getServiceAccountUri),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<Object>) any()))
        .thenReturn(responseEntityServiceAccount);

    Set<String> response = aivenApiService.getServiceAccountUsers("testproject", "testservice");
    assertThat(response).contains("user1", "user2");
  }

  // Get service accounts
  @Test
  public void getServiceAccountsDontExist() {
    // get service account stubs
    String getServiceAccountUri =
        "https://api.aiven.io/v1/project/" + "testproject" + "/service/" + "testservice";

    Map<String, Map<String, Object>> serviceAccountsResponse = new HashMap<>();
    ResponseEntity responseEntityServiceAccount =
        new ResponseEntity<>(serviceAccountsResponse, HttpStatus.OK);

    when(restTemplate.exchange(
            eq(getServiceAccountUri),
            eq(HttpMethod.GET),
            any(),
            (ParameterizedTypeReference<Object>) any()))
        .thenReturn(responseEntityServiceAccount);

    Set<String> response = aivenApiService.getServiceAccountUsers("testproject", "testservice");
    assertThat(response).hasSize(0);
  }

  @Disabled
  @Test
  public void deleteAclsTest() throws Exception {
    // TODO when, asserts
    ClusterAclRequest clusterAclRequest =
        ClusterAclRequest.builder()
            .aivenAclKey("4322342")
            .projectName("testproject")
            .serviceName("serviceName")
            .build();

    aivenApiService.deleteAcls(clusterAclRequest);
  }
}
