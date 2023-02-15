package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawRestException;
import io.aiven.klaw.helpers.ValidationHelper;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.RequestVerdict;
import io.aiven.klaw.service.AclControllerService;
import io.aiven.klaw.service.KafkaConnectControllerService;
import io.aiven.klaw.service.SchemaRegstryControllerService;
import io.aiven.klaw.service.TopicControllerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/request")
public class RequestController {

  @Autowired SchemaRegstryControllerService schemaRegstryControllerService;

  @Autowired KafkaConnectControllerService kafkaConnectControllerService;

  @Autowired private TopicControllerService topicControllerService;

  @Autowired private AclControllerService aclControllerService;

  @RequestMapping(
      value = "/approve",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> approveRequest(@RequestBody RequestVerdict verdict)
      throws KlawException {
    log.info("My Verdict{}", verdict);
    switch (verdict.getResourceType()) {
      case KAFKA:
        return wrapInResponseEntity(
            topicControllerService.approveTopicRequests(verdict.getReqId()), HttpStatus.ACCEPTED);
      case ACL:
        return wrapInResponseEntity(
            aclControllerService.approveAclRequests(verdict.getReqId()), HttpStatus.ACCEPTED);
      case SCHEMA:
        return wrapInResponseEntity(
            schemaRegstryControllerService.execSchemaRequests(verdict.getReqId()),
            HttpStatus.ACCEPTED);
      case CONNECTOR:
        return wrapInResponseEntity(
            kafkaConnectControllerService.approveConnectorRequests(verdict.getReqId()),
            HttpStatus.ACCEPTED);
      default:
        return wrapInResponseEntity(
            ApiResponse.builder().result("Unable to determine target resource.").build(),
            HttpStatus.BAD_REQUEST);
    }
  }

  @RequestMapping(
      value = "/decline",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> declineRequest(@RequestBody RequestVerdict verdict)
      throws KlawException, KlawRestException {
    log.info("My bad Verdict{}", verdict);
    ValidationHelper.validateNotEmptyOrBlank(
        verdict.getReason(), "A reason must be provided for why a request was declined.");
    switch (verdict.getResourceType()) {
      case KAFKA:
        return wrapInResponseEntity(
            topicControllerService.declineTopicRequests(verdict.getReqId(), verdict.getReason()),
            HttpStatus.ACCEPTED);
      case ACL:
        return wrapInResponseEntity(
            aclControllerService.declineAclRequests(verdict.getReqId(), verdict.getReason()),
            HttpStatus.ACCEPTED);
      case SCHEMA:
        return wrapInResponseEntity(
            schemaRegstryControllerService.execSchemaRequestsDecline(
                verdict.getReqId(), verdict.getReason()),
            HttpStatus.ACCEPTED);
      case CONNECTOR:
        return wrapInResponseEntity(
            kafkaConnectControllerService.declineConnectorRequests(
                verdict.getReqId(), verdict.getReason()),
            HttpStatus.ACCEPTED);
      default:
        return wrapInResponseEntity(
            ApiResponse.builder().result("Unable to determine target resource.").build(),
            HttpStatus.BAD_REQUEST);
    }
  }

  private ResponseEntity<ApiResponse> wrapInResponseEntity(ApiResponse obj, HttpStatus status) {
    return ResponseEntity.status(status).body(obj);
  }
}
