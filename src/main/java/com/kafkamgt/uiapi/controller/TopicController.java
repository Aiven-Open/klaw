package com.kafkamgt.uiapi.controller;


import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
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

    @PostMapping(value = "/createTopics")
    public ResponseEntity<String> createTopics(@RequestBody TopicRequest addTopicRequest) throws KafkawizeException {
        String result = topicControllerService.createTopics(addTopicRequest);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = "/updateSyncTopics")
    public ResponseEntity<String> updateSyncTopics(@RequestParam ("updatedSyncTopics") String updatedSyncTopics,
                                                   @RequestParam ("envSelected") String envSelected) {
        String updateSyncTopicsResult = topicControllerService.updateSyncTopics(updatedSyncTopics, envSelected);
        return new ResponseEntity<>(updateSyncTopicsResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<TopicRequest>> getTopicRequests(@RequestParam("pageNo") String pageNo) {
        return new ResponseEntity<>(topicControllerService.getTopicRequests(pageNo), HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicTeam", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Topic> getTopicTeam(@RequestParam("topicName") String topicName,
                                              @RequestParam("env") String env) {
       return new ResponseEntity<>(topicControllerService.getTopicTeam(topicName, env), HttpStatus.OK);
    }

    @RequestMapping(value = "/getCreatedTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<List<TopicRequest>>> getCreatedTopicRequests() {

        return new ResponseEntity<>(topicControllerService.getCreatedTopicRequests(), HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteTopicRequests(@RequestParam("topicName") String topicName) {
        return new ResponseEntity<>(topicControllerService.deleteTopicRequests(topicName), HttpStatus.OK);
    }

    @PostMapping(value = "/execTopicRequests", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> approveTopicRequests(@RequestParam("topicName") String topicName,
                                                       @RequestParam("env") String env) throws KafkawizeException {
        return new ResponseEntity<>(topicControllerService.approveTopicRequests(topicName, env), HttpStatus.OK);
    }

    @PostMapping(value = "/execTopicRequestsDecline", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> declineTopicRequests(@RequestParam("topicName") String topicName,
                                                       @RequestParam("env") String env) throws KafkawizeException {

        return new ResponseEntity<>(topicControllerService.declineTopicRequests(topicName, env), HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopics", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<List<TopicInfo>>> getTopics(@RequestParam("env") String env,
                                                     @RequestParam("pageNo") String pageNo,
                                                     @RequestParam(value="topicnamesearch",required=false) String topicNameSearch) throws Exception {

        return new ResponseEntity<>(topicControllerService.getTopics(env, pageNo, topicNameSearch), HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicsOnly", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<String>> getTopicsOnly(@RequestParam("env") String env) throws Exception {

        return new ResponseEntity<>(topicControllerService.getAllTopics(env), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSyncTopics", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<TopicRequest>> getSyncTopics(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo,
                                                            @RequestParam(value="topicnamesearch",required=false) String topicNameSearch) throws Exception {

        return new ResponseEntity<>(topicControllerService.getSyncTopics(env, pageNo, topicNameSearch), HttpStatus.OK);
    }

}
