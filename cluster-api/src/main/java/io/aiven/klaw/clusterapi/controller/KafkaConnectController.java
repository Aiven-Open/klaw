package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterConnectorRequest;
import io.aiven.klaw.clusterapi.models.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.services.KafkaConnectService;
import java.util.*;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topics")
@Slf4j
public class KafkaConnectController {

  @Autowired KafkaConnectService kafkaConnectService;

  @RequestMapping(
      value = "/getAllConnectors/{kafkaConnectHost}/{protocol}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getAllConnectors(
      @PathVariable String kafkaConnectHost, @Valid @PathVariable KafkaSupportedProtocol protocol) {
    return new ResponseEntity<>(
        kafkaConnectService.getConnectors(kafkaConnectHost, protocol), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getConnectorDetails/{connectorName}/{kafkaConnectHost}/{protocol}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<LinkedHashMap<String, Object>> getConnectorDetails(
      @PathVariable String connectorName,
      @PathVariable String kafkaConnectHost,
      @Valid @PathVariable KafkaSupportedProtocol protocol) {
    return new ResponseEntity<>(
        kafkaConnectService.getConnectorDetails(connectorName, kafkaConnectHost, protocol),
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
          ApiResponse.builder().result("Unable to register connector").build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping(value = "/updateConnector")
  public ResponseEntity<Map<String, String>> updateConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    return new ResponseEntity<>(
        kafkaConnectService.updateConnector(clusterConnectorRequest), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteConnector")
  public ResponseEntity<Map<String, String>> deleteConnector(
      @RequestBody @Valid ClusterConnectorRequest clusterConnectorRequest) {
    return new ResponseEntity<>(
        kafkaConnectService.deleteConnector(clusterConnectorRequest), HttpStatus.OK);
  }
}
