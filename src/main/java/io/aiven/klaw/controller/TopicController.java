package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicRequestModel;
import io.aiven.klaw.service.TopicControllerService;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
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
public class TopicController {

  @Autowired private TopicControllerService topicControllerService;

  @PostMapping(value = "/createTopics")
  public ResponseEntity<Map<String, String>> createTopicsRequest(
      @Valid @RequestBody TopicRequestModel addTopicRequest) throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.createTopicsRequest(addTopicRequest), HttpStatus.OK);
  }

  @PostMapping(
      value = "/createTopicDeleteRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> createTopicDeleteRequest(
      @RequestParam("topicName") String topicName, @RequestParam("env") String envId) {
    return new ResponseEntity<>(
        topicControllerService.createTopicDeleteRequest(topicName, envId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/createClaimTopicRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> createClaimTopicRequest(
      @RequestParam("topicName") String topicName, @RequestParam("env") String envId) {
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
      @RequestParam(value = "patternType", defaultValue = "LITERAL") String patternType)
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
  public ResponseEntity<String> deleteTopicRequests(@RequestParam("topicId") String topicId) {
    return new ResponseEntity<>(topicControllerService.deleteTopicRequests(topicId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execTopicRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> approveTopicRequests(@RequestParam("topicId") String topicId)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.approveTopicRequests(topicId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execTopicRequestsDecline",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<String> declineTopicRequests(
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
      @RequestParam(value = "topicType", required = false) String topicType)
      throws Exception {

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
      @RequestParam(value = "isMyTeamTopics", defaultValue = "false") String isMyTeamTopics)
      throws Exception {
    return new ResponseEntity<>(
        topicControllerService.getAllTopics(Boolean.parseBoolean(isMyTeamTopics)), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicDetailsPerEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, Object>> getTopicDetailsPerEnv(
      @RequestParam("envSelected") String envId, @RequestParam("topicname") String topicName)
      throws Exception {

    return new ResponseEntity<>(
        topicControllerService.getTopicDetailsPerEnv(envId, topicName), HttpStatus.OK);
  }

  @PostMapping(value = "/saveTopicDocumentation")
  public ResponseEntity<Map<String, String>> saveTopicDocumentation(
      @RequestBody TopicInfo topicInfo) {
    Map<String, String> saveTopicDocumentationResult =
        topicControllerService.saveTopicDocumentation(topicInfo);
    return new ResponseEntity<>(saveTopicDocumentationResult, HttpStatus.OK);
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
}
