package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawRestException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.KafkaConnectorDeleteRequestModel;
import io.aiven.klaw.model.requests.KafkaConnectorModel;
import io.aiven.klaw.model.requests.KafkaConnectorRequestModel;
import io.aiven.klaw.model.requests.KafkaConnectorRestartModel;
import io.aiven.klaw.model.response.ConnectorOverview;
import io.aiven.klaw.model.response.ConnectorOverviewPerEnv;
import io.aiven.klaw.model.response.KafkaConnectorModelResponse;
import io.aiven.klaw.model.response.KafkaConnectorRequestsResponseModel;
import io.aiven.klaw.service.KafkaConnectControllerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class KafkaConnectController {

  @Autowired KafkaConnectControllerService kafkaConnectControllerService;

  @PostMapping(
      value = "/createConnector",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createConnectorRequest(
      @Valid @RequestBody KafkaConnectorRequestModel addTopicRequest) throws KlawException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.createConnectorRequest(addTopicRequest), HttpStatus.OK);
  }

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'CREATED' or 'DELETED'
   * @param env The name of the environment you would like returned e.g. '1' or '4'
   * @param search A wildcard search term that searches topicNames.
   * @param requestOperationType is a filter to only return requests of a certain operation type
   *     e.g. CREATE/UPDATE/PROMOTE/CLAIM/DELETE
   * @param order allows the requestor to specify what order the pagination should be returned in *
   *     OLDEST_FIRST/NEWEST_FIRST
   * @return A List of Kafka Connector Requests filtered by the provided parameters.
   */
  @RequestMapping(
      value = "/getConnectorRequestsForApprover",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KafkaConnectorRequestsResponseModel>> getCreatedConnectorRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "CREATED") RequestStatus requestStatus,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "order", required = false, defaultValue = "DESC_REQUESTED_TIME")
          Order order,
      @RequestParam(value = "operationType", required = false)
          RequestOperationType requestOperationType,
      @RequestParam(value = "search", required = false) String search) {
    return new ResponseEntity<>(
        kafkaConnectControllerService.getCreatedConnectorRequests(
            pageNo, currentPage, requestStatus.value, env, order, requestOperationType, search),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/deleteConnectorRequests",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteConnectorRequests(
      @RequestParam("connectorId") String connectorId) throws KlawException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.deleteConnectorRequests(connectorId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execConnectorRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> approveTopicRequests(
      @RequestParam("connectorId") String connectorId) throws KlawException, KlawRestException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.approveConnectorRequests(connectorId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execConnectorRequestsDecline",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> declineConnectorRequests(
      @RequestParam("connectorId") String connectorId,
      @RequestParam("reasonForDecline") String reasonForDecline)
      throws KlawException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.declineConnectorRequests(connectorId, reasonForDecline),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/createConnectorDeleteRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createConnectorDeleteRequest(
      @RequestBody @Valid KafkaConnectorDeleteRequestModel deleteRequestModel)
      throws KlawException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.createConnectorDeleteRequest(
            deleteRequestModel.getConnectorName(), deleteRequestModel.getEnvId()),
        HttpStatus.OK);
  }

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'CREATED' or
   *     'DELETED' @Param operationType The RequestOperationType Create/Update/Promote/Claim/Delete
   * @param env The name of the environment you would like returned e.g. '1' or '4' @Param search A
   *     wildcard search on the topic name allowing
   * @param order allows the requestor to specify what order the pagination should be returned in
   *     OLDEST_FIRST/NEWEST_FIRST @Param search A wildcard search that filters by a partial case
   *     insensitive match
   * @param requestOperationType is a filter to only return requests of a certain operation type
   *     e.g. CREATE/UPDATE/PROMOTE/CLAIM/DELETE
   * @param search A wildcard search term that searches topicNames.
   * @param isMyRequest Only return requests created by the user calling the API
   * @return A list of Kafka Connector requests
   */
  @RequestMapping(
      value = "/getConnectorRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KafkaConnectorRequestsResponseModel>> getConnectorRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "ALL") RequestStatus requestStatus,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "order", required = false, defaultValue = "DESC_REQUESTED_TIME")
          Order order,
      @RequestParam(value = "operationType", required = false)
          RequestOperationType requestOperationType,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "isMyRequest", required = false, defaultValue = "false")
          boolean isMyRequest) {
    return new ResponseEntity<>(
        kafkaConnectControllerService.getConnectorRequests(
            pageNo,
            currentPage,
            requestStatus,
            requestOperationType,
            env,
            order,
            search,
            isMyRequest),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getConnectors",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<List<KafkaConnectorModelResponse>>> getConnectors(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "connectornamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "teamId", required = false) Integer teamId)
      throws Exception {
    return new ResponseEntity<>(
        kafkaConnectControllerService.getConnectors(
            envId, pageNo, currentPage, topicNameSearch, teamId),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getConnectorOverview",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ConnectorOverview> getConnectorOverview(
      @RequestParam(value = "connectornamesearch") String connectorNameSearch,
      @RequestParam(value = "environmentId", defaultValue = "") String environmentId) {
    return new ResponseEntity<>(
        kafkaConnectControllerService.getConnectorOverview(connectorNameSearch, environmentId),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/createClaimConnectorRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createClaimConnectorRequest(
      @RequestParam("connectorName") String connectorName, @RequestParam("env") String envId)
      throws KlawException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.createClaimConnectorRequest(connectorName, envId),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/saveConnectorDocumentation",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> saveConnectorDocumentation(
      @RequestBody KafkaConnectorModel topicInfo) {
    return new ResponseEntity<>(
        kafkaConnectControllerService.saveConnectorDocumentation(topicInfo), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getConnectorDetailsPerEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ConnectorOverviewPerEnv> getConnectorDetailsPerEnv(
      @RequestParam("envSelected") String envId,
      @RequestParam("connectorName") String connectorName)
      throws KlawBadRequestException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.getConnectorDetailsPerEnvToEdit(envId, connectorName),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/connector/restart",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> restartConnector(
      @RequestBody @Valid KafkaConnectorRestartModel kafkaConnectorRestartModel)
      throws KlawException, KlawRestException {
    return new ResponseEntity<>(
        kafkaConnectControllerService.restartConnector(kafkaConnectorRestartModel), HttpStatus.OK);
  }
}
