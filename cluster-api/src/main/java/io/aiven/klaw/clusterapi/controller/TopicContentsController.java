package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.services.TopicContentsService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@Slf4j
public class TopicContentsController {

  @Autowired TopicContentsService topicContentsService;

  @RequestMapping(
      value =
          "/getTopicContents/{bootstrapServers}/{protocol}/{clusterName}/{consumerGroupId}/{topicName}/{offsetPosition}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<Long, String>> getTopicContents(
      @PathVariable String bootstrapServers,
      @PathVariable String protocol,
      @PathVariable String clusterName,
      @PathVariable String consumerGroupId,
      @PathVariable String topicName,
      @PathVariable int offsetPosition)
      throws Exception {
    Map<Long, String> events =
        topicContentsService.readEvents(
            bootstrapServers,
            protocol,
            clusterName,
            consumerGroupId,
            topicName,
            offsetPosition,
            "OFFSET_ID");

    return new ResponseEntity<>(events, HttpStatus.OK);
  }
}
