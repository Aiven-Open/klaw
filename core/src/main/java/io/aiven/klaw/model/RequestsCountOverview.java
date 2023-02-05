package io.aiven.klaw.model;

import java.util.Set;
import lombok.Data;

@Data
public class RequestsCountOverview {
  Set<RequestEntityStatusCount> requestEntityStatusCount;
}
