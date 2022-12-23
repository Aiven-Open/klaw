package io.aiven.klaw.clusterapi.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.clusterapi.models.AivenAclResponse;
import io.aiven.klaw.clusterapi.models.AivenAclStruct;
import io.aiven.klaw.clusterapi.models.ClusterAclRequest;
import io.aiven.klaw.clusterapi.models.enums.AclAttributes;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class AivenApiService {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  public static final String PROJECT_NAME = "projectName";
  public static final String SERVICE_NAME = "serviceName";
  public static final String USERNAME = "username";

  private RestTemplate restTemplate;

  private HttpHeaders httpHeaders;

  @Value("${klaw.clusters.accesstoken:accesstoken}")
  private String clusterAccessToken;

  @Value("${klaw.clusters.listacls.api:api}")
  private String listAclsApiEndpoint;

  @Value("${klaw.clusters.addacls.api:api}")
  private String addAclsApiEndpoint;

  @Value("${klaw.clusters.deleteacls.api:api}")
  private String deleteAclsApiEndpoint;

  @Value("${klaw.clusters.addserviceaccount.api:api}")
  private String addServiceAccountApiEndpoint;

  @Value("${klaw.clusters.getserviceaccount.api:api}")
  private String getServiceAccountApiEndpoint;

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
      if (getServiceAccountDetails(projectName, serviceName, clusterAclRequest.getUsername())
          .isEmpty()) {
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

  // Get Aiven service account details
  public Map<String, String> getServiceAccountDetails(
      String projectName, String serviceName, String userName) {
    log.debug(
        "Service account for project :{} service : {} user : {}",
        projectName,
        serviceName,
        userName);
    HttpHeaders headers = getHttpHeaders();
    String uri =
        getServiceAccountApiEndpoint
            .replace(PROJECT_NAME, projectName)
            .replace(SERVICE_NAME, serviceName)
            .replace("userName", userName);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(headers);
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
          Map<String, String> responseInnerMap = new HashMap<>();
          Map<String, String> resultMap = responseMap.get("user");
          responseInnerMap.put("password", resultMap.get("password"));
          responseInnerMap.put(USERNAME, resultMap.get(USERNAME));
          return responseInnerMap;
        }
      }
    } catch (Exception e) {
      log.error("Exception:", e);
    }
    return new HashMap<>();
  }

  public String deleteAcls(ClusterAclRequest clusterAclRequest) throws Exception {
    RestTemplate restTemplate = getRestTemplate();

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
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new Exception("Error in deleting acls " + e.getMessage());
    }

    return ApiResultStatus.SUCCESS.value;
  }

  public Set<Map<String, String>> listAcls(String projectName, String serviceName)
      throws Exception {
    RestTemplate restTemplate = getRestTemplate();
    log.info("listAcls {} {}", projectName, serviceName);
    Set<Map<String, String>> acls = new HashSet<>();

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
            case "id":
              aclsMapUpdated.put("aivenaclid", aclsMap.get(keyAcls));
              break;
            case "permission":
              aclsMapUpdated.put("operation", aclsMap.get(keyAcls).toUpperCase());
              aclsMapUpdated.put("resourceType", "TOPIC");
              break;
            case "topic":
              aclsMapUpdated.put("resourceName", aclsMap.get(keyAcls));
              break;
            case USERNAME:
              aclsMapUpdated.put("principle", aclsMap.get(keyAcls));
              break;
          }
        }
        aclsMapUpdated.put("host", "*");
        aclsMapUpdated.put("permissionType", "ALLOW");
        if ("READ".equals(aclsMapUpdated.get("operation"))) {
          Map<String, String> newRGroupMap = new HashMap<>(aclsMapUpdated);
          newRGroupMap.put("resourceType", "GROUP");
          newRGroupMap.put("resourceName", "-na-");
          aclsListUpdated.add(newRGroupMap);
        }
        if (!"ADMIN".equals(aclsMapUpdated.get("operation"))) {
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
