package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SchemaOverview;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.SchemaPromotion;
import io.aiven.klaw.model.requests.SchemaRequestModel;
import io.aiven.klaw.model.response.SchemaRequestsResponseModel;
import io.aiven.klaw.service.SchemaOverviewService;
import io.aiven.klaw.service.SchemaRegistryControllerService;
import io.aiven.klaw.service.TopicOverviewService;
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
public class SchemaRegistryController {

  @Autowired SchemaRegistryControllerService schemaRegistryControllerService;

  @Autowired TopicOverviewService topicOverviewService;
  @Autowired SchemaOverviewService schemaOverviewService;

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'all' 'created' or
   *     'deleted'
   * @return A list of filtered Schema Requests for My (Teams) Requests page
   */
  @RequestMapping(
      value = "/getSchemaRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<SchemaRequestsResponseModel>> getSchemaRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "ALL") RequestStatus requestStatus,
      @RequestParam(value = "topic", required = false) String topic,
      @RequestParam(value = "operationType", required = false)
          RequestOperationType requestOperationType,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "isMyRequest", required = false, defaultValue = "false")
          boolean isMyRequest) {
    return new ResponseEntity<>(
        schemaRegistryControllerService.getSchemaRequests(
            pageNo,
            currentPage,
            requestStatus.value,
            requestOperationType,
            false,
            topic,
            env,
            search,
            isMyRequest),
        HttpStatus.OK);
  }

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'all' 'created' or
   *     'deleted'
   * @param topic The name of the topic you would like returned
   * @param env The name of the environment you would like returned e.g. '1'
   * @param search A wildcard seearch on the topic name allowing
   * @return A list of filtered Schema Requests for approval
   */
  @RequestMapping(
      value = "/getSchemaRequestsForApprover",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<SchemaRequestsResponseModel>> getSchemaRequestsForApprover(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "CREATED") RequestStatus requestStatus,
      @RequestParam(value = "topic", required = false) String topic,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "search", required = false) String search) {
    return new ResponseEntity<>(
        schemaRegistryControllerService.getSchemaRequests(
            pageNo, currentPage, requestStatus.value, null, true, topic, env, search, false),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/deleteSchemaRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteSchemaRequests(
      @RequestParam("req_no") String avroSchemaReqId) throws KlawException {
    return new ResponseEntity<>(
        schemaRegistryControllerService.deleteSchemaRequests(avroSchemaReqId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execSchemaRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> execSchemaRequests(
      @RequestParam("avroSchemaReqId") String avroSchemaReqId) throws KlawException {
    return new ResponseEntity<>(
        schemaRegistryControllerService.execSchemaRequests(avroSchemaReqId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execSchemaRequestsDecline",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> execSchemaRequestsDecline(
      @RequestParam("avroSchemaReqId") String avroSchemaReqId,
      @RequestParam("reasonForDecline") String reasonForDecline)
      throws KlawException {
    return new ResponseEntity<>(
        schemaRegistryControllerService.execSchemaRequestsDecline(
            avroSchemaReqId, reasonForDecline),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/uploadSchema",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> uploadSchema(
      @Valid @RequestBody SchemaRequestModel addSchemaRequest) throws KlawException {
    return new ResponseEntity<>(
        schemaRegistryControllerService.uploadSchema(addSchemaRequest), HttpStatus.OK);
  }

  @PostMapping(
      value = "/promote/schema",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> promoteSchema(@RequestBody SchemaPromotion promoteSchemaReq)
      throws Exception {

    return ResponseEntity.ok(schemaRegistryControllerService.promoteSchema(promoteSchemaReq));
  }

  @RequestMapping(
      value = "/getSchemaOfTopic",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<SchemaOverview> getSchemaOfTopic(
      @RequestParam(value = "topicnamesearch") String topicNameSearch,
      @RequestParam(value = "schemaVersionSearch", defaultValue = "") String schemaVersionSearch) {
    return new ResponseEntity<>(
        schemaOverviewService.getSchemaOfTopic(topicNameSearch, schemaVersionSearch),
        HttpStatus.OK);
  }
}
