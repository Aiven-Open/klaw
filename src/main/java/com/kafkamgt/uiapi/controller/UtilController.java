package com.kafkamgt.uiapi.controller;


import com.kafkamgt.uiapi.service.UtilControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController
@RequestMapping("/")
public class UtilController {

    @Autowired
    private UtilControllerService utilControllerService;

    @RequestMapping(value = "/getAuth", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getAuth() {
        return new ResponseEntity<>(utilControllerService.getAuth(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getExecAuth", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getExecAuth() {

        return new ResponseEntity<>(utilControllerService.getExecAuth(), HttpStatus.OK);
    }

    @GetMapping("/logout")
    public void getLogoutPage(HttpServletRequest request, HttpServletResponse response){
        utilControllerService.getLogoutPage(request, response);
    }

}
