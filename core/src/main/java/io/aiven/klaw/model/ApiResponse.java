package io.aiven.klaw.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.aiven.klaw.model.enums.ApiResultStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
  @NotNull private boolean success;

  private String errCode;

  @NotNull private String message;

  private String debugMessage;

  private Object data;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
  private LocalDateTime timestamp;

  public static ApiResponse ok(String message) {
    return ApiResponse.builder().success(true).message(message).build();
  }

  public static ApiResponse notOk(String message) {
    return ApiResponse.builder().success(false).message(message).build();
  }

  public static final ApiResponse SUCCESS = ApiResponse.ok(ApiResultStatus.SUCCESS.value);
  public static final ApiResponse FAILURE = ApiResponse.notOk(ApiResultStatus.FAILURE.value);
  public static final ApiResponse NOT_AUTHORIZED =
      ApiResponse.notOk(ApiResultStatus.NOT_AUTHORIZED.value);
}
