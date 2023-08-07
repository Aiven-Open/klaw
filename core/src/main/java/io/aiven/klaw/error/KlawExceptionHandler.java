package io.aiven.klaw.error;

import static io.aiven.klaw.error.KlawErrorMessages.REQ_FAILURE;

import io.aiven.klaw.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class KlawExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({KlawException.class})
  protected ResponseEntity<ApiResponse> handleKlawExceptionInternal(
      KlawException ex, WebRequest request) {
    log.error("Error ", ex);
    return new ResponseEntity<>(ApiResponse.notOk(REQ_FAILURE), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({KlawNotAuthorizedException.class})
  protected ResponseEntity<ApiResponse> handleKlawNotAuthoirzedExceptionInternal(
      HttpServletRequest request, KlawNotAuthorizedException ex) {
    log.error("Error ", ex);
    return new ResponseEntity<>(ApiResponse.NOT_AUTHORIZED, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({KlawRestException.class})
  protected ResponseEntity<ApiResponse> handleKlawRestExceptionInternal(
      KlawRestException ex, WebRequest request) {
    log.error("Error ", ex);
    return new ResponseEntity<>(ApiResponse.notOk(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler({KlawValidationException.class})
  protected ResponseEntity<ApiResponse> handleKlawValidationExceptionInternal(
      KlawValidationException ex, WebRequest request) {
    log.error("KlawValidationException handler: ", ex);
    return new ResponseEntity<>(ApiResponse.notOk(ex.getMessage()), HttpStatus.CONFLICT);
  }

  @ExceptionHandler({KlawBadRequestException.class})
  protected ResponseEntity<ApiResponse> handleKlawBadRequestException(
      KlawBadRequestException ex, WebRequest request) {
    log.error("KlawBadRequestException handler: ", ex);
    return new ResponseEntity<>(ApiResponse.notOk(ex.getMessage()), HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {
    log.error("Validation Error ", ex);
    return new ResponseEntity<>(
        ApiResponse.notOk(ex.getAllErrors().get(0).getDefaultMessage()), HttpStatus.BAD_REQUEST);
  }
}
