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

  @Value("${klaw.clusters.accesstoken:accesstoken}")
  private String clusterAccessToken;

  @Value("${klaw.clusters.listacls.api:api}")
  private String listAclsApiEndpoint;

  @Value("${klaw.clusters.addacls.api:api}")
  private String addAclsApiEndpoint;

  @Value("${klaw.clusters.deleteacls.api:api}")
  private String deleteAclsApiEndpoint;

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
        addAclsApiEndpoint.replace("projectName", projectName).replace("serviceName", serviceName);

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

      if (response.getStatusCode().equals(HttpStatus.OK)) {
        resultMap.put("result", ApiResultStatus.SUCCESS.value);
      } else {
        resultMap.put("result", "Failure in adding acls" + response.getBody());
      }

      return resultMap;
    } catch (Exception e) {
      log.error("Exception:", e);
      resultMap.put("result", "Failure in adding acls" + e.getMessage());
      return resultMap;
    }
  }

  public String deleteAcls(ClusterAclRequest clusterAclRequest) throws Exception {
    RestTemplate restTemplate = getRestTemplate();

    try {
      String projectName = clusterAclRequest.getProjectName();
      String serviceName = clusterAclRequest.getServiceName();
      String aclId = clusterAclRequest.getAivenAclKey();

      String uri =
          deleteAclsApiEndpoint
              .replace("projectName", projectName)
              .replace("serviceName", serviceName)
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
        listAclsApiEndpoint.replace("projectName", projectName).replace("serviceName", serviceName);

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
            case "username":
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
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + clusterAccessToken);
    return headers;
  }

  private RestTemplate getRestTemplate() {
    return new RestTemplate();
  }
}
