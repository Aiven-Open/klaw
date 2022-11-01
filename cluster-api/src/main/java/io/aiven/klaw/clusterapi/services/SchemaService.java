package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterStatus;
import io.aiven.klaw.clusterapi.models.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SchemaService {
  public static final String SCHEMA_REGISTRY_CONTENT_TYPE =
      "application/vnd.schemaregistry.v1+json";

  @Value("${klaw.schemaregistry.compatibility.default:BACKWARD}")
  private String defaultSchemaCompatibility;

  final ClusterApiUtils clusterApiUtils;

  public SchemaService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public synchronized ApiResponse registerSchema(ClusterSchemaRequest clusterSchemaRequest) {
    try {
      log.info("Into register schema request {}", clusterSchemaRequest);
      //            set default compatibility
      //            setSchemaCompatibility(environmentVal, topicName, false, protocol);
      String suffixUrl =
          clusterSchemaRequest.getEnv()
              + "/subjects/"
              + clusterSchemaRequest.getTopicName()
              + "-value/versions";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(
              suffixUrl, clusterSchemaRequest.getProtocol(), KafkaClustersType.SCHEMA_REGISTRY);

      Map<String, String> params = new HashMap<>();
      params.put("schema", clusterSchemaRequest.getFullSchema());

      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", SCHEMA_REGISTRY_CONTENT_TYPE);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
      ResponseEntity<String> responseNew =
          reqDetails.getRight().postForEntity(reqDetails.getLeft(), request, String.class);

      String updateTopicReqStatus = responseNew.getBody();
      log.info(responseNew.getBody());

      return ApiResponse.builder().result(updateTopicReqStatus).build();
    } catch (Exception e) {
      log.error(e.getMessage());
      if (((HttpClientErrorException.Conflict) e).getStatusCode().value() == 409) {
        return ApiResponse.builder()
            .result("Schema being registered is incompatible with an earlier schema")
            .build();
      }
      return ApiResponse.builder().result("Failure in registering schema.").build();
    }
  }

  public Map<Integer, Map<String, Object>> getSchema(
      String environmentVal, KafkaSupportedProtocol protocol, String topicName) {
    try {
      log.info("Into getSchema request {} {} {}", topicName, environmentVal, protocol);
      if (environmentVal == null) {
        return null;
      }

      List<Integer> versionsList = getSchemaVersions(environmentVal, topicName, protocol);
      String schemaCompatibility = getSchemaCompatibility(environmentVal, topicName, protocol);
      Map<Integer, Map<String, Object>> allSchemaObjects = new TreeMap<>();

      if (versionsList != null) {
        for (Integer schemaVersion : versionsList) {
          String suffixUrl =
              environmentVal + "/subjects/" + topicName + "-value/versions/" + schemaVersion;
          Pair<String, RestTemplate> reqDetails =
              clusterApiUtils.getRequestDetails(
                  suffixUrl, protocol, KafkaClustersType.SCHEMA_REGISTRY);

          Map<String, String> params = new HashMap<>();

          ResponseEntity<HashMap> responseNew =
              reqDetails.getRight().getForEntity(reqDetails.getLeft(), HashMap.class, params);
          Map<String, Object> schemaResponse = responseNew.getBody();
          if (schemaResponse != null) {
            schemaResponse.put("compatibility", schemaCompatibility);
          }

          log.info(Objects.requireNonNull(responseNew.getBody()).toString());
          allSchemaObjects.put(schemaVersion, schemaResponse);
        }
      }

      return allSchemaObjects;
    } catch (Exception e) {
      log.error("Error from getSchema : " + e.getMessage());
      return new TreeMap<>();
    }
  }

  private List<Integer> getSchemaVersions(
      String environmentVal, String topicName, KafkaSupportedProtocol protocol) {
    try {
      log.info("Into getSchema versions {} {}", topicName, environmentVal);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/subjects/" + topicName + "-value/versions";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol, KafkaClustersType.SCHEMA_REGISTRY);

      Map<String, String> params = new HashMap<>();

      ResponseEntity<ArrayList> responseList =
          reqDetails.getRight().getForEntity(reqDetails.getLeft(), ArrayList.class, params);
      log.info("Schema versions " + responseList);
      return responseList.getBody();
    } catch (Exception e) {
      log.error("Error in getting versions " + e.getMessage());
      return new ArrayList<>();
    }
  }

  private String getSchemaCompatibility(
      String environmentVal, String topicName, KafkaSupportedProtocol protocol) {
    try {
      log.info("Into getSchema compatibility {} {}", topicName, environmentVal);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/config/" + topicName + "-value";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol, KafkaClustersType.SCHEMA_REGISTRY);

      Map<String, String> params = new HashMap<>();

      ResponseEntity<HashMap> responseList =
          reqDetails.getRight().getForEntity(reqDetails.getLeft(), HashMap.class, params);
      log.info("Schema compatibility " + responseList);
      return (String) responseList.getBody().get("compatibilityLevel");
    } catch (Exception e) {
      log.error("Error in getting schema compatibility " + e.getMessage());
      return "NOT SET";
    }
  }

  private boolean setSchemaCompatibility(
      String environmentVal, String topicName, boolean isForce, KafkaSupportedProtocol protocol) {
    try {
      log.info("Into setSchema compatibility {} {}", topicName, environmentVal);
      if (environmentVal == null) {
        return false;
      }

      String suffixUrl = environmentVal + "/config/" + topicName + "-value";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol, KafkaClustersType.SCHEMA_REGISTRY);

      Map<String, String> params = new HashMap<>();
      if (isForce) {
        params.put("compatibility", "NONE");
      } else params.put("compatibility", defaultSchemaCompatibility);

      HttpHeaders headers = new HttpHeaders();
      headers.set("Content-Type", SCHEMA_REGISTRY_CONTENT_TYPE);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
      reqDetails.getRight().put(reqDetails.getLeft(), request, String.class);
      return true;
    } catch (Exception e) {
      log.error("Error in setting schema compatibility " + e.getMessage());
      return false;
    }
  }

  protected ClusterStatus getSchemaRegistryStatus(
      String environmentVal, KafkaSupportedProtocol protocol) {

    String suffixUrl = environmentVal + "/subjects";
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, protocol, KafkaClustersType.SCHEMA_REGISTRY);

    Map<String, String> params = new HashMap<>();

    try {
      reqDetails.getRight().getForEntity(reqDetails.getLeft(), Object.class, params);
      return ClusterStatus.ONLINE;
    } catch (RestClientException e) {
      e.printStackTrace();
      return ClusterStatus.OFFLINE;
    }
  }
}
