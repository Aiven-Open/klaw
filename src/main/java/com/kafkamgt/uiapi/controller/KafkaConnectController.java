package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.*;
import com.kafkamgt.uiapi.service.KafkaConnectControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/")
public class KafkaConnectController {

    @Autowired
    KafkaConnectControllerService kafkaConnectControllerService;

    @PostMapping(value = "/createConnector")
    public ResponseEntity<HashMap<String, String>> createConnectorRequest(@Valid @RequestBody KafkaConnectorRequestModel addTopicRequest) throws KafkawizeException {
        return new ResponseEntity<>(kafkaConnectControllerService.createConnectorRequest(addTopicRequest), HttpStatus.OK);
    }

    /*
        For executing connector requests
     */
    @RequestMapping(value = "/getCreatedConnectorRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<KafkaConnectorRequestModel>> getCreatedConnectorRequests(@RequestParam("pageNo") String pageNo,
                                                                           @RequestParam(value="currentPage",defaultValue = "") String currentPage,
                                                                           @RequestParam(value="requestsType", defaultValue = "created")
                                                                                   String requestsType) {

        return new ResponseEntity<>(kafkaConnectControllerService.getCreatedConnectorRequests(pageNo, currentPage, requestsType), HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteConnectorRequests", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteConnectorRequests(@RequestParam("connectorId") String connectorId) {
        return new ResponseEntity<>(kafkaConnectControllerService.deleteConnectorRequests(connectorId), HttpStatus.OK);
    }

    @PostMapping(value = "/execConnectorRequests", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, String>> approveTopicRequests(@RequestParam("connectorId") String connectorId) throws KafkawizeException {
        return new ResponseEntity<>(kafkaConnectControllerService.approveConnectorRequests(connectorId), HttpStatus.OK);
    }

    @PostMapping(value = "/execConnectorRequestsDecline", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> declineConnectorRequests(@RequestParam("connectorId") String connectorId,
                                                       @RequestParam("reasonForDecline") String reasonForDecline) throws KafkawizeException {

        return new ResponseEntity<>(kafkaConnectControllerService.declineConnectorRequests(connectorId, reasonForDecline), HttpStatus.OK);
    }

    @PostMapping(value = "/createConnectorDeleteRequest", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, String>> createConnectorDeleteRequest(@RequestParam("connectorName") String topicName,
                                                                            @RequestParam("env") String envId) {
        return new ResponseEntity<>(kafkaConnectControllerService.createConnectorDeleteRequest(topicName, envId), HttpStatus.OK);
    }

    @RequestMapping(value = "/getConnectorRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<KafkaConnectorRequestModel>> getConnectorRequests(@RequestParam("pageNo") String pageNo,
                                                                    @RequestParam(value="currentPage",defaultValue = "") String currentPage,
                                                                    @RequestParam(value="requestsType", defaultValue = "all")
                                                                            String requestsType) {
        return new ResponseEntity<>(kafkaConnectControllerService.getConnectorRequests(pageNo, currentPage, requestsType), HttpStatus.OK);
    }

    @RequestMapping(value = "/getConnectors", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<List<KafkaConnectorModel>>> getTopics(@RequestParam("env") String envId,
                                                                     @RequestParam("pageNo") String pageNo,
                                                                     @RequestParam(value="currentPage",defaultValue = "") String currentPage,
                                                                     @RequestParam(value="connectornamesearch",required=false) String topicNameSearch,
                                                                     @RequestParam(value="teamName",required=false) String teamName
    ) throws Exception {

        return new ResponseEntity<>(kafkaConnectControllerService.getConnectors(envId, pageNo, currentPage, topicNameSearch, teamName), HttpStatus.OK);
    }

    @RequestMapping(value = "/getConnectorOverview", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ConnectorOverview> getConnectorOverview(@RequestParam(value="connectornamesearch") String connectorNameSearch) {
        return new ResponseEntity<>(kafkaConnectControllerService.getConnectorOverview(connectorNameSearch), HttpStatus.OK);
    }


    @PostMapping(value = "/createClaimConnectorRequest", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, String>> createClaimConnectorRequest(@RequestParam("connectorName") String connectorName,
                                                                           @RequestParam("env") String envId) {
        return new ResponseEntity<>(kafkaConnectControllerService.createClaimConnectorRequest(connectorName, envId), HttpStatus.OK);
    }

    @PostMapping(value = "/saveConnectorDocumentation")
    public ResponseEntity<HashMap<String, String>> saveConnectorDocumentation(@RequestBody KafkaConnectorModel topicInfo) {
        HashMap<String, String> saveTopicDocumentationResult = kafkaConnectControllerService.saveConnectorDocumentation(topicInfo);
        return new ResponseEntity<>(saveTopicDocumentationResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/getConnectorDetailsPerEnv", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, Object>> getConnectorDetailsPerEnv(@RequestParam("envSelected") String envId,
                                                                         @RequestParam("connectorName") String connectorName) throws Exception {

        return new ResponseEntity<>(kafkaConnectControllerService.getConnectorDetailsPerEnv(envId, connectorName), HttpStatus.OK);
    }

}
