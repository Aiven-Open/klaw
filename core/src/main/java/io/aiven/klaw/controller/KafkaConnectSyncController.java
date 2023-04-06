package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncConnectorUpdates;
import io.aiven.klaw.model.response.KafkaConnectorModelResponse;
import io.aiven.klaw.service.KafkaConnectSyncControllerService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class KafkaConnectSyncController {

  @Autowired KafkaConnectSyncControllerService kafkaConnectControllerService;

  @PostMapping(value = "/updateSyncConnectors")
  public ResponseEntity<ApiResponse> updateSyncConnectors(
      @RequestBody List<SyncConnectorUpdates> syncConnectorUpdates) throws KlawException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.updateSyncConnectors(syncConnectorUpdates), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getConnectorDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getConnectorDetails(
      @RequestParam("env") String envId, @RequestParam("connectorName") String connectorName)
      throws KlawException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.getConnectorDetails(connectorName, envId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncConnectors",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KafkaConnectorModelResponse>> getSyncTopics(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "connectornamesearch", required = false) String connectorNameSearch,
      @RequestParam(value = "isBulkOption", defaultValue = "false", required = false)
          String isBulkOption)
      throws Exception {
    return new ResponseEntity<>(
        kafkaConnectControllerService.getSyncConnectors(
            envId, pageNo, currentPage, connectorNameSearch, Boolean.parseBoolean(isBulkOption)),
        HttpStatus.OK);
  }
}
