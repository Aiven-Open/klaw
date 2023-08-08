package io.aiven.klaw.clusterapi.services;

import static io.aiven.klaw.clusterapi.models.error.ClusterApiErrorMessages.CLUSTER_API_ERR_1;
import static io.aiven.klaw.clusterapi.models.error.ClusterApiErrorMessages.CLUSTER_API_ERR_2;
import static io.aiven.klaw.clusterapi.models.error.ClusterApiErrorMessages.CLUSTER_API_ERR_3;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterConnectorRequest;
import io.aiven.klaw.clusterapi.models.connect.ConnectorState;
import io.aiven.klaw.clusterapi.models.connect.ConnectorsStatus;
import io.aiven.klaw.clusterapi.models.connect.Status;
import io.aiven.klaw.clusterapi.models.enums.ClusterStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.error.RestErrorResponse;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class KafkaConnectService {

  private static final ParameterizedTypeReference<Map<String, Map<String, Object>>>
      GET_CONNECTORS_TYPEREF = new ParameterizedTypeReference<>() {};

  private static final ParameterizedTypeReference<List<String>> GET_CONNECTORS_STR_TYPEREF =
      new ParameterizedTypeReference<>() {};
  private static final ParameterizedTypeReference<Map<String, Object>>
      GET_CONNECTOR_DETAILS_TYPEREF = new ParameterizedTypeReference<>() {};
  public static final String FAILED_STATUS = "FAILED";
  public static final String RUNNING_STATUS = "RUNNING";
  public static final String CONNECTOR_URI_EXPAND_STATUS = "?expand=status";

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
      return buildErrorResponseFromRestException(e, CLUSTER_API_ERR_3);
    } catch (RestClientException ex) {
      log.error("Error in deleting connector ", ex);
      return ApiResponse.notOk(CLUSTER_API_ERR_3);
    }
    return ApiResponse.SUCCESS;
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
      return buildErrorResponseFromRestException(e, CLUSTER_API_ERR_2);
    } catch (Exception ex) {
      return ApiResponse.notOk(CLUSTER_API_ERR_2);
    }
    return ApiResponse.SUCCESS;
  }

  private static ApiResponse buildErrorResponseFromRestException(
      HttpStatusCodeException e, String defaultErrorMsg) {
    RestErrorResponse errorResponse = null;
    try {
      errorResponse = e.getResponseBodyAs(RestErrorResponse.class);
    } catch (Exception ex) {
      log.error("Error caught trying to process the error response. ", ex);
    }
    return ApiResponse.notOk(errorResponse == null ? defaultErrorMsg : errorResponse.getMessage());
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

      return buildErrorResponseFromRestException(e, CLUSTER_API_ERR_1);
    } catch (Exception ex) {
      return ApiResponse.notOk(CLUSTER_API_ERR_1);
    }
    return responseNew.getStatusCodeValue() == 201 ? ApiResponse.SUCCESS : ApiResponse.FAILURE;
  }

  public ConnectorsStatus getConnectors(
      String environmentVal,
      KafkaSupportedProtocol protocol,
      String clusterIdentification,
      boolean getConnectorStatuses) {
    ConnectorsStatus connectorsStatus = new ConnectorsStatus();
    List<ConnectorState> connectorStateList = new ArrayList<>();
    connectorsStatus.setConnectorStateList(connectorStateList);

    try {
      log.info("Into getConnectors {} {}", environmentVal, protocol);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/connectors";

      if (getConnectorStatuses) {
        suffixUrl = suffixUrl + CONNECTOR_URI_EXPAND_STATUS;
      }

      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol);

      HttpHeaders headers =
          clusterApiUtils.createHeaders(clusterIdentification, KafkaClustersType.KAFKA_CONNECT);
      HttpEntity<Object> request = new HttpEntity<>(headers);
      Map<String, String> params = new HashMap<>();

      if (!getConnectorStatuses) {
        ResponseEntity<List<String>> responseList =
            reqDetails
                .getRight()
                .exchange(
                    reqDetails.getLeft(),
                    HttpMethod.GET,
                    request,
                    GET_CONNECTORS_STR_TYPEREF,
                    params);
        log.info("connectors list " + responseList);
        if (responseList.getBody() != null) {
          for (String connectorName : responseList.getBody()) {
            ConnectorState connectorState = new ConnectorState();
            connectorState.setConnectorName(connectorName);
            connectorStateList.add(connectorState);
          }
        }

        return connectorsStatus;
      }

      ResponseEntity<Map<String, Map<String, Status>>> responseEntity =
          reqDetails
              .getRight()
              .exchange(
                  reqDetails.getLeft(),
                  HttpMethod.GET,
                  request,
                  new ParameterizedTypeReference<>() {},
                  params);
      Map<String, Map<String, Status>> responseBody = responseEntity.getBody();

      for (String connectorName : Objects.requireNonNull(responseBody).keySet()) {
        Map<String, Status> statusMap = responseBody.get(connectorName);
        Status statusConnector = statusMap.get("status");
        long failedTasksCount =
            statusConnector.getTasks().stream()
                .filter(task -> task.getState().equals(FAILED_STATUS))
                .count();
        long runningTasksCount =
            statusConnector.getTasks().stream()
                .filter(task -> task.getState().equals(RUNNING_STATUS))
                .count();

        ConnectorState connectorState = new ConnectorState();
        connectorState.setConnectorName(connectorName);
        connectorState.setConnectorStatus(statusConnector.getConnector().getState());
        connectorState.setRunningTasks(runningTasksCount);
        connectorState.setFailedTasks(failedTasksCount);
        connectorStateList.add(connectorState);
      }
      connectorsStatus.setConnectorStateList(connectorStateList);

      log.info("connectors list " + responseEntity);
      return connectorsStatus;
    } catch (Exception e) {
      log.error("Error in getting connectors " + e);
      return connectorsStatus;
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

  public ApiResponse restartConnector(ClusterConnectorRequest clusterConnectorRequest) {
    log.info("Into restartConnector clusterConnectorRequest {} ", clusterConnectorRequest);

    String suffixUrl =
        clusterConnectorRequest.getEnv()
            + "/connectors"
            + "/"
            + clusterConnectorRequest.getConnectorName()
            + "/"
            + "restart";

    suffixUrl =
        suffixUrl
            + "?includeTasks=true&onlyFailed="
            + clusterConnectorRequest.isIncludeFailedTasksOnly();

    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, clusterConnectorRequest.getProtocol());

    HttpHeaders headers =
        clusterApiUtils.createHeaders(
            clusterConnectorRequest.getClusterIdentification(), KafkaClustersType.KAFKA_CONNECT);
    headers.set("Content-Type", "application/json");
    HttpEntity<String> request = new HttpEntity<>(headers);
    ResponseEntity<String> responseNew;
    try {
      responseNew =
          reqDetails.getRight().postForEntity(reqDetails.getLeft(), request, String.class);
    } catch (HttpServerErrorException | HttpClientErrorException e) {
      return buildErrorResponseFromRestException(e, CLUSTER_API_ERR_1);
    } catch (Exception ex) {
      return ApiResponse.notOk(CLUSTER_API_ERR_1);
    }
    return responseNew.getStatusCode().is2xxSuccessful()
        ? ApiResponse.SUCCESS
        : ApiResponse.FAILURE;
  }

  public ApiResponse pauseConnector(ClusterConnectorRequest clusterConnectorRequest) {
    return ApiResponse.notOk("To be implemented");
  }

  public ApiResponse resumeConnector(ClusterConnectorRequest clusterConnectorRequest) {
    return ApiResponse.notOk("To be implemented");
  }
}
