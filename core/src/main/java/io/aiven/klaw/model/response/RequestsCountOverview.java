package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

@Data
public class RequestsCountOverview {
  @NotNull Set<RequestEntityStatusCount> requestEntityStatistics;
}
