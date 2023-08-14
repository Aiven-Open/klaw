package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.requests.ConsumerOffsetResetRequestModel;
import io.aiven.klaw.service.OperationalRequestsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Slf4j
public class OperationalRequestsController {

  @Autowired OperationalRequestsService operationalRequestsService;

  @PostMapping(
      value = "/operationalRequest/consumerOffsetsReset/create",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> createConsumerOffsetsResetRequest(
      @Valid @RequestBody ConsumerOffsetResetRequestModel consumerOffsetResetRequestModel)
      throws KlawNotAuthorizedException {
    return new ResponseEntity<>(
        operationalRequestsService.createConsumerOffsetsResetRequest(
            consumerOffsetResetRequestModel),
        HttpStatus.OK);
  }
}
