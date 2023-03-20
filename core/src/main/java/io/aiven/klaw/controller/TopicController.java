package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.TopicTeamResponse;
import io.aiven.klaw.model.enums.AclPatternType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.TopicCreateRequestModel;
import io.aiven.klaw.model.requests.TopicUpdateRequestModel;
import io.aiven.klaw.model.response.TopicRequestsResponseModel;
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
  public ResponseEntity<ApiResponse> createTopicsCreateRequest(
      @Valid @RequestBody TopicCreateRequestModel addTopicRequest)
      throws KlawException, KlawNotAuthorizedException {
    return new ResponseEntity<>(
        topicControllerService.createTopicsCreateRequest(addTopicRequest), HttpStatus.OK);
  }

  @PostMapping(value = "/updateTopics")
  public ResponseEntity<ApiResponse> createTopicsUpdateRequest(
      @Valid @RequestBody TopicUpdateRequestModel addTopicRequest)
      throws KlawException, KlawNotAuthorizedException {
    return new ResponseEntity<>(
        topicControllerService.createTopicsUpdateRequest(addTopicRequest), HttpStatus.OK);
  }

  @PostMapping(
      value = "/createTopicDeleteRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createTopicDeleteRequest(
      @RequestParam("topicName") String topicName,
      @RequestParam("env") String envId,
      @RequestParam(value = "deleteAssociatedSchema", defaultValue = "false")
          boolean deleteAssociatedSchema)
      throws KlawException, KlawNotAuthorizedException {
    return new ResponseEntity<>(
        topicControllerService.createTopicDeleteRequest(topicName, envId, deleteAssociatedSchema),
        HttpStatus.OK);
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

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'CREATED' or 'DELETED'
   * @param env The name of the environment you would like returned e.g. '1' or '4'
   * @param isMyRequest Only return requests created by the user calling the API
   * @return A List of Topic Requests filtered by the provided parameters.
   */
  @RequestMapping(
      value = "/getTopicRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TopicRequestsResponseModel>> getTopicRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "ALL") RequestStatus requestStatus,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "operationType", required = false)
          RequestOperationType requestOperationType,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "isMyRequest", required = false, defaultValue = "false")
          boolean isMyRequest) {
    return new ResponseEntity<>(
        topicControllerService.getTopicRequests(
            pageNo,
            currentPage,
            requestOperationType,
            requestStatus.value,
            env,
            search,
            isMyRequest),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicTeam",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TopicTeamResponse> getTopicTeam(
      @RequestParam("topicName") String topicName,
      @RequestParam(value = "patternType", defaultValue = "LITERAL") AclPatternType patternType)
      throws KlawException {
    return new ResponseEntity<>(
        topicControllerService.getTopicTeamOnly(topicName, patternType), HttpStatus.OK);
  }

  /**
   * @param pageNo Which page would you like returned e.g. 1
   * @param currentPage Which Page are you currently on e.g. 1
   * @param requestStatus What type of requests are you looking for e.g. 'CREATED' or 'DELETED'
   * @param teamId The identifier of the team that created the request that you wish to filter the
   *     results by, e.g. 1,2,3
   * @param env The name of the environment you would like returned e.g. '1' or '4'
   * @param search A wildcard search term that searches topicNames.
   * @return A List of Topic Requests filtered by the provided parameters.
   */
  @RequestMapping(
      value = "/getTopicRequestsForApprover",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TopicRequestsResponseModel>> getTopicRequestsForApprover(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "CREATED") RequestStatus requestStatus,
      @RequestParam(value = "teamId", required = false) Integer teamId,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "search", required = false) String search) {
    return new ResponseEntity<>(
        topicControllerService.getTopicRequestsForApprover(
            pageNo, currentPage, requestStatus.value, teamId, env, search),
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
