package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawRestException;
import io.aiven.klaw.helpers.ValidationHelper;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.RequestVerdict;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
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

  @Autowired private RequestService service;

  @RequestMapping(
      value = "/approve",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(
      summary = "Approve a Request",
      description = "Updates the Status of a request to Approved and provisions the request",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Internal Server Error",
            responseCode = "500",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Multi Status",
            responseCode = "207",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Bad Request",
            responseCode = "405",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class))))
      })
  public ResponseEntity<List<ApiResponse>> approveRequest(
      @Valid @RequestBody RequestVerdict verdict) throws KlawException {

    return wrapInResponseEntity(service.processApprovalRequests(verdict));
  }

  @RequestMapping(
      value = "/decline",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(
      summary = "Decline a Request",
      description = "Updates the Status of a request to Declined",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Internal Server Error",
            responseCode = "500",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Multi Status",
            responseCode = "207",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Bad Request",
            responseCode = "405",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class))))
      })
  public ResponseEntity<List<ApiResponse>> declineRequest(
      @Valid @RequestBody RequestVerdict verdict) throws KlawException, KlawRestException {
    log.info("My bad Verdict{}", verdict);
    ValidationHelper.validateNotEmptyOrBlank(
        verdict.getReason(), "A reason must be provided for why a request was declined.");
    return wrapInResponseEntity(service.processDeclineRequests(verdict));
  }

  @RequestMapping(
      value = "/delete",
      method = RequestMethod.POST,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  @Operation(
      summary = "Delete a Request",
      description = "Updates the Status of a request to Deleted",
      responses = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Internal Server Error",
            responseCode = "500",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Multi Status",
            responseCode = "207",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class)))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            description = "Bad Request",
            responseCode = "405",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ApiResponse.class))))
      })
  public ResponseEntity<List<ApiResponse>> deleteRequest(@Valid @RequestBody RequestVerdict verdict)
      throws KlawException, KlawRestException {
    log.info("Delete Requests : {}", verdict);
    return wrapInResponseEntity(service.processDeleteRequests(verdict));
  }

  private ResponseEntity<List<ApiResponse>> wrapInResponseEntity(List<ApiResponse> obj) {
    int failure = 0, success = 0;
    HttpStatus status;
    for (ApiResponse resp : obj) {
      if (resp.getResult().contains(ApiResultStatus.SUCCESS.value)) {
        success++;
      } else {
        failure++;
      }
    }
    if (failure == 0 && success > 0) {
      status = HttpStatus.OK;
    } else if (failure > 0 && success == 0) {
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    } else if (failure > 0 && success > 0) {
      status = HttpStatus.MULTI_STATUS;
    } else {
      status = HttpStatus.BAD_REQUEST;
    }
    return ResponseEntity.status(status).body(obj);
  }
}
