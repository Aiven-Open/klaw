package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.OperationalRequestType;
import io.aiven.klaw.model.enums.Order;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.requests.ConsumerOffsetResetRequestModel;
import io.aiven.klaw.model.response.OperationalRequestsResponseModel;
import io.aiven.klaw.service.OperationalRequestsService;
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

  @RequestMapping(
      value = "/operationalRequest",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<OperationalRequestsResponseModel>> getConsumerOffsetsResetRequests(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "requestStatus", defaultValue = "ALL") RequestStatus requestStatus,
      @RequestParam(value = "env", required = false) String env,
      @RequestParam(value = "operationType", required = false)
          OperationalRequestType operationalRequestType,
      @RequestParam(value = "search", required = false) String search,
      @RequestParam(value = "order", required = false, defaultValue = "DESC_REQUESTED_TIME")
          Order order,
      @RequestParam(value = "isMyRequest", required = false, defaultValue = "false")
          boolean isMyRequest) {
    return new ResponseEntity<>(
        operationalRequestsService.getConsumerOffsetsResetRequests(
            pageNo,
            currentPage,
            operationalRequestType,
            requestStatus.value,
            env,
            search,
            order,
            isMyRequest),
        HttpStatus.OK);
  }
}
