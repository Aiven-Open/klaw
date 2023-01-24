package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class SchemaService {

  private static final ParameterizedTypeReference<Map<String, String>>
      GET_SCHEMA_COMPATIBILITY_TYPEREF = new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<Map<String, Object>> GET_SCHEMA_TYPEREF =
      new ParameterizedTypeReference<>() {};

  private static final ParameterizedTypeReference<List<Integer>> GET_SCHEMAVERSIONS_TYPEREF =
      new ParameterizedTypeReference<>() {};

  public static final String SCHEMA_REGISTRY_CONTENT_TYPE =
      "application/vnd.schemaregistry.v1+json";
  public static final String SCHEMA_COMPATIBILITY_NOT_SET = "NOT SET";
  public static final String SUBJECT = "Subject";
  public static final String GLOBAL = "Global";

  @Value("${klaw.schemaregistry.compatibility.default:BACKWARD}")
  private String defaultSchemaCompatibility;

  final ClusterApiUtils clusterApiUtils;

  public SchemaService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public synchronized ApiResponse registerSchema(ClusterSchemaRequest clusterSchemaRequest) {
    String schemaCompatibility = null;
    boolean schemaCompatibilityCompleted = false;
    try {
      log.debug("RegisterSchema on {} ", clusterSchemaRequest.getTopicName());
      //            set default compatibility
      log.info("isForceRegisterEnabled {}", clusterSchemaRequest.isForceRegister());
      if (clusterSchemaRequest.isForceRegister()) {
        log.debug("RegisterSchema - Force Register Enabled");
        schemaCompatibility =
            getSubjectSchemaCompatibility(
                clusterSchemaRequest.getEnv(),
                clusterSchemaRequest.getTopicName(),
                clusterSchemaRequest.getProtocol(),
                clusterSchemaRequest.getClusterIdentification());
        // Error thrown preventing progress if Schema Compatibility not retrieved correctly.
        schemaCompatibilityCompleted =
            checkSchemaCompatibilitySuccessfullyRetrieved(schemaCompatibility);
        log.debug(
            "RegisterSchema - original Schema Compatibility {} for Topic {}",
            schemaCompatibility,
            clusterSchemaRequest.getTopicName());
        // only reset if we have the previous schema to revert to.
        if (schemaCompatibilityCompleted) {
          setSchemaCompatibility(
              clusterSchemaRequest.getEnv(),
              clusterSchemaRequest.getTopicName(),
              clusterSchemaRequest.isForceRegister(),
              clusterSchemaRequest.getProtocol(),
              clusterSchemaRequest.getClusterIdentification(),
              null);
        }
      }
      String suffixUrl =
          clusterSchemaRequest.getEnv()
              + "/subjects/"
              + clusterSchemaRequest.getTopicName()
              + "-value/versions";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, clusterSchemaRequest.getProtocol());

      Map<String, String> params = new HashMap<>();
      params.put("schema", clusterSchemaRequest.getFullSchema());

      HttpHeaders headers =
          clusterApiUtils.createHeaders(
              clusterSchemaRequest.getClusterIdentification(), KafkaClustersType.SCHEMA_REGISTRY);
      headers.set("Content-Type", SCHEMA_REGISTRY_CONTENT_TYPE);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
      ResponseEntity<String> responseNew =
          reqDetails.getRight().postForEntity(reqDetails.getLeft(), request, String.class);

      String updateTopicReqStatus = responseNew.getBody();
      log.info("SchemaRequest response body {}", responseNew.getBody());

      return ApiResponse.builder().result(updateTopicReqStatus).build();
    } catch (Exception e) {
      log.error("Exception:", e);

      if (e instanceof HttpClientErrorException
          && ((HttpClientErrorException.Conflict) e).getStatusCode().value() == 409) {
        return ApiResponse.builder()
            .result("Schema being registered is incompatible with an earlier schema")
            .build();
      }
      return ApiResponse.builder().result("Failure in registering schema.").build();
    } finally {
      // Ensure the Schema compatibility is returned to previous setting before the force update.
      // (Set isForce to false in all cases to revert it.
      if (clusterSchemaRequest.isForceRegister() && schemaCompatibilityCompleted) {
        log.info(
            "RegisterSchema - Force Commit revert to original Schema Compatibility {} for Topic {}",
            schemaCompatibility,
            clusterSchemaRequest.getTopicName());
        setSchemaCompatibility(
            clusterSchemaRequest.getEnv(),
            clusterSchemaRequest.getTopicName(),
            false,
            clusterSchemaRequest.getProtocol(),
            clusterSchemaRequest.getClusterIdentification(),
            schemaCompatibility);
      }
    }
  }

  public Map<Integer, Map<String, Object>> getSchema(
      String environmentVal,
      KafkaSupportedProtocol protocol,
      String clusterIdentification,
      String topicName) {
    try {
      log.info("Into getSchema request {} {} {}", topicName, environmentVal, protocol);
      if (environmentVal == null) {
        return null;
      }

      List<Integer> versionsList =
          getSchemaVersions(environmentVal, topicName, protocol, clusterIdentification);
      String schemaCompatibility =
          getSubjectSchemaCompatibility(environmentVal, topicName, protocol, clusterIdentification);
      Map<Integer, Map<String, Object>> allSchemaObjects = new TreeMap<>();

      if (versionsList != null) {
        for (Integer schemaVersion : versionsList) {
          String suffixUrl =
              environmentVal + "/subjects/" + topicName + "-value/versions/" + schemaVersion;
          Pair<String, RestTemplate> reqDetails =
              clusterApiUtils.getRequestDetails(suffixUrl, protocol);

          Map<String, String> params = new HashMap<>();
          HttpEntity<Object> request = createSchemaRegistryRequest(clusterIdentification);

          ResponseEntity<Map<String, Object>> responseNew =
              reqDetails
                  .getRight()
                  .exchange(
                      reqDetails.getLeft(), HttpMethod.GET, request, GET_SCHEMA_TYPEREF, params);
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
      log.error("Error from getSchema : ", e);
      return Collections.emptyMap();
    }
  }

  private List<Integer> getSchemaVersions(
      String environmentVal,
      String topicName,
      KafkaSupportedProtocol protocol,
      String clusterIdentification) {
    try {
      log.info("Into getSchema versions {} {}", topicName, environmentVal);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/subjects/" + topicName + "-value/versions";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol);

      Map<String, String> params = new HashMap<>();
      HttpEntity<Object> request = createSchemaRegistryRequest(clusterIdentification);

      ResponseEntity<List<Integer>> responseList =
          reqDetails
              .getRight()
              .exchange(
                  reqDetails.getLeft(),
                  HttpMethod.GET,
                  request,
                  GET_SCHEMAVERSIONS_TYPEREF,
                  params);
      log.info("Schema versions " + responseList);
      return responseList.getBody();
    } catch (Exception e) {
      log.error("Error in getting versions ", e);
      return Collections.emptyList();
    }
  }

  private String getSubjectSchemaCompatibility(
      String environmentVal,
      String topicName,
      KafkaSupportedProtocol protocol,
      String clusterIdentification) {
    try {
      log.info("Into getSchema compatibility {} {}", topicName, environmentVal);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/config/" + topicName + "-value";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol);

      ResponseEntity<Map<String, String>> responseList =
          getSubjectSchemaCompatibilityRequest(
              reqDetails, new HashMap<>(), createSchemaRegistryRequest(clusterIdentification));
      log.info("Schema compatibility " + responseList);
      return responseList.getBody().get("compatibilityLevel");
    } catch (Exception e) {
      log.error("Error in getting schema compatibility ", e);
      return SCHEMA_COMPATIBILITY_NOT_SET;
    }
  }

  private HttpEntity<Object> createSchemaRegistryRequest(String clusterIdentification) {
    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.SCHEMA_REGISTRY);
    HttpEntity<Object> request = new HttpEntity<>(headers);
    return request;
  }

  private static ResponseEntity<Map<String, String>> getSubjectSchemaCompatibilityRequest(
      Pair<String, RestTemplate> reqDetails,
      Map<String, String> params,
      HttpEntity<Object> request) {
    return reqDetails
        .getRight()
        .exchange(
            reqDetails.getLeft(),
            HttpMethod.GET,
            request,
            GET_SCHEMA_COMPATIBILITY_TYPEREF,
            params);
  }

  private boolean setSchemaCompatibility(
      String environmentVal,
      String topicName,
      boolean isForce,
      KafkaSupportedProtocol protocol,
      String clusterIdentification,
      String schemaCompatibilityMode) {
    try {
      log.info("Into setSchema compatibility {} {}", topicName, environmentVal);
      if (environmentVal == null) {
        return false;
      }

      String suffixUrl = environmentVal + "/config/" + topicName + "-value";

      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol);

      Map<String, String> params = new HashMap<>();
      if (isForce) {
        params.put("compatibility", "NONE");
      } else if (schemaCompatibilityMode != null && !schemaCompatibilityMode.isBlank()) {
        // Allows us to specify the schema compatibility to the exact type we want.
        params.put("compatibility", schemaCompatibilityMode);
      } else {
        params.put("compatibility", defaultSchemaCompatibility);
      }

      HttpHeaders headers =
          clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.SCHEMA_REGISTRY);
      headers.set("Content-Type", SCHEMA_REGISTRY_CONTENT_TYPE);
      HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);

      reqDetails.getRight().put(reqDetails.getLeft(), request, String.class);
      return true;
    } catch (Exception e) {
      log.error("Error in setting schema compatibility ", e);
      return false;
    }
  }

  private boolean checkSchemaCompatibilitySuccessfullyRetrieved(String schemaCompatibility)
      throws Exception {
    if (schemaCompatibility != null && !schemaCompatibility.equals(SCHEMA_COMPATIBILITY_NOT_SET)) {
      log.info("Current Schema Compatibility set to {}", schemaCompatibility);
      return true;
    } else {
      return false;
    }
  }

  protected ClusterStatus getSchemaRegistryStatus(
      String environmentVal, KafkaSupportedProtocol protocol, String clusterIdentification) {

    String suffixUrl = environmentVal + "/subjects";
    Pair<String, RestTemplate> reqDetails = clusterApiUtils.getRequestDetails(suffixUrl, protocol);

    HttpEntity<Object> request = createSchemaRegistryRequest(clusterIdentification);

    try {
      reqDetails
          .getRight()
          .exchange(
              reqDetails.getLeft(), HttpMethod.GET, request, new ParameterizedTypeReference<>() {});
      return ClusterStatus.ONLINE;
    } catch (RestClientException e) {
      log.error("Exception:", e);
      return ClusterStatus.OFFLINE;
    }
  }
}
