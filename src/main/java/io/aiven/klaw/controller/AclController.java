package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.*;
import io.aiven.klaw.service.AclControllerService;
import java.util.HashMap;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class AclController {

  @Autowired AclControllerService aclControllerService;

  @PostMapping(value = "/createAcl")
  public ResponseEntity<String> createAcl(@Valid @RequestBody AclRequestsModel addAclRequest) {
    return new ResponseEntity<>(aclControllerService.createAcl(addAclRequest), HttpStatus.OK);
  }

  @PostMapping(value = "/updateSyncAcls")
  public ResponseEntity<HashMap<String, String>> updateSyncAcls(
      @RequestBody List<SyncAclUpdates> syncAclUpdates) {
    return new ResponseEntity<>(aclControllerService.updateSyncAcls(syncAclUpdates), HttpStatus.OK);
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
  public ResponseEntity<String> deleteAclRequests(@RequestParam("req_no") String req_no) {
    return new ResponseEntity<>(aclControllerService.deleteAclRequests(req_no), HttpStatus.OK);
  }

  @PostMapping(value = "/execAclRequest")
  public ResponseEntity<String> approveAclRequests(@RequestParam("req_no") String req_no)
      throws KlawException {
    return new ResponseEntity<>(aclControllerService.approveAclRequests(req_no), HttpStatus.OK);
  }

  @PostMapping(value = "/createDeleteAclSubscriptionRequest")
  public ResponseEntity<String> deleteAclSubscriptionRequest(
      @RequestParam("req_no") String req_no) {
    return new ResponseEntity<>(
        aclControllerService.createDeleteAclSubscriptionRequest(req_no), HttpStatus.OK);
  }

  @PostMapping(value = "/execAclRequestDecline")
  public ResponseEntity<String> declineAclRequests(
      @RequestParam("req_no") String req_no,
      @RequestParam("reasonForDecline") String reasonForDecline) {
    return new ResponseEntity<>(
        aclControllerService.declineAclRequests(req_no, reasonForDecline), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAcls",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TopicOverview> getAcls(
      @RequestParam(value = "topicnamesearch") String topicNameSearch,
      @RequestParam(value = "schemaVersionSearch", defaultValue = "") String schemaVersionSearch) {
    return new ResponseEntity<>(
        aclControllerService.getAcls(topicNameSearch, schemaVersionSearch), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncBackAcls",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclInfo>> getSyncBackAcls(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "teamName", required = false) String teamName) {
    return new ResponseEntity<>(
        aclControllerService.getSyncBackAcls(envId, pageNo, currentPage, topicNameSearch, teamName),
        HttpStatus.OK);
  }

  @PostMapping(value = "/updateSyncBackAcls")
  public ResponseEntity<HashMap<String, List<String>>> updateSyncBackAcls(
      @RequestBody SyncBackAcls syncBackAcls) {
    HashMap<String, List<String>> updateSyncAclsResult =
        aclControllerService.updateSyncBackAcls(syncBackAcls);
    return new ResponseEntity<>(updateSyncAclsResult, HttpStatus.OK);
  }

  // get acls from kafka cluster
  @RequestMapping(
      value = "/getSyncAcls",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclInfo>> getSyncAcls(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "showAllAcls", defaultValue = "false", required = false)
          String showAllAcls)
      throws KlawException {
    return new ResponseEntity<>(
        aclControllerService.getSyncAcls(envId, pageNo, currentPage, topicNameSearch, showAllAcls),
        HttpStatus.OK);
  }

  // getConsumerOffsets from kafka cluster
  @RequestMapping(
      value = "/getConsumerOffsets",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<HashMap<String, String>>> getConsumerOffsets(
      @RequestParam("env") String envId,
      @RequestParam("topicName") String topicName,
      @RequestParam(value = "consumerGroupId") String consumerGroupId)
      throws KlawException {
    return new ResponseEntity<>(
        aclControllerService.getConsumerOffsets(envId, consumerGroupId, topicName), HttpStatus.OK);
  }
}
