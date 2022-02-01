package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.dao.ActivityLog;
import com.kafkamgt.uiapi.service.UiConfigControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/")
public class UiConfigController {

    @Autowired
    private UiConfigControllerService uiConfigControllerService;

    @RequestMapping(value = "/getDbAuth", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<HashMap<String, String>> getDbAuth() {
        return new ResponseEntity<>(uiConfigControllerService.getDbAuth(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getRequestTypeStatuses", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<String>> getRequestTypeStatuses() {
        return new ResponseEntity<>(uiConfigControllerService.getRequestTypeStatuses(), HttpStatus.OK);
    }

    @PostMapping(value = "/sendMessageToAdmin")
    public ResponseEntity<HashMap<String, String>> sendMessageToAdmin(@RequestParam ("contactFormSubject") String contactFormSubject,
                                                     @RequestParam ("contactFormMessage") String contactFormMessage){
        return new ResponseEntity<>(uiConfigControllerService.sendMessageToAdmin(contactFormSubject, contactFormMessage), HttpStatus.OK);
    }

    @RequestMapping(value = "/getActivityLogPerEnv", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<ActivityLog>> showActivityLog(@RequestParam(value="env",defaultValue = "") String env,
                                                             @RequestParam("pageNo") String pageNo,
                                                             @RequestParam(value="currentPage",defaultValue = "") String currentPage){
        return new ResponseEntity<>(uiConfigControllerService.showActivityLog(env, pageNo, currentPage), HttpStatus.OK);
    }

}
