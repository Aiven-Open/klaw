package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.KafkaConnectorModel;
import com.kafkamgt.uiapi.model.SyncConnectorUpdates;
import com.kafkamgt.uiapi.service.KafkaConnectSyncControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/")
public class KafkaConnectSyncController {

    @Autowired
    KafkaConnectSyncControllerService kafkaConnectControllerService;

    @PostMapping(value = "/updateSyncConnectors")
    public ResponseEntity<HashMap<String, String>> updateSyncTopics(@RequestBody List<SyncConnectorUpdates> syncConnectorUpdates) {
        HashMap<String, String> updateSyncConnectorsResult = kafkaConnectControllerService.updateSyncConnectors(syncConnectorUpdates);
        return new ResponseEntity<>(updateSyncConnectorsResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/getConnectorDetails", method = RequestMethod.GET,produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, String>> getConnectorDetails(@RequestParam("env") String envId,
                                                                             @RequestParam("connectorName") String connectorName) throws KafkawizeException {
        return new ResponseEntity<>(kafkaConnectControllerService.getConnectorDetails(connectorName, envId), HttpStatus.OK);
    }

    @RequestMapping(value = "/getSyncConnectors", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<KafkaConnectorModel>> getSyncTopics(@RequestParam("env") String envId,
                                                                 @RequestParam("pageNo") String pageNo,
                                                                 @RequestParam(value="currentPage",defaultValue = "") String currentPage,
                                                                 @RequestParam(value="connectornamesearch",required=false) String connectorNameSearch,
                                                                 @RequestParam(value="isBulkOption",defaultValue = "false", required=false) String isBulkOption
    ) throws Exception {
//        if(Boolean.parseBoolean(showAllTopics))
            return new ResponseEntity<>(kafkaConnectControllerService.getSyncConnectors(envId, pageNo, currentPage, connectorNameSearch,
                     Boolean.parseBoolean(isBulkOption)), HttpStatus.OK);
//        else
//            return new ResponseEntity<>(topicSyncControllerService.getReconTopics(envId, pageNo, currentPage, topicNameSearch,
//                    showAllTopics, Boolean.parseBoolean(isBulkOption)), HttpStatus.OK);
    }
}
