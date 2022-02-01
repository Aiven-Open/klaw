package com.kafkamgt.uiapi.controller;


import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.SchemaRequestModel;
import com.kafkamgt.uiapi.service.SchemaRegstryControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/")
public class SchemaRegstryController {

    @Autowired
    SchemaRegstryControllerService schemaRegstryControllerService;

    @RequestMapping(value = "/getSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SchemaRequestModel>> getSchemaRequests(@RequestParam("pageNo") String pageNo,
                                                                      @RequestParam(value="currentPage",defaultValue = "") String currentPage,
                                                                      @RequestParam(value="requestsType", defaultValue = "all")
                                                                              String requestsType) {
        return new ResponseEntity<>(schemaRegstryControllerService.getSchemaRequests(pageNo, currentPage, requestsType), HttpStatus.OK);
    }

    @RequestMapping(value = "/getCreatedSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SchemaRequestModel>> getCreatedSchemaRequests(@RequestParam("pageNo") String pageNo,
                                                                             @RequestParam(value="currentPage",defaultValue = "") String currentPage,
                                                                             @RequestParam(value="requestsType", defaultValue = "created")
                                                                                     String requestsType) {
        return new ResponseEntity<>(schemaRegstryControllerService.getSchemaRequests(pageNo, currentPage, requestsType), HttpStatus.OK);
    }

    @PostMapping(value = "/deleteSchemaRequests", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteSchemaRequests(@RequestParam("req_no") String avroSchemaReqId) {

        String deleteTopicReqStatus = schemaRegstryControllerService.deleteSchemaRequests(avroSchemaReqId);

        deleteTopicReqStatus = "{\"result\":\""+deleteTopicReqStatus+"\"}";
        return new ResponseEntity<>(deleteTopicReqStatus, HttpStatus.OK);
    }

    @PostMapping(value = "/execSchemaRequests", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> execSchemaRequests(@RequestParam("avroSchemaReqId") String avroSchemaReqId) throws KafkawizeException {

        String updateTopicReqStatus = "{\"result\":\"" + schemaRegstryControllerService.execSchemaRequests(avroSchemaReqId) + "\"}";
        return new ResponseEntity<>(updateTopicReqStatus, HttpStatus.OK);
    }

    @PostMapping(value = "/execSchemaRequestsDecline", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> execSchemaRequestsDecline(@RequestParam("avroSchemaReqId") String avroSchemaReqId,
                                                            @RequestParam("reasonForDecline") String reasonForDecline)  {
        String updateTopicReqStatus = "{\"result\":\"" + schemaRegstryControllerService
                .execSchemaRequestsDecline(avroSchemaReqId, reasonForDecline) + "\"}";
        return new ResponseEntity<>(updateTopicReqStatus, HttpStatus.OK);
    }

    @PostMapping(value = "/uploadSchema")
    public ResponseEntity<String> uploadSchema(@Valid @RequestBody SchemaRequestModel addSchemaRequest){
        String schemaaddResult = "{\"result\":\"" + schemaRegstryControllerService.uploadSchema(addSchemaRequest)+"\"}";
        return new ResponseEntity<>(schemaaddResult, HttpStatus.OK);
    }
}
