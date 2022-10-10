package io.aiven.klaw.error;

import io.aiven.klaw.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class KlawExceptionHandler extends ResponseEntityExceptionHandler {
  public static final String ERROR_MSG =
      "Unable to process the request. Please contact our Administrator !!";

  @ExceptionHandler(KlawException.class)
  protected ResponseEntity<ApiResponse> handleExceptionInternal(
      KlawException ex, WebRequest request) {
    ex.printStackTrace();
    return new ResponseEntity<>(
        ApiResponse.builder().message(ERROR_MSG).build(), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
