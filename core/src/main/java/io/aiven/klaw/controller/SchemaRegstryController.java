package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SchemaRequestModel;
import io.aiven.klaw.service.SchemaRegstryControllerService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
      @RequestParam("req_no") String avroSchemaReqId) throws KlawException {
    return new ResponseEntity<>(
        schemaRegstryControllerService.deleteSchemaRequests(avroSchemaReqId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execSchemaRequests",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> execSchemaRequests(
      @RequestParam("avroSchemaReqId") String avroSchemaReqId) throws KlawException {
    return new ResponseEntity<>(
        schemaRegstryControllerService.execSchemaRequests(avroSchemaReqId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/execSchemaRequestsDecline",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> execSchemaRequestsDecline(
      @RequestParam("avroSchemaReqId") String avroSchemaReqId,
      @RequestParam("reasonForDecline") String reasonForDecline)
      throws KlawException {
    return new ResponseEntity<>(
        schemaRegstryControllerService.execSchemaRequestsDecline(avroSchemaReqId, reasonForDecline),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/uploadSchema",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> uploadSchema(
      @Valid @RequestBody SchemaRequestModel addSchemaRequest) throws KlawException {
    return new ResponseEntity<>(
        schemaRegstryControllerService.uploadSchema(addSchemaRequest), HttpStatus.OK);
  }
}
