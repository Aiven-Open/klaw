package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.service.UtilControllerService;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UtilController {

  @Autowired private UtilControllerService utilControllerService;

  @RequestMapping(
      value = "/getDashboardStats",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getDashboardStats() {
    return new ResponseEntity<>(utilControllerService.getDashboardStats(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAuth",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getAuth() {
    return new ResponseEntity<>(utilControllerService.getAuth(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getBasicInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getBasicInfo() {
    return new ResponseEntity<>(utilControllerService.getBasicInfo(), HttpStatus.OK);
  }

  @PostMapping(value = "/logout")
  public ResponseEntity<HashMap<String, String>> logout(
      HttpServletRequest request, HttpServletResponse response) {
    utilControllerService.getLogoutPage(request, response);
    return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/resetMemoryCache/{tenantName}/{entityType}/{operationType}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> resetMemoryCache(
      @PathVariable String tenantName,
      @PathVariable String entityType,
      @PathVariable String operationType) {
    utilControllerService.resetCache(tenantName, entityType, operationType);
    return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
  }

  @GetMapping("/shutdownContext")
  public void shutdownApp() {
    utilControllerService.shutdownContext();
  }
}
