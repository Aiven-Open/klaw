package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.AclGroupBy;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.AclRequestsModel;
import io.aiven.klaw.model.response.AclRequestsResponseModel;
import io.aiven.klaw.model.response.OffsetDetails;
import io.aiven.klaw.model.response.ServiceAccountDetails;
import io.aiven.klaw.model.response.TopicOverview;
import io.aiven.klaw.service.AclControllerService;
import io.aiven.klaw.service.TopicOverviewService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
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
public class AclController {

  @Autowired AclControllerService aclControllerService;

  @Autowired TopicOverviewService topicOverviewService;

  @PostMapping(value = "/createAcl")
  public ResponseEntity<ApiResponse> createAcl(@Valid @RequestBody AclRequestsModel addAclRequest)
      throws KlawException {
    return new ResponseEntity<>(aclControllerService.createAcl(addAclRequest), HttpStatus.OK);
  }

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'CREATED' or
   *     'DELETED' @Param operationType The RequestOperationType Create/Update/Promote/Claim/Delete
   * @param requestOperationType is a filter to only return requests of a certain operation type
   *     e.g. CREATE/UPDATE/PROMOTE/CLAIM/DELETE
   * @param topic The name of the topic you would like returned
   * @param env The name of the environment you would like returned e.g. '1' or '4'
   * @param search is a wildcard search that will patial match against the topic name
   * @param aclType The Type of acl Consumer/Producer @Param search A wildcard search on the topic
   *     name allowing
   * @param order allows the requestor to specify what order the pagination should be returned in
   *     OLDEST_FIRST/NEWEST_FIRST
   * @param isMyRequest filter requests to ony return your own requests
   * @return An array of AclRequests that met the criteria of the inputted values.
   */
  @RequestMapping(
      value = "/getAclRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclRequestsResponseModel>> getAclRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "ALL") RequestStatus requestStatus,
      @RequestParam(value = "operationType", required = false)
          RequestOperationType requestOperationType,
      @RequestParam(value = "topic", required = false) String topic,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "aclType", required = false) AclType aclType,
      @RequestParam(value = "order", required = false, defaultValue = "DESC_REQUESTED_TIME")
          Order order,
      @RequestParam(value = "isMyRequest", required = false, defaultValue = "false")
          boolean isMyRequest) {
    return new ResponseEntity<>(
        aclControllerService.getAclRequests(
            pageNo,
            currentPage,
            requestStatus.value,
            requestOperationType,
            topic,
            env,
            search,
            aclType,
            order,
            isMyRequest),
        HttpStatus.OK);
  }

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'CREATED' or 'DELETED'
   * @param topic The name of the topic you would like returned
   * @param env The name of the environment you would like returned e.g. '1' or '4'
   * @param search is a wildcard search that will patial match against the topic name
   * @param aclType The Type of acl Consumer/Producer
   * @param order allows the requestor to specify what order the pagination should be returned in
   *     OLDEST_FIRST/NEWEST_FIRST
   * @param requestOperationType The type of the request operation
   *     Create/Update/Promote/Claim/Delete
   * @return An array of AclRequests that met the criteria of the inputted values.
   */
  /*
     For executing acl requests
  */
  @RequestMapping(
      value = "/getAclRequestsForApprover",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclRequestsResponseModel>> getAclRequestsForApprover(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "CREATED") RequestStatus requestStatus,
      @RequestParam(value = "topic", required = false) String topic,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "aclType", required = false) AclType aclType,
      @RequestParam(value = "operationType", required = false)
          RequestOperationType requestOperationType,
      @RequestParam(value = "order", required = false, defaultValue = "ASC_REQUESTED_TIME")
          Order order) {
    return new ResponseEntity<>(
        aclControllerService.getAclRequestsForApprover(
            pageNo,
            currentPage,
            requestStatus.value,
            topic,
            env,
            requestOperationType,
            search,
            aclType,
            order),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/deleteAclRequests",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteAclRequests(@RequestParam("req_no") String req_no)
      throws KlawException {
    return new ResponseEntity<>(aclControllerService.deleteAclRequests(req_no), HttpStatus.OK);
  }

  @PostMapping(value = "/execAclRequest")
  public ResponseEntity<ApiResponse> approveAclRequests(@RequestParam("req_no") String req_no)
      throws KlawException {
    return new ResponseEntity<>(aclControllerService.approveAclRequests(req_no), HttpStatus.OK);
  }

  @PostMapping(value = "/createDeleteAclSubscriptionRequest")
  public ResponseEntity<ApiResponse> deleteAclSubscriptionRequest(
      @RequestParam("req_no") String req_no) throws KlawException {
    return new ResponseEntity<>(
        aclControllerService.createDeleteAclSubscriptionRequest(req_no), HttpStatus.OK);
  }

  @PostMapping(value = "/execAclRequestDecline")
  public ResponseEntity<ApiResponse> declineAclRequests(
      @RequestParam("req_no") String req_no,
      @RequestParam("reasonForDecline") String reasonForDecline)
      throws KlawException {
    return new ResponseEntity<>(
        aclControllerService.declineAclRequests(req_no, reasonForDecline), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicOverview",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TopicOverview> getTopicOverview(
      @RequestParam(value = "topicName") String topicName,
      @RequestParam(value = "environmentId", defaultValue = "") String environmentId,
      @RequestParam(value = "groupBy", required = false, defaultValue = "NONE")
          AclGroupBy groupBy) {
    return new ResponseEntity<>(
        topicOverviewService.getTopicOverview(topicName, environmentId, groupBy), HttpStatus.OK);
  }

  // getConsumerOffsets from kafka cluster
  @RequestMapping(
      value = "/getConsumerOffsets",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<OffsetDetails>> getConsumerOffsets(
      @RequestParam("env") String envId,
      @RequestParam("topicName") String topicName,
      @RequestParam(value = "consumerGroupId") String consumerGroupId) {
    return new ResponseEntity<>(
        aclControllerService.getConsumerOffsets(envId, consumerGroupId, topicName), HttpStatus.OK);
  }

  // Aiven api call - get ServiceAccountDetails for a subscription
  @RequestMapping(
      value = "/getAivenServiceAccount",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ServiceAccountDetails> getAivenServiceAccountDetails(
      @RequestParam("env") String envId,
      @RequestParam("topicName") String topicName,
      @RequestParam(value = "userName") String userName,
      @RequestParam(value = "aclReqNo") String aclReqNo) {
    return new ResponseEntity<>(
        aclControllerService.getAivenServiceAccountDetails(envId, topicName, userName, aclReqNo),
        HttpStatus.OK);
  }

  // Aiven api call - get ServiceAccounts for an environment
  @RequestMapping(
      value = "/getAivenServiceAccounts",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Set<String>> getAivenServiceAccounts(@RequestParam("env") String envId) {
    return new ResponseEntity<>(aclControllerService.getAivenServiceAccounts(envId), HttpStatus.OK);
  }
}
