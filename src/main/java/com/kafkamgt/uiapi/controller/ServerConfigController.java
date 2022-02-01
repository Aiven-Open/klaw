package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.model.KwPropertiesModel;
import com.kafkamgt.uiapi.model.ServerConfigProperties;
import com.kafkamgt.uiapi.service.ServerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/")
public class ServerConfigController {

    @Autowired
    private ServerConfigService serverConfigService;

    @RequestMapping(value = "/getAllServerConfig", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<ServerConfigProperties>> getAllProperties() {
        return new ResponseEntity<>(serverConfigService.getAllProps(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getAllServerEditableConfig", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<HashMap<String, String>>> getAllEditableProps() {
        return new ResponseEntity<>(serverConfigService.getAllEditableProps(), HttpStatus.OK);
    }

    @RequestMapping(value = "/resetCache", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, String>> resetCache() {
        return new ResponseEntity<>(serverConfigService.resetCache(), HttpStatus.OK);
    }

    @PostMapping(value = "/updateKwCustomProperty")
    public ResponseEntity<HashMap<String, String>> updateKwCustomProperty(@RequestBody KwPropertiesModel kwPropertiesModel) {
        return new ResponseEntity<>(serverConfigService.updateKwCustomProperty(kwPropertiesModel), HttpStatus.OK);
    }

    @RequestMapping(value = "/testClusterApiConnection", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, String>> testClusterApiConnection(@RequestParam("clusterApiUrl") String clusterApiUrl) {
        return new ResponseEntity<>(serverConfigService.testClusterApiConnection(clusterApiUrl), HttpStatus.OK);
    }

}
