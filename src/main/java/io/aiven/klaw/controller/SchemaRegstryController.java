package io.aiven.klaw.controller;

import static io.aiven.klaw.service.UtilControllerService.handleException;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SchemaRequestModel;
import io.aiven.klaw.service.SchemaRegstryControllerService;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@Slf4j
public class SchemaRegstryController {

  @Autowired SchemaRegstryControllerService schemaRegstryControllerService;

  @RequestMapping(
      value = "/getSchemaRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<SchemaRequestModel>> getSchemaRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestsType", defaultValue = "all") String requestsType) {
    return new ResponseEntity<>(
        schemaRegstryControllerService.getSchemaRequests(pageNo, currentPage, requestsType),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getCreatedSchemaRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<SchemaRequestModel>> getCreatedSchemaRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestsType", defaultValue = "created") String requestsType) {
    return new ResponseEntity<>(
        schemaRegstryControllerService.getSchemaRequests(pageNo, currentPage, requestsType),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/deleteSchemaRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteSchemaRequests(
      @RequestParam("req_no") String avroSchemaReqId) {
    try {
      return new ResponseEntity<>(
          schemaRegstryControllerService.deleteSchemaRequests(avroSchemaReqId), HttpStatus.OK);
    } catch (KlawException e) {
      return handleException(e);
    }
  }

  @PostMapping(
      value = "/execSchemaRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> execSchemaRequests(
      @RequestParam("avroSchemaReqId") String avroSchemaReqId) {
    try {
      return new ResponseEntity<>(
          schemaRegstryControllerService.execSchemaRequests(avroSchemaReqId), HttpStatus.OK);
    } catch (KlawException e) {
      return handleException(e);
    }
  }

  @PostMapping(
      value = "/execSchemaRequestsDecline",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> execSchemaRequestsDecline(
      @RequestParam("avroSchemaReqId") String avroSchemaReqId,
      @RequestParam("reasonForDecline") String reasonForDecline) {

    try {
      return new ResponseEntity<>(
          schemaRegstryControllerService.execSchemaRequestsDecline(
              avroSchemaReqId, reasonForDecline),
          HttpStatus.OK);
    } catch (KlawException e) {
      return handleException(e);
    }
  }

  @PostMapping(value = "/uploadSchema")
  public ResponseEntity<ApiResponse> uploadSchema(
      @Valid @RequestBody SchemaRequestModel addSchemaRequest) {
    try {
      return new ResponseEntity<>(
          schemaRegstryControllerService.uploadSchema(addSchemaRequest), HttpStatus.OK);
    } catch (KlawException e) {
      return handleException(e);
    }
  }
}
