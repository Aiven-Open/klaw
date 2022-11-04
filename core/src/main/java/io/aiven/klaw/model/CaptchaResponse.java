package io.aiven.klaw.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CaptchaResponse {

  boolean success;

  LocalDateTime challenge_ts;
  String hostname;

  @JsonProperty("error-codes")
  List<String> errorCodes;
}
