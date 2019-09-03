package com.kafkamgt.uiapi.controller;


import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.service.SchemaRegstryControllerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/")
public class SchemaRegstryController {

    private static Logger LOG = LoggerFactory.getLogger(SchemaRegstryController.class);

    @Autowired
    SchemaRegstryControllerService schemaRegstryControllerService;

    public SchemaRegstryController(SchemaRegstryControllerService schemaRegstryControllerService){
        this.schemaRegstryControllerService = schemaRegstryControllerService;
    }

    @RequestMapping(value = "/getSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SchemaRequest>> getSchemaRequests() {
        return new ResponseEntity<>(schemaRegstryControllerService.getSchemaRequests(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getCreatedSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SchemaRequest>> getCreatedSchemaRequests() {

        return new ResponseEntity<>(schemaRegstryControllerService.getCreatedSchemaRequests(), HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteSchemaRequests(@RequestParam("topicName") String topicName) {

        String deleteTopicReqStatus = schemaRegstryControllerService.deleteSchemaRequests(topicName);

        deleteTopicReqStatus = "{\"result\":\""+deleteTopicReqStatus+"\"}";
        return new ResponseEntity<>(deleteTopicReqStatus, HttpStatus.OK);
    }

    @RequestMapping(value = "/execSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> execSchemaRequests(@RequestParam("topicName") String topicName) {

        String updateTopicReqStatus = "{\"result\":\"" + schemaRegstryControllerService.execSchemaRequests(topicName) + "\"}";
        return new ResponseEntity<>(updateTopicReqStatus, HttpStatus.OK);
    }

    @PostMapping(value = "/uploadSchema")
    public ResponseEntity<String> uploadSchema(@RequestBody SchemaRequest addSchemaRequest){

        String schemaaddResult = "{\"result\":\""+schemaRegstryControllerService.uploadSchema(addSchemaRequest)+"\"}";
        return new ResponseEntity<>(schemaaddResult, HttpStatus.OK);
    }
}
