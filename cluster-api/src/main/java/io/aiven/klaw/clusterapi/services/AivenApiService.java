package io.aiven.klaw.clusterapi.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.AivenAclResponse;
import io.aiven.klaw.clusterapi.models.AivenAclStruct;
import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.ServiceAccountDetails;
import io.aiven.klaw.clusterapi.models.enums.AclAttributes;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AivenApiService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final String PROJECT_NAME = "projectName";
  public static final String SERVICE_NAME = "serviceName";
  public static final String USERNAME = "username";
  public static final String PRINCIPLE = "principle";
  public static final String RESOURCE_TYPE = "resourceType";
  public static final String USER_NAME = "userName";

  private RestTemplate restTemplate;

  private HttpHeaders httpHeaders;

  @Value("${klaw.clusters.accesstoken:accesstoken}")
  private String clusterAccessToken;

  @Value("${klaw.clusters.aiven.listacls.api:api}")
  private String listAclsApiEndpoint;

  @Value("${klaw.clusters.aiven.addacls.api:api}")
  private String addAclsApiEndpoint;

  @Value("${klaw.clusters.aiven.deleteacls.api:api}")
  private String deleteAclsApiEndpoint;

  @Value("${klaw.clusters.aiven.addserviceaccount.api:api}")
  private String addServiceAccountApiEndpoint;

  @Value("${klaw.clusters.aiven.getserviceaccount.api:api}")
  private String serviceAccountApiEndpoint;

  @Value("${klaw.clusters.aiven.servicedetails.api:api}")
  private String serviceDetailsApiEndpoint;

  public Map<String, String> createAcls(ClusterAclRequest clusterAclRequest) {
    Map<String, String> resultMap = new HashMap<>();
    RestTemplate restTemplate = getRestTemplate();
    String projectName = clusterAclRequest.getProjectName();
    String serviceName = clusterAclRequest.getServiceName();

    Map<String, String> permissionsMap = new HashMap<>();
    permissionsMap.put(AclAttributes.TOPIC.value, clusterAclRequest.getTopicName());
    permissionsMap.put(AclAttributes.PERMISSION.value, clusterAclRequest.getPermission());
    permissionsMap.put(AclAttributes.USERNAME.value, clusterAclRequest.getUsername());

    String uri =
        addAclsApiEndpoint.replace(PROJECT_NAME, projectName).replace(SERVICE_NAME, serviceName);

    HttpHeaders headers = getHttpHeaders();
    HttpEntity<Map<String, String>> request = new HttpEntity<>(permissionsMap, headers);

    try {
      ResponseEntity<String> response = restTemplate.postForEntity(uri, request, String.class);
      AivenAclResponse aivenAclResponse =
          OBJECT_MAPPER.readValue(response.getBody(), AivenAclResponse.class);
      Optional<AivenAclStruct> aivenAclStructOptional =
          Arrays.stream(aivenAclResponse.getAcl())
              .filter(
                  acl ->
                      acl.getUsername().equals(clusterAclRequest.getUsername())
                          && acl.getTopic().equals(clusterAclRequest.getTopicName())
                          && acl.getPermission().equals(clusterAclRequest.getPermission()))
              .findFirst();
      aivenAclStructOptional.ifPresent(
          aivenAclStruct -> resultMap.put("aivenaclid", aivenAclStruct.getId()));

      handleAclCreationResponse(clusterAclRequest, resultMap, projectName, serviceName, response);

      return resultMap;
    } catch (Exception e) {
      log.error("Exception:", e);
      resultMap.put("result", "Failure in adding acls" + e.getMessage());
      return resultMap;
    }
  }

  private void handleAclCreationResponse(
      ClusterAclRequest clusterAclRequest,
      Map<String, String> resultMap,
      String projectName,
      String serviceName,
      ResponseEntity<String> response) {
    if (response.getStatusCode().equals(HttpStatus.OK)) {
      log.info(
          "Acl created. Project :{} Service : {} Topic : {}",
          projectName,
          serviceName,
          clusterAclRequest.getTopicName());
      if (!getServiceAccountDetails(projectName, serviceName, clusterAclRequest.getUsername())
          .isAccountFound()) {
        createServiceAccount(clusterAclRequest, resultMap);
      } else {
        resultMap.put("result", ApiResultStatus.SUCCESS.value);
      }
    } else {
      log.error(
          "Acl creation failure Project :{} Service : {} Topic : {}",
          projectName,
          serviceName,
          clusterAclRequest.getTopicName());
      resultMap.put("result", "Failure in adding acls" + response.getBody());
    }
  }

  private void createServiceAccount(
      ClusterAclRequest clusterAclRequest, Map<String, String> resultMap) {
    log.debug("Creating service account clusterAclRequest :{}", clusterAclRequest);
    String projectName = clusterAclRequest.getProjectName();
    String serviceName = clusterAclRequest.getServiceName();
    HttpHeaders headers = getHttpHeaders();
    String uri =
        addServiceAccountApiEndpoint
            .replace(PROJECT_NAME, projectName)
            .replace(SERVICE_NAME, serviceName);
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put(AclAttributes.USERNAME.value, clusterAclRequest.getUsername());
    HttpEntity<Map<String, String>> request = new HttpEntity<>(requestMap, headers);
    try {
      ResponseEntity<String> response = getRestTemplate().postForEntity(uri, request, String.class);
      if (response.getStatusCode().equals(HttpStatus.OK)) {
        log.info("Service account created successfully {}", clusterAclRequest);
        resultMap.put("result", ApiResultStatus.SUCCESS.value);
      } else {
        log.info("Service account creation failure {}", clusterAclRequest);
        resultMap.put("result", "Failure in adding service account " + response.getBody());
      }
    } catch (Exception e) {
      log.error("Exception:", e);
      resultMap.put("result", "Failure in adding acls" + e.getMessage());
    }
  }

  private String deleteServiceAccountUser(
      String projectName, String serviceName, String serviceAccountUser) {
    log.debug(
        "Deleting service account user : projectName serviceName serviceAccountUser :{} {} {}",
        projectName,
        serviceName,
        serviceAccountUser);
    String uri =
        serviceAccountApiEndpoint
            .replace(PROJECT_NAME, projectName)
            .replace(SERVICE_NAME, serviceName)
            .replace(USER_NAME, serviceAccountUser);
    try {
      HttpHeaders headers = getHttpHeaders();
      HttpEntity<?> request = new HttpEntity<>(headers);
      restTemplate.exchange(uri, HttpMethod.DELETE, request, Object.class);
    } catch (Exception e) {
      log.error("Exception:", e);
      return ApiResultStatus.FAILURE.value;
    }
    return ApiResultStatus.SUCCESS.value;
  }

  // Get Aiven service account details
  public ServiceAccountDetails getServiceAccountDetails(
      String projectName, String serviceName, String userName) {
    log.debug(
        "Service account for project :{} service : {} user : {}",
        projectName,
        serviceName,
        userName);
    HttpHeaders headers = getHttpHeaders();
    String uri =
        serviceAccountApiEndpoint
            .replace(PROJECT_NAME, projectName)
            .replace(SERVICE_NAME, serviceName)
            .replace(USER_NAME, userName);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);
    ServiceAccountDetails serviceAccountDetails = new ServiceAccountDetails();
    serviceAccountDetails.setAccountFound(false);
    try {
      ResponseEntity<Map<String, Map<String, String>>> response =
          getRestTemplate()
              .exchange(uri, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
      if (response.getStatusCode().equals(HttpStatus.OK)) {
        Map<String, Map<String, String>> responseMap = response.getBody();
        if (responseMap != null
            && responseMap.containsKey("user")
            && responseMap.get("user").containsKey(USERNAME)) {
          // Not sending the full service account details.
          // Response enriched only with username and password. Certificates are removed from the
          // response.

          Map<String, String> resultMap = responseMap.get("user");
          serviceAccountDetails.setPassword(resultMap.get("password"));
          serviceAccountDetails.setUsername(resultMap.get(USERNAME));
          serviceAccountDetails.setAccountFound(true);
          return serviceAccountDetails;
        }
      }
    } catch (Exception e) {
      log.error("Exception:", e);
    }
    return serviceAccountDetails;
  }

  // Get Aiven service accounts
  public Set<String> getServiceAccountUsers(String projectName, String serviceName) {
    log.debug("Services account for project :{} service : {}", projectName, serviceName);
    Set<String> serviceAccountsSet = new HashSet<>();
    HttpHeaders headers = getHttpHeaders();
    String uri =
        serviceDetailsApiEndpoint
            .replace(PROJECT_NAME, projectName)
            .replace(SERVICE_NAME, serviceName);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);
    try {
      ResponseEntity<Map<String, Map<String, Object>>> response =
          getRestTemplate()
              .exchange(uri, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
      if (response.getStatusCode().equals(HttpStatus.OK)) {
        Map<String, Map<String, Object>> responseMap = response.getBody();
        if (responseMap != null && responseMap.containsKey("service")) {
          Map<String, Object> serviceDetailsMap = responseMap.get("service");
          if (serviceDetailsMap.containsKey("users")) {
            ArrayList<HashMap<String, Object>> userList =
                (ArrayList) serviceDetailsMap.get("users");
            userList.forEach(a -> serviceAccountsSet.add((String) a.get("username")));
            return serviceAccountsSet;
          }
        }
      }
    } catch (Exception e) {
      log.error("Exception:", e);
    }
    return new HashSet<>();
  }

  public ApiResponse deleteAcls(ClusterAclRequest clusterAclRequest) throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    boolean serviceUserDeleted = false;
    try {
      String projectName = clusterAclRequest.getProjectName();
      String serviceName = clusterAclRequest.getServiceName();
      String aclId = clusterAclRequest.getAivenAclKey();

      String uri =
          deleteAclsApiEndpoint
              .replace(PROJECT_NAME, projectName)
              .replace(SERVICE_NAME, serviceName)
              .replace("aclId", aclId);

      HttpHeaders headers = getHttpHeaders();
      HttpEntity<?> request = new HttpEntity<>(headers);
      restTemplate.exchange(uri, HttpMethod.DELETE, request, Object.class);

      // Check if service user can be deleted
      serviceUserDeleted = deleteServiceAccountUser(clusterAclRequest);
    } catch (Exception e) {
      log.error("Exception:", e);
      if (e instanceof HttpClientErrorException) {
        if (((HttpClientErrorException) e).getStatusCode() == HttpStatus.NOT_FOUND) {
          return ApiResponse.builder()
              .success(true)
              .message(ApiResultStatus.SUCCESS.value)
              .data(false)
              .build();
        }
      }
      throw new Exception("Error in deleting acls " + e.getMessage());
    }
    return ApiResponse.builder()
        .success(true)
        .message(ApiResultStatus.SUCCESS.value)
        .data(serviceUserDeleted)
        .build();
  }

  private boolean deleteServiceAccountUser(ClusterAclRequest clusterAclRequest) throws Exception {
    Set<Map<String, String>> aclsList =
        listAcls(clusterAclRequest.getProjectName(), clusterAclRequest.getServiceName());
    long aclsListFiltered =
        aclsList.stream()
            .filter(
                aclMap ->
                    aclMap.containsKey(PRINCIPLE)
                        && aclMap.containsKey(RESOURCE_TYPE)
                        && aclMap.get(PRINCIPLE).equals(clusterAclRequest.getUsername())
                        && aclMap.get(RESOURCE_TYPE).equals("TOPIC"))
            .count();
    // Check if there are any other topics using the same principle
    if (aclsListFiltered == 0) {
      deleteServiceAccountUser(
          clusterAclRequest.getProjectName(),
          clusterAclRequest.getServiceName(),
          clusterAclRequest.getUsername());
      return true;
    }
    return false;
  }

  public Set<Map<String, String>> listAcls(String projectName, String serviceName)
      throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info("listAcls {} {}", projectName, serviceName);

    String uri =
        listAclsApiEndpoint.replace(PROJECT_NAME, projectName).replace(SERVICE_NAME, serviceName);

    HttpHeaders headers = getHttpHeaders();
    HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);

    try {
      ResponseEntity<Map<String, List<Map<String, String>>>> responseEntity =
          restTemplate.exchange(
              uri, HttpMethod.GET, request, new ParameterizedTypeReference<>() {});

      List<Map<String, String>> aclsList =
          Objects.requireNonNull(responseEntity.getBody()).get("acl");
      List<Map<String, String>> aclsListUpdated = new ArrayList<>();
      for (Map<String, String> aclsMap : aclsList) {
        Map<String, String> aclsMapUpdated = new HashMap<>();
        for (String keyAcls : aclsMap.keySet()) {
          switch (keyAcls) {
            case "id" -> aclsMapUpdated.put("aivenaclid", aclsMap.get(keyAcls));
            case "permission" -> {
              aclsMapUpdated.put("operation", aclsMap.get(keyAcls).toUpperCase());
              aclsMapUpdated.put(RESOURCE_TYPE, "TOPIC");
            }
            case "topic" -> aclsMapUpdated.put("resourceName", aclsMap.get(keyAcls));
            case USERNAME -> aclsMapUpdated.put(PRINCIPLE, aclsMap.get(keyAcls));
          }
        }
        aclsMapUpdated.put("host", "*");
        aclsMapUpdated.put("permissionType", "ALLOW");
        if ("READ".equals(aclsMapUpdated.get("operation"))) {
          Map<String, String> newRGroupMap = new HashMap<>(aclsMapUpdated);
          newRGroupMap.put(RESOURCE_TYPE, "GROUP");
          newRGroupMap.put("resourceName", "-na-");
          aclsListUpdated.add(newRGroupMap);
        }
        if (!"ADMIN".equals(aclsMapUpdated.get("operation"))
            && !"READWRITE".equals(aclsMapUpdated.get("operation"))) {
          aclsListUpdated.add(aclsMapUpdated);
        }
      }

      return new HashSet<>(aclsListUpdated);
    } catch (RestClientException e) {
      log.error("Exception:", e);
      throw new Exception("Error in listing acls : " + e.getMessage());
    }
  }

  private HttpHeaders getHttpHeaders() {
    if (this.httpHeaders == null) {
      this.httpHeaders = new HttpHeaders();
      this.httpHeaders.set("Authorization", "Bearer " + clusterAccessToken);
      return this.httpHeaders;
    }
    return this.httpHeaders;
  }

  private RestTemplate getRestTemplate() {
    if (this.restTemplate == null) {
      this.restTemplate = new RestTemplate();
    }
    return this.restTemplate;
  }
}
