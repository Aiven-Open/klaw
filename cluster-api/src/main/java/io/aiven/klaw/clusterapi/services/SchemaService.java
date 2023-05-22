package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.RegisterSchemaCustomResponse;
import io.aiven.klaw.clusterapi.models.RegisterSchemaResponse;
import io.aiven.klaw.clusterapi.models.SchemaCompatibilityCheckResponse;
import io.aiven.klaw.clusterapi.models.SchemaOfTopic;
import io.aiven.klaw.clusterapi.models.SchemasOfClusterResponse;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
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

  private static final ParameterizedTypeReference<Set<Integer>> GET_SCHEMAVERSIONS_TYPEREF =
      new ParameterizedTypeReference<>() {};

  private static final ParameterizedTypeReference<List<String>> GET_SUBJECTS_TYPEREF =
      new ParameterizedTypeReference<>() {};

  public static final String SCHEMA_REGISTRY_CONTENT_TYPE =
      "application/vnd.schemaregistry.v1+json";
  public static final String SCHEMA_COMPATIBILITY_NOT_SET = "NOT SET";
  public static final String SCHEMA_VALUE_URI = "-value";

  public static final String SCHEMA_SUBJECTS_URI = "subjects";

  public static final String TOPIC_COMPATIBILITY_URI_TEMPLATE =
      "/compatibility/subjects/{topic_name}-value/versions/latest";

  public static final String TOPIC_GET_VERSIONS_URI_TEMPLATE =
      "/subjects/{topic_name}-value/versions";

  @Value("${klaw.schemaregistry.compatibility.default:BACKWARD}")
  private String defaultSchemaCompatibility;

  final ClusterApiUtils clusterApiUtils;

  public SchemaService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public synchronized ApiResponse registerSchema(ClusterSchemaRequest clusterSchemaRequest) {
    String schemaCompatibility = null;
    boolean schemaCompatibilitySetOnSubject = false;
    try {
      log.debug(
          "RegisterSchema on {} isForceRegisterEnabled {}",
          clusterSchemaRequest.getTopicName(),
          clusterSchemaRequest.isForceRegister());
      if (clusterSchemaRequest.isForceRegister()) {
        schemaCompatibility =
            getSubjectSchemaCompatibility(
                clusterSchemaRequest.getEnv(),
                clusterSchemaRequest.getTopicName(),
                clusterSchemaRequest.getProtocol(),
                clusterSchemaRequest.getClusterIdentification());
        // Verify if schema compatibility is set on subject
        schemaCompatibilitySetOnSubject = checkIfSchemaCompatibilitySet(schemaCompatibility);
        log.debug(
            "RegisterSchema - original Schema Compatibility {} for Topic {}",
            schemaCompatibility,
            clusterSchemaRequest.getTopicName());
        // set subject compatibility to NONE, if it's not NONE, or NOT SET
        if (!schemaCompatibilitySetOnSubject || !schemaCompatibility.equals("NONE")) {
          setSchemaCompatibility(
              clusterSchemaRequest.getEnv(),
              clusterSchemaRequest.getTopicName(),
              clusterSchemaRequest.isForceRegister(),
              clusterSchemaRequest.getProtocol(),
              clusterSchemaRequest.getClusterIdentification(),
              null);
        }
      }

      RegisterSchemaCustomResponse registerSchemaCustomResponse =
          registerSchemaPostCall(clusterSchemaRequest);

      return ApiResponse.builder()
          .success(true)
          .message(ApiResultStatus.SUCCESS.value)
          .data(registerSchemaCustomResponse)
          .build();
    } catch (Exception e) {
      log.error("Exception:", e);
      if (e instanceof HttpClientErrorException
          && ((HttpClientErrorException.Conflict) e).getStatusCode().value() == 409) {
        return ApiResponse.builder()
            .success(false)
            .message("Schema being registered is incompatible with an earlier schema")
            .build();
      }
      return ApiResponse.builder()
          .success(false)
          .message("Failure in registering schema." + e.getMessage())
          .build();
    } finally {
      // Ensure the Schema compatibility is returned to previous setting before the force update.
      resetCompatibilityOnSubject(
          clusterSchemaRequest, schemaCompatibility, schemaCompatibilitySetOnSubject);
    }
  }

  private void resetCompatibilityOnSubject(
      ClusterSchemaRequest clusterSchemaRequest,
      String schemaCompatibility,
      boolean subjectSchemaCompatibilitySet) {
    if (clusterSchemaRequest.isForceRegister()) {
      if (!subjectSchemaCompatibilitySet) // check if subject compatibility is not set before
      {
        // get global compatibility on subject level
        schemaCompatibility =
            getGlobalSchemaCompatibility(
                clusterSchemaRequest.getEnv(),
                clusterSchemaRequest.getProtocol(),
                clusterSchemaRequest.getClusterIdentification());
      }
      if (schemaCompatibility != null
          && !schemaCompatibility.equals(SCHEMA_COMPATIBILITY_NOT_SET)) {
        log.info(
            "RegisterSchema - Force Commit revert to original Schema Compatibility(Subject/Global) {} for Topic {}",
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

  private RegisterSchemaCustomResponse registerSchemaPostCall(
      ClusterSchemaRequest clusterSchemaRequest) throws Exception {
    String suffixUrl =
        clusterSchemaRequest.getEnv()
            + "/"
            + SCHEMA_SUBJECTS_URI
            + "/"
            + clusterSchemaRequest.getTopicName()
            + SCHEMA_VALUE_URI
            + "/versions";
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, clusterSchemaRequest.getProtocol());

    HttpEntity<Map<String, String>> request =
        buildSchemaEntity(
            clusterSchemaRequest.getFullSchema(), clusterSchemaRequest.getClusterIdentification());

    ResponseEntity<RegisterSchemaResponse> schemaResponseResponseEntity =
        reqDetails
            .getRight()
            .postForEntity(reqDetails.getLeft(), request, RegisterSchemaResponse.class);
    List<Integer> versionsListAfter =
        new ArrayList<>(
            Objects.requireNonNull(
                getSchemaVersions(
                    clusterSchemaRequest.getEnv(),
                    clusterSchemaRequest.getTopicName(),
                    clusterSchemaRequest.getProtocol(),
                    clusterSchemaRequest.getClusterIdentification())));

    RegisterSchemaResponse registerSchemaResponse = schemaResponseResponseEntity.getBody();
    RegisterSchemaCustomResponse registerSchemaCustomResponse = new RegisterSchemaCustomResponse();
    if (registerSchemaResponse != null) {
      registerSchemaCustomResponse.setId(registerSchemaResponse.getId());
    } else {
      throw new Exception("Failure in registering schema.");
    }

    registerSchemaCustomResponse.setVersion(
        versionsListAfter.get(versionsListAfter.size() - 1)); // set the last element of array
    registerSchemaCustomResponse.setSchemaRegistered(true);

    log.debug("SchemaRequest response body {}", schemaResponseResponseEntity.getBody());
    return registerSchemaCustomResponse;
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

      Set<Integer> versionsList =
          getSchemaVersions(environmentVal, topicName, protocol, clusterIdentification);
      String schemaCompatibility =
          getSubjectSchemaCompatibility(environmentVal, topicName, protocol, clusterIdentification);
      if (Objects.equals(schemaCompatibility, SCHEMA_COMPATIBILITY_NOT_SET)) {
        schemaCompatibility =
            getGlobalSchemaCompatibility(environmentVal, protocol, clusterIdentification);
      }
      Map<Integer, Map<String, Object>> allSchemaObjects = new TreeMap<>();

      if (versionsList != null) {
        for (Integer schemaVersion : versionsList) {
          String suffixUrl =
              environmentVal
                  + "/"
                  + SCHEMA_SUBJECTS_URI
                  + "/"
                  + topicName
                  + SCHEMA_VALUE_URI
                  + "/versions/"
                  + schemaVersion;
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

  private Set<Integer> getSchemaVersions(
      String environmentVal,
      String topicName,
      KafkaSupportedProtocol protocol,
      String clusterIdentification) {
    try {
      log.info("Into getSchema versions {} {}", topicName, environmentVal);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl =
          environmentVal
              + "/"
              + SCHEMA_SUBJECTS_URI
              + "/"
              + topicName
              + SCHEMA_VALUE_URI
              + "/versions";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol);

      Map<String, String> params = new HashMap<>();
      HttpEntity<Object> request = createSchemaRegistryRequest(clusterIdentification);

      ResponseEntity<Set<Integer>> responseList =
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
      return Collections.emptySet();
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
      return getCompatibility(protocol, clusterIdentification, suffixUrl);
    } catch (Exception e) {
      log.error("Error in getting schema compatibility ", e);
      return SCHEMA_COMPATIBILITY_NOT_SET;
    }
  }

  private String getGlobalSchemaCompatibility(
      String environmentVal, KafkaSupportedProtocol protocol, String clusterIdentification) {
    try {
      log.info("Into global getSchema compatibility {}", environmentVal);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/config";
      return getCompatibility(protocol, clusterIdentification, suffixUrl);
    } catch (Exception e) {
      log.error("Error in getting schema compatibility ", e);
      return SCHEMA_COMPATIBILITY_NOT_SET;
    }
  }

  private String getCompatibility(
      KafkaSupportedProtocol protocol, String clusterIdentification, String suffixUrl) {
    Pair<String, RestTemplate> reqDetails = clusterApiUtils.getRequestDetails(suffixUrl, protocol);

    ResponseEntity<Map<String, String>> responseList =
        getSubjectSchemaCompatibilityRequest(
            reqDetails, new HashMap<>(), createSchemaRegistryRequest(clusterIdentification));
    log.info("Schema compatibility " + responseList);
    return responseList.getBody().get("compatibilityLevel");
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

      String suffixUrl = environmentVal + "/config/" + topicName + SCHEMA_VALUE_URI;

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

  private boolean checkIfSchemaCompatibilitySet(String schemaCompatibility) throws Exception {
    if (schemaCompatibility != null && !schemaCompatibility.equals(SCHEMA_COMPATIBILITY_NOT_SET)) {
      log.info("Current Schema Compatibility set to {}", schemaCompatibility);
      return true;
    } else {
      return false;
    }
  }

  protected ClusterStatus getSchemaRegistryStatus(
      String environmentVal, KafkaSupportedProtocol protocol, String clusterIdentification) {

    String suffixUrl = environmentVal + "/" + SCHEMA_SUBJECTS_URI;
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

  public ApiResponse deleteSchema(ClusterTopicRequest clusterTopicRequest) {
    String suffixUrl =
        clusterTopicRequest.getSchemaEnv()
            + "/subjects/"
            + clusterTopicRequest.getTopicName()
            + SCHEMA_VALUE_URI;
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, clusterTopicRequest.getSchemaEnvProtocol());
    HttpEntity<Object> request =
        createSchemaRegistryRequest(clusterTopicRequest.getSchemaClusterIdentification());

    try {
      reqDetails
          .getRight()
          .exchange(
              reqDetails.getLeft(),
              HttpMethod.DELETE,
              request,
              new ParameterizedTypeReference<>() {});
      log.info("Schema deleted {}", clusterTopicRequest);
      return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
    } catch (RestClientException e) {
      log.error("Exception:", e);
      return ApiResponse.builder()
          .success(false)
          .message("Schema deletion failure " + e.getMessage())
          .build();
    }
  }

  public ApiResponse checkSchemaCompatibility(
      String schema,
      String topicName,
      KafkaSupportedProtocol schemaProtocol,
      String schemaEnv,
      String clusterIdentification) {
    try {
      log.info("Check Schema Compatibility for TopicName: {}", topicName);
      if (isFirstSchema(topicName, schemaProtocol, schemaEnv, clusterIdentification)) {
        return ApiResponse.builder().success(true).message("No Existing Schemas").build();
      }

      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(
              schemaEnv + TOPIC_COMPATIBILITY_URI_TEMPLATE.replace("{topic_name}", topicName),
              schemaProtocol);

      HttpEntity<Map<String, String>> request = buildSchemaEntity(schema, clusterIdentification);
      ResponseEntity<SchemaCompatibilityCheckResponse> compatibility =
          reqDetails
              .getRight()
              .postForEntity(reqDetails.getLeft(), request, SchemaCompatibilityCheckResponse.class);
      if (compatibility.hasBody()
          && Objects.requireNonNull(compatibility.getBody()).isCompatible()) {
        return ApiResponse.builder()
            .success(true)
            .message(ApiResultStatus.SUCCESS.value + " Schema is compatible.")
            .build();
      } else {
        return ApiResponse.builder()
            .success(false)
            .message(ApiResultStatus.FAILURE.value + "  Schema is not compatible.")
            .build();
      }
    } catch (HttpClientErrorException httpEx) {
      log.error("Exception on validating: ", httpEx);

      if (httpEx.getStatusCode().equals(HttpStatusCode.valueOf(422))) {

        return ApiResponse.builder()
            .success(false)
            .message(
                ApiResultStatus.FAILURE.value
                    + " Invalid Schema. Unable to validate Schema Compatibility.")
            .build();
      } else {
        throw httpEx;
      }
    } catch (Exception ex) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.FAILURE.value + " Unable to validate Schema Compatibility.")
          .build();
    }
  }

  public boolean isFirstSchema(
      String topicName,
      KafkaSupportedProtocol schemaProtocol,
      String schemaEnv,
      String clusterIdentification) {

    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(
            schemaEnv + TOPIC_GET_VERSIONS_URI_TEMPLATE.replace("{topic_name}", topicName),
            schemaProtocol);
    try {

      reqDetails
          .getRight()
          .exchange(
              reqDetails.getLeft(),
              HttpMethod.GET,
              new HttpEntity(
                  clusterApiUtils.createHeaders(
                      clusterIdentification, KafkaClustersType.SCHEMA_REGISTRY)),
              Integer[].class);

    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode().equals(HttpStatusCode.valueOf(404))) {
        log.info("No existing Schema Found for Topic: {}", topicName);
        return true;
      } else {
        throw ex;
      }
    }
    return false;
  }

  private HttpEntity<Map<String, String>> buildSchemaEntity(
      String schema, String clusterIdentification) {
    Map<String, String> params = new HashMap<>();
    params.put("schema", schema);
    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.SCHEMA_REGISTRY);
    headers.set("Content-Type", SCHEMA_REGISTRY_CONTENT_TYPE);
    return new HttpEntity<>(params, headers);
  }

  public SchemasOfClusterResponse getSchemasOfCluster(
      String bootstrapServers, KafkaSupportedProtocol protocol, String clusterIdentification) {
    log.info(
        "bootstrapServers {} protocol {} clusterIdentification {}",
        bootstrapServers,
        protocol,
        clusterIdentification);
    SchemasOfClusterResponse schemasOfClusterResponse = new SchemasOfClusterResponse();
    String suffixUrl = bootstrapServers + "/" + SCHEMA_SUBJECTS_URI;
    Pair<String, RestTemplate> reqDetails = clusterApiUtils.getRequestDetails(suffixUrl, protocol);

    Map<String, String> params = new HashMap<>();
    HttpEntity<Object> request = createSchemaRegistryRequest(clusterIdentification);

    ResponseEntity<List<String>> responseList =
        reqDetails
            .getRight()
            .exchange(reqDetails.getLeft(), HttpMethod.GET, request, GET_SUBJECTS_TYPEREF, params);

    List<SchemaOfTopic> schemaOfTopicList = new ArrayList<>();
    List<String> subjectList = responseList.getBody();
    if (subjectList != null) {
      for (String subject : subjectList) {
        if (subject.indexOf(SCHEMA_VALUE_URI) > 0) {
          subject = subject.substring(0, subject.indexOf(SCHEMA_VALUE_URI));
          Set<Integer> schemaVersions =
              getSchemaVersions(bootstrapServers, subject, protocol, clusterIdentification);
          SchemaOfTopic schemaOfTopic = new SchemaOfTopic();
          schemaOfTopic.setTopic(subject);
          schemaOfTopic.setSchemaVersions(schemaVersions);
          schemaOfTopicList.add(schemaOfTopic);
        }
      }
    }
    schemasOfClusterResponse.setSchemaOfTopicList(schemaOfTopicList);
    return schemasOfClusterResponse;
  }
}
