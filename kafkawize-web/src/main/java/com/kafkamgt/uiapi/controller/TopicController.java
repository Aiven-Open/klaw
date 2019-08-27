package com.kafkamgt.uiapi.controller;


import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.model.TopicInfo;
import com.kafkamgt.uiapi.service.TopicControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/")
public class TopicController {

    @Autowired
    private TopicControllerService topicControllerService;

    @Autowired
    public TopicController(TopicControllerService topicControllerService){
        this.topicControllerService = topicControllerService;
    }

    @PostMapping(value = "/createTopics")
    public ResponseEntity<String> createTopics(@RequestBody TopicRequest addTopicRequest) {
        String result = topicControllerService.createTopics(addTopicRequest);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/updateSyncTopics")
    public ResponseEntity<String> updateSyncTopics(@RequestParam ("updatedSyncTopics") String updatedSyncTopics,
                                                   @RequestParam ("envSelected") String envSelected) {
        String updateSyncTopicsResult = topicControllerService.updateSyncTopics(updatedSyncTopics, envSelected);
        return new ResponseEntity<>(updateSyncTopicsResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicStreams", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<PCStream>> getTopicStreams(@RequestParam ("env") String envSelected) {
        return new ResponseEntity<>(topicControllerService.getTopicStreams(envSelected), HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<TopicRequest>> getTopicRequests() {

        return new ResponseEntity<>(topicControllerService.getTopicRequests(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicTeam", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Topic> getTopicTeam(@RequestParam("topicName") String topicName,
                                                     @RequestParam("env") String env) {
       return new ResponseEntity<>(topicControllerService.getTopicTeam(topicName, env), HttpStatus.OK);
    }

    @RequestMapping(value = "/getCreatedTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<TopicRequest>> getCreatedTopicRequests() {

        return new ResponseEntity<>(topicControllerService.getCreatedTopicRequests(), HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteTopicRequests(@RequestParam("topicName") String topicName) {

        return new ResponseEntity<>(topicControllerService.deleteTopicRequests(topicName), HttpStatus.OK);
    }

    @RequestMapping(value = "/execTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> approveTopicRequests(@RequestParam("topicName") String topicName) {

        return new ResponseEntity<>(topicControllerService.approveTopicRequests(topicName), HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopics", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<TopicInfo>> getTopics(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo,
                                                     @RequestParam(value="topicnamesearch",required=false) String topicNameSearch) {

        return new ResponseEntity<>(topicControllerService.getTopics(env, pageNo, topicNameSearch), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSyncTopics", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<TopicRequest>> getSyncTopics(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo,
                        @RequestParam(value="topicnamesearch",required=false) String topicNameSearch) {

        return new ResponseEntity<>(topicControllerService.getSyncTopics(env, pageNo, topicNameSearch), HttpStatus.OK);
    }

}
