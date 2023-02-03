package io.aiven.klaw.model;

import io.aiven.klaw.model.enums.RequestEntityType;
import io.aiven.klaw.model.enums.RequestStatus;
import java.util.Set;
import lombok.Data;

@Data
public class RequestsCountOverview {
  Set<RequestsCount> requestsCountSet;
}

@Data
class RequestsCount {
  RequestEntityType requestEntityType;
  RequestStatus requestStatus;
  int count;
}
