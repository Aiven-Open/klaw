package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.dao.ServerConfigProperties;
import com.kafkamgt.uiapi.service.ServerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

}
