package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.AclRequestsModel;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SchemaOverview;
import io.aiven.klaw.model.TopicOverview;
import io.aiven.klaw.service.AclControllerService;
import io.aiven.klaw.service.TopicOverviewService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

  @RequestMapping(
      value = "/getAclRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclRequestsModel>> getAclRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestsType", defaultValue = "all") String requestsType) {
    return new ResponseEntity<>(
        aclControllerService.getAclRequests(pageNo, currentPage, requestsType), HttpStatus.OK);
  }

  /*
     For executing acl requests
  */
  @RequestMapping(
      value = "/getCreatedAclRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclRequestsModel>> getCreatedAclRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestsType", defaultValue = "created") String requestsType) {
    return new ResponseEntity<>(
        aclControllerService.getCreatedAclRequests(pageNo, currentPage, requestsType),
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
      value = "/getAcls",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TopicOverview> getAcls(
      @RequestParam(value = "topicnamesearch") String topicNameSearch) {
    return new ResponseEntity<>(
        topicOverviewService.getTopicOverview(topicNameSearch), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSchemaOfTopic",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<SchemaOverview> getSchemaOfTopic(
      @RequestParam(value = "topicnamesearch") String topicNameSearch,
      @RequestParam(value = "schemaVersionSearch", defaultValue = "") String schemaVersionSearch) {
    return new ResponseEntity<>(
        topicOverviewService.getSchemaOfTopic(topicNameSearch, schemaVersionSearch), HttpStatus.OK);
  }

  // getConsumerOffsets from kafka cluster
  @RequestMapping(
      value = "/getConsumerOffsets",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<Map<String, String>>> getConsumerOffsets(
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
  public ResponseEntity<ApiResponse> getAivenServiceAccountDetails(
      @RequestParam("env") String envId,
      @RequestParam("topicName") String topicName,
      @RequestParam(value = "userName") String userName,
      @RequestParam(value = "aclReqNo") String aclReqNo) {
    return new ResponseEntity<>(
        aclControllerService.getAivenServiceAccountDetails(envId, topicName, userName, aclReqNo),
        HttpStatus.OK);
  }
}
