package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterConnectorRequest;
import io.aiven.klaw.clusterapi.models.connect.ConnectorsStatus;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.services.KafkaConnectService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@Slf4j
public class KafkaConnectController {

  @Autowired KafkaConnectService kafkaConnectService;

  @RequestMapping(
      value = "/getAllConnectors/{kafkaConnectHost}/{protocol}/{clusterIdentification}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ConnectorsStatus> getAllConnectors(
      @PathVariable String kafkaConnectHost,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterIdentification,
      @RequestParam("connectorStatus") boolean connectorStatus) {
    return new ResponseEntity<>(
        kafkaConnectService.getConnectors(
            kafkaConnectHost, protocol, clusterIdentification, connectorStatus),
        HttpStatus.OK);
  }

  @RequestMapping(
      value =
          "/getConnectorDetails/{connectorName}/{kafkaConnectHost}/{protocol}/{clusterIdentification}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, Object>> getConnectorDetails(
      @PathVariable String connectorName,
      @PathVariable String kafkaConnectHost,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterIdentification) {
    return new ResponseEntity<>(
        kafkaConnectService.getConnectorDetails(
            connectorName, kafkaConnectHost, protocol, clusterIdentification),
        HttpStatus.OK);
  }

  @PostMapping(value = "/postConnector")
  public ResponseEntity<ApiResponse> postConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    try {
      ApiResponse result = kafkaConnectService.postNewConnector(clusterConnectorRequest);
      return new ResponseEntity<>(result, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(
          ApiResponse.builder().success(false).message(e.getMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(value = "/updateConnector")
  public ResponseEntity<ApiResponse> updateConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    return new ResponseEntity<>(
        kafkaConnectService.updateConnector(clusterConnectorRequest), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteConnector")
  public ResponseEntity<ApiResponse> deleteConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    return new ResponseEntity<>(
        kafkaConnectService.deleteConnector(clusterConnectorRequest), HttpStatus.OK);
  }

  @PostMapping(value = "/connector/restart")
  public ResponseEntity<ApiResponse> restartConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    return new ResponseEntity<>(
        kafkaConnectService.restartConnector(clusterConnectorRequest), HttpStatus.OK);
  }

  @PostMapping(value = "/connector/pause")
  public ResponseEntity<ApiResponse> pauseConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    return new ResponseEntity<>(
        kafkaConnectService.pauseConnector(clusterConnectorRequest), HttpStatus.OK);
  }

  @PostMapping(
      value = "/connector/resume",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> resumeConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    return new ResponseEntity<>(
        kafkaConnectService.resumeConnector(clusterConnectorRequest), HttpStatus.OK);
  }
}
