package io.aiven.klaw.clusterapi.services;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ApiResultStatus;
import io.aiven.klaw.clusterapi.models.ClusterConnectorRequest;
import io.aiven.klaw.clusterapi.models.ClusterStatus;
import io.aiven.klaw.clusterapi.models.KafkaClustersType;
import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.utils.ClusterApiUtils;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class KafkaConnectService {

  final ClusterApiUtils clusterApiUtils;

  public KafkaConnectService(ClusterApiUtils clusterApiUtils) {
    this.clusterApiUtils = clusterApiUtils;
  }

  public Map<String, String> deleteConnector(ClusterConnectorRequest clusterConnectorRequest) {
    log.info("Into deleteConnector {}", clusterConnectorRequest);
    Map<String, String> result = new HashMap<>();

    String suffixUrl =
        clusterConnectorRequest.getEnv()
            + "/connectors/"
            + clusterConnectorRequest.getConnectorName();
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(
            suffixUrl, clusterConnectorRequest.getProtocol(), KafkaClustersType.KAFKA_CONNECT);

    try {
      reqDetails.getRight().delete(reqDetails.getLeft(), String.class);
    } catch (RestClientException e) {
      log.error("Error in deleting connector " + e.toString());
      result.put("result", ApiResultStatus.ERROR.value);
      result.put("errorText", e.toString().replaceAll("\"", ""));
      return result;
    }
    result.put("result", ApiResultStatus.SUCCESS.value);
    return result;
  }

  public Map<String, String> updateConnector(ClusterConnectorRequest clusterConnectorRequest) {
    log.info("Into updateConnector {}", clusterConnectorRequest);
    Map<String, String> result = new HashMap<>();

    String suffixUrl =
        clusterConnectorRequest.getEnv()
            + "/connectors/"
            + clusterConnectorRequest.getConnectorName()
            + "/config";
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(
            suffixUrl, clusterConnectorRequest.getProtocol(), KafkaClustersType.KAFKA_CONNECT);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<String> request =
        new HttpEntity<>(clusterConnectorRequest.getConnectorConfig(), headers);

    try {
      reqDetails.getRight().put(reqDetails.getLeft(), request, String.class);
    } catch (RestClientException e) {
      log.error("Error in updating connector " + e.toString());
      result.put("result", ApiResultStatus.ERROR.value);
      result.put("errorText", e.toString().replaceAll("\"", ""));
      return result;
    }
    result.put("result", ApiResultStatus.SUCCESS.value);

    return result;
  }

  public ApiResponse postNewConnector(ClusterConnectorRequest clusterConnectorRequest)
      throws Exception {
    log.info("Into postNewConnector clusterConnectorRequest {} ", clusterConnectorRequest);

    String suffixUrl = clusterConnectorRequest.getEnv() + "/connectors";
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(
            suffixUrl, clusterConnectorRequest.getProtocol(), KafkaClustersType.KAFKA_CONNECT);

    HttpHeaders headers = new HttpHeaders();
    headers.set("Content-Type", "application/json");

    HttpEntity<String> request =
        new HttpEntity<>(clusterConnectorRequest.getConnectorConfig(), headers);
    ResponseEntity<String> responseNew;
    try {
      responseNew =
          reqDetails.getRight().postForEntity(reqDetails.getLeft(), request, String.class);
    } catch (RestClientException e) {
      log.error("Error in registering new connector " + e.toString());
      throw new Exception(e.toString());
    }
    if (responseNew.getStatusCodeValue() == 201) {
      return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
    } else {
      return ApiResponse.builder().result(ApiResultStatus.FAILURE.value).build();
    }
  }

  public List<String> getConnectors(String environmentVal, KafkaSupportedProtocol protocol) {
    try {
      log.info("Into getConnectors {} {}", environmentVal, protocol);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/connectors";
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol, KafkaClustersType.KAFKA_CONNECT);

      Map<String, String> params = new HashMap<>();
      ResponseEntity<List> responseList =
          reqDetails.getRight().getForEntity(reqDetails.getLeft(), List.class, params);

      log.info("connectors list " + responseList);
      return responseList.getBody();
    } catch (Exception e) {
      log.error("Error in getting connectors " + e.getMessage());
      return new ArrayList<>();
    }
  }

  public LinkedHashMap<String, Object> getConnectorDetails(
      String connector, String environmentVal, KafkaSupportedProtocol protocol) {
    try {
      log.info("Into getConnectorDetails {} {}", environmentVal, protocol);
      if (environmentVal == null) {
        return null;
      }

      String suffixUrl = environmentVal + "/connectors" + "/" + connector;
      Pair<String, RestTemplate> reqDetails =
          clusterApiUtils.getRequestDetails(suffixUrl, protocol, KafkaClustersType.KAFKA_CONNECT);

      Map<String, String> params = new HashMap<>();

      ResponseEntity<LinkedHashMap> responseList =
          reqDetails.getRight().getForEntity(reqDetails.getLeft(), LinkedHashMap.class, params);
      log.info("connectors list " + responseList);

      return responseList.getBody();
    } catch (Exception e) {
      log.error("Error in getting connector detail " + e.getMessage());
      return new LinkedHashMap<>();
    }
  }

  protected ClusterStatus getKafkaConnectStatus(
      String environment, KafkaSupportedProtocol protocol) {
    String suffixUrl = environment + "/connectors";
    Pair<String, RestTemplate> reqDetails =
        clusterApiUtils.getRequestDetails(suffixUrl, protocol, KafkaClustersType.KAFKA_CONNECT);
    Map<String, String> params = new HashMap<String, String>();

    try {
      reqDetails.getRight().getForEntity(reqDetails.getLeft(), Object.class, params);
      return ClusterStatus.ONLINE;
    } catch (RestClientException e) {
      e.printStackTrace();
      return ClusterStatus.OFFLINE;
    }
  }
}
