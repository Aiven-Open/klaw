package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncBackTopics;
import io.aiven.klaw.model.SyncTopicUpdates;
import io.aiven.klaw.model.SyncTopicsBulk;
import io.aiven.klaw.model.TopicInfo;
import io.aiven.klaw.model.response.SyncTopicsList;
import io.aiven.klaw.service.TopicSyncControllerService;
import java.util.List;
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
public class TopicSyncController {

  @Autowired private TopicSyncControllerService topicSyncControllerService;

  @PostMapping(
      value = "/updateSyncTopics",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateSyncTopics(
      @RequestBody List<SyncTopicUpdates> syncTopicUpdates) throws KlawException {
    return new ResponseEntity<>(
        topicSyncControllerService.updateSyncTopics(syncTopicUpdates), HttpStatus.OK);
  }

  @PostMapping(
      value = "/updateSyncTopicsBulk",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateSyncTopicsBulk(
      @RequestBody SyncTopicsBulk syncTopicsBulk) throws KlawException {
    return new ResponseEntity<>(
        topicSyncControllerService.updateSyncTopicsBulk(syncTopicsBulk), HttpStatus.OK);
  }

  // sync back topics
  @RequestMapping(
      value = "/getTopicsRowView",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TopicInfo>> getTopicsRowView(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "teamId", required = false) Integer teamId,
      @RequestParam(value = "topicType", required = false) String topicType) {
    return new ResponseEntity<>(
        topicSyncControllerService.getTopicsRowView(
            envId, pageNo, currentPage, topicNameSearch, teamId, topicType),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/updateSyncBackTopics",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateSyncBackTopics(
      @RequestBody SyncBackTopics syncBackTopics) {
    return new ResponseEntity<>(
        topicSyncControllerService.updateSyncBackTopics(syncBackTopics), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncTopics",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<SyncTopicsList> getSyncTopics(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "showAllTopics", defaultValue = "false", required = false)
          String showAllTopics,
      @RequestParam(value = "isBulkOption", defaultValue = "false", required = false)
          String isBulkOption)
      throws Exception {
    if (Boolean.parseBoolean(showAllTopics))
      return new ResponseEntity<>(
          topicSyncControllerService.getSyncTopics(
              envId,
              pageNo,
              currentPage,
              topicNameSearch,
              showAllTopics,
              Boolean.parseBoolean(isBulkOption)),
          HttpStatus.OK);
    else
      return new ResponseEntity<>(
          topicSyncControllerService.getReconTopics(
              envId,
              pageNo,
              currentPage,
              topicNameSearch,
              showAllTopics,
              Boolean.parseBoolean(isBulkOption)),
          HttpStatus.OK);
  }
}
