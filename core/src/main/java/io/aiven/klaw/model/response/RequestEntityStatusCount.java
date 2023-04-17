package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.RequestEntityType;
import java.util.Set;
import lombok.Data;

@Data
public class RequestEntityStatusCount {
  RequestEntityType requestEntityType; // TOPIC,ACL,SCHEMA,CONNECTOR
  Set<RequestStatusCount> requestStatusCountSet; // created/approved/deleted/declined
  Set<RequestsOperationTypeCount> requestsOperationTypeCountSet; // Create/Delete/Update/Claim
}
