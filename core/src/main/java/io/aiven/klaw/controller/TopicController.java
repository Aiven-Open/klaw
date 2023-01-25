package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.service.TopicControllerService;
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
public class TopicController {

  @Autowired private TopicControllerService topicControllerService;

  @PostMapping(value = "/createTopics")
  public ResponseEntity<ApiResponse> createTopicsRequest(
      @Valid @RequestBody TopicRequestModel addTopicRequest) throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.createTopicsRequest(addTopicRequest), HttpStatus.OK);
  }

  @PostMapping(
      value = "/createTopicDeleteRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createTopicDeleteRequest(
      @RequestParam("topicName") String topicName, @RequestParam("env") String envId)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.createTopicDeleteRequest(topicName, envId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/createClaimTopicRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createClaimTopicRequest(
      @RequestParam("topicName") String topicName, @RequestParam("env") String envId)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.createClaimTopicRequest(topicName, envId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TopicRequestModel>> getTopicRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestsType", defaultValue = "all") String requestsType) {
    return new ResponseEntity<>(
        topicControllerService.getTopicRequests(pageNo, currentPage, requestsType), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicTeam",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getTopicTeam(
      @RequestParam("topicName") String topicName,
      @RequestParam(value = "patternType", defaultValue = "LITERAL") AclPatternType patternType)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.getTopicTeamOnly(topicName, patternType), HttpStatus.OK);
  }

  /*
     For executing topic requests
  */
  @RequestMapping(
      value = "/getCreatedTopicRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TopicRequestModel>> getCreatedTopicRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestsType", defaultValue = "created") String requestsType) {
    return new ResponseEntity<>(
        topicControllerService.getCreatedTopicRequests(pageNo, currentPage, requestsType),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/deleteTopicRequests",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteTopicRequests(@RequestParam("topicId") String topicId)
      throws KlawException {
    return new ResponseEntity<>(topicControllerService.deleteTopicRequests(topicId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execTopicRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> approveTopicRequests(@RequestParam("topicId") String topicId)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.approveTopicRequests(topicId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execTopicRequestsDecline",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> declineTopicRequests(
      @RequestParam("topicId") String topicId,
      @RequestParam("reasonForDecline") String reasonForDecline)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.declineTopicRequests(topicId, reasonForDecline), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopics",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<List<TopicInfo>>> getTopics(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "teamName", required = false) String teamName,
      @RequestParam(value = "topicType", required = false) String topicType) {

    return new ResponseEntity<>(
        topicControllerService.getTopics(
            envId, pageNo, currentPage, topicNameSearch, teamName, topicType),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicsOnly",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getTopicsOnly(
      @RequestParam(value = "isMyTeamTopics", defaultValue = "false") String isMyTeamTopics,
      @RequestParam(value = "envSelected", defaultValue = "ALL") String envSelected) {
    return new ResponseEntity<>(
        topicControllerService.getAllTopics(Boolean.parseBoolean(isMyTeamTopics), envSelected),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicDetailsPerEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, Object>> getTopicDetailsPerEnv(
      @RequestParam("envSelected") String envId, @RequestParam("topicname") String topicName) {
    return new ResponseEntity<>(
        topicControllerService.getTopicDetailsPerEnv(envId, topicName), HttpStatus.OK);
  }

  @PostMapping(value = "/saveTopicDocumentation")
  public ResponseEntity<ApiResponse> saveTopicDocumentation(@RequestBody TopicInfo topicInfo)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.saveTopicDocumentation(topicInfo), HttpStatus.OK);
  }

  // getTopic Events from kafka cluster
  @RequestMapping(
      value = "/getTopicEvents",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getTopicEvents(
      @RequestParam("envId") String envId,
      @RequestParam("topicName") String topicName,
      @RequestParam(value = "consumerGroupId") String consumerGroupId,
      @RequestParam(value = "offsetId") String offsetId)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.getTopicEvents(envId, consumerGroupId, topicName, offsetId),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAdvancedTopicConfigs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getAdvancedTopicConfigs() {
    return new ResponseEntity<>(topicControllerService.getAdvancedTopicConfigs(), HttpStatus.OK);
  }
}
