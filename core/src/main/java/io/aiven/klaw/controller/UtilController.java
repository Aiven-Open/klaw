package io.aiven.klaw.controller;

import io.aiven.klaw.model.RequestsCountOverview;
import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.service.UtilControllerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UtilController {

  @Autowired private UtilControllerService utilControllerService;

  @RequestMapping(
      value = "/getDashboardStats",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getDashboardStats() {
    return new ResponseEntity<>(utilControllerService.getDashboardStats(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAuth",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getAuth() {
    return new ResponseEntity<>(utilControllerService.getAuth(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getBasicInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, Object>> getBasicInfo() {
    return new ResponseEntity<>(utilControllerService.getBasicInfo(), HttpStatus.OK);
  }

  @PostMapping(value = "/logout")
  public ResponseEntity<Map<String, String>> logout(
      HttpServletRequest request, HttpServletResponse response) {
    utilControllerService.getLogoutPage(request, response);
    return new ResponseEntity<>(new HashMap<>(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/resetMemoryCache/{tenantName}/{entityType}/{operationType}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> resetMemoryCache(
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

  /**
   * @param requestEntityType topic, acl, schema, connector, user
   * @param requestStatus requests in different status created/deleted/declined/approved/all
   * @return RequestsCountOverview A count of each request entity type, and request status, and
   *     overall count
   */
  /*
     Get counts of all request entity types, and for their different status types
  */
  @Operation(
      summary = "Get counts of all request entity types, and for their different status types",
      responses = {
        @ApiResponse(
            content = @Content(schema = @Schema(implementation = RequestsCountOverview.class)))
      })
  @RequestMapping(
      value = "/requests/requestEntityType/{requestEntityType}/requestStatus/{requestStatus}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<RequestsCountOverview> getRequestsCountOverview(
      @PathVariable RequestEntityType requestEntityType,
      @PathVariable RequestStatus requestStatus) {
    return new ResponseEntity<>(
        utilControllerService.getRequestsCountOverview(requestEntityType, requestStatus),
        HttpStatus.OK);
  }
}
