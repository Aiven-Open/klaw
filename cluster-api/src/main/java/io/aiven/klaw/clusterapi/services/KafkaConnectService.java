package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterConnectorRequest;
import io.aiven.klaw.clusterapi.models.enums.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.error.RestErrorResponse;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class KafkaConnectService {

  private static final ParameterizedTypeReference<List<String>> GET_CONNECTORS_TYPEREF =
      new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<Map<String, Object>>
      GET_CONNECTOR_DETAILS_TYPEREF = new ParameterizedTypeReference<>() {};
  public static final String UNABLE_TO_CREATE_CONNECTOR_ON_CLUSTER =
      "Unable to create Connector on Cluster.";
  public static final String UNABLE_TO_UPDATE_CONNECTOR_ON_CLUSTER =
      "Unable to update Connector on Cluster";
  public static final String UNABLE_TO_DELETE_CONNECTOR_ON_CLUSTER = "Unable To Delete Connector on Cluster.";

  final ClusterApiUtils clusterApiUtils;

  public KafkaConnectService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public ApiResponse deleteConnector(ClusterConnectorRequest clusterConnectorRequest) {
    log.info("Into deleteConnector {}", clusterConnectorRequest);
    String suffixUrl =
        clusterConnectorRequest.getEnv()
            + "/connectors/"
            + clusterConnectorRequest.getConnectorName();
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, clusterConnectorRequest.getProtocol());
    HttpHeaders headers =
        clusterApiUtils.createHeaders(
            clusterConnectorRequest.getClusterIdentification(), KafkaClustersType.KAFKA_CONNECT);
    HttpEntity<Object> request = new HttpEntity<>(headers);

    try {
      reqDetails
          .getRight()
          .exchange(
              reqDetails.getLeft(),
              HttpMethod.DELETE,
              request,
              new ParameterizedTypeReference<>() {});
    } catch (HttpServerErrorException | HttpClientErrorException e) {
      log.error("Error in deleting connector ", e);
      RestErrorResponse errorResponse = e.getResponseBodyAs(RestErrorResponse.class);
      if (errorResponse != null) {
        return ApiResponse.builder().success(false).message(errorResponse.getMessage()).build();
      } else {
        return ApiResponse.builder()
            .success(false)
            .message(UNABLE_TO_DELETE_CONNECTOR_ON_CLUSTER)
            .build();
      }
    }
    return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
  }

  public ApiResponse updateConnector(ClusterConnectorRequest clusterConnectorRequest) {
    log.info("Into updateConnector {}", clusterConnectorRequest);
    String suffixUrl =
        clusterConnectorRequest.getEnv()
            + "/connectors/"
            + clusterConnectorRequest.getConnectorName()
            + "/config";
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, clusterConnectorRequest.getProtocol());

    HttpHeaders headers =
        clusterApiUtils.createHeaders(
            clusterConnectorRequest.getClusterIdentification(), KafkaClustersType.KAFKA_CONNECT);
    headers.set("Content-Type", "application/json");
    HttpEntity<String> request =
        new HttpEntity<>(clusterConnectorRequest.getConnectorConfig(), headers);

    try {
      reqDetails.getRight().put(reqDetails.getLeft(), request, String.class);
    } catch (HttpServerErrorException | HttpClientErrorException e) {
      log.error("Error in updating connector ", e);
      RestErrorResponse errorResponse = e.getResponseBodyAs(RestErrorResponse.class);
      if (errorResponse != null) {
        return ApiResponse.builder().success(false).message(errorResponse.getMessage()).build();
      } else {
        return ApiResponse.builder()
            .success(false)
            .message(UNABLE_TO_UPDATE_CONNECTOR_ON_CLUSTER)
            .build();
      }
    }
    return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
  }

  public ApiResponse postNewConnector(ClusterConnectorRequest clusterConnectorRequest)
      throws Exception {
    log.info("Into postNewConnector clusterConnectorRequest {} ", clusterConnectorRequest);

    String suffixUrl = clusterConnectorRequest.getEnv() + "/connectors";
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, clusterConnectorRequest.getProtocol());

    HttpHeaders headers =
        clusterApiUtils.createHeaders(
            clusterConnectorRequest.getClusterIdentification(), KafkaClustersType.KAFKA_CONNECT);
    headers.set("Content-Type", "application/json");

    HttpEntity<String> request =
        new HttpEntity<>(clusterConnectorRequest.getConnectorConfig(), headers);
    ResponseEntity<String> responseNew;
    try {
      responseNew =
          reqDetails.getRight().postForEntity(reqDetails.getLeft(), request, String.class);
    } catch (HttpServerErrorException | HttpClientErrorException e) {

      RestErrorResponse errorResponse = e.getResponseBodyAs(RestErrorResponse.class);
      if (errorResponse != null) {
        throw new Exception(errorResponse.getMessage());
      } else {
        throw new Exception(UNABLE_TO_CREATE_CONNECTOR_ON_CLUSTER);
      }
    }
    if (responseNew.getStatusCodeValue() == 201) {
      return ApiResponse.builder().success(true).message(ApiResultStatus.SUCCESS.value).build();
    } else {
      return ApiResponse.builder().success(false).message(ApiResultStatus.FAILURE.value).build();
    }
  }

  public List<String> getConnectors(
      String environmentVal, KafkaSupportedProtocol protocol, String clusterIdentification) {
    try {
      log.info("Into getConnectors {} {}", environmentVal, protocol);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/connectors";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol);

      HttpHeaders headers =
          clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.KAFKA_CONNECT);
      HttpEntity<Object> request = new HttpEntity<>(headers);

      Map<String, String> params = new HashMap<>();
      ResponseEntity<List<String>> responseList =
          reqDetails
              .getRight()
              .exchange(
                  reqDetails.getLeft(), HttpMethod.GET, request, GET_CONNECTORS_TYPEREF, params);
      log.info("connectors list " + responseList);
      return responseList.getBody();
    } catch (Exception e) {
      log.error("Error in getting connectors " + e);
      return Collections.emptyList();
    }
  }

  public Map<String, Object> getConnectorDetails(
      String connector,
      String environmentVal,
      KafkaSupportedProtocol protocol,
      String clusterIdentification) {
    try {
      log.info("Into getConnectorDetails {} {}", environmentVal, protocol);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/connectors" + "/" + connector;
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol);

      HttpHeaders headers =
          clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.KAFKA_CONNECT);
      HttpEntity<Object> request = new HttpEntity<>(headers);
      Map<String, String> params = new HashMap<>();

      ResponseEntity<Map<String, Object>> responseList =
          reqDetails
              .getRight()
              .exchange(
                  reqDetails.getLeft(),
                  HttpMethod.GET,
                  request,
                  GET_CONNECTOR_DETAILS_TYPEREF,
                  params);
      log.info("connectors list " + responseList);

      return responseList.getBody();
    } catch (Exception e) {
      log.error("Error in getting connector detail ", e);
      return Collections.emptyMap();
    }
  }

  protected ClusterStatus getKafkaConnectStatus(
      String environment, KafkaSupportedProtocol protocol, String clusterIdentification) {
    String suffixUrl = environment + "/connectors";
    Pair<String, RestTemplate> reqDetails = clusterApiUtils.getRequestDetails(suffixUrl, protocol);
    HttpHeaders headers =
        clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.KAFKA_CONNECT);
    HttpEntity<Object> request = new HttpEntity<>(headers);

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
