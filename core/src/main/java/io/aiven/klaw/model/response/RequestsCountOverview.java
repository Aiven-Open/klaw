package io.aiven.klaw.model.response;

import java.util.Set;
import lombok.Data;

@Data
public class RequestsCountOverview {
  Set<RequestEntityStatusCount> requestEntityStatistics;
}
