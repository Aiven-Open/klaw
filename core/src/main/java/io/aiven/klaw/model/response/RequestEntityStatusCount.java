package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.RequestEntityType;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

@Data
public class RequestEntityStatusCount {
  @NotNull RequestEntityType requestEntityType; // TOPIC,ACL,SCHEMA,CONNECTOR

  @NotNull Set<RequestStatusCount> requestStatusCountSet; // created/approved/deleted/declined

  @NotNull
  Set<RequestsOperationTypeCount> requestsOperationTypeCountSet; // Create/Delete/Update/Claim
}
