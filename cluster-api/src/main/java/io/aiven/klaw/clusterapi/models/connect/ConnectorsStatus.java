package io.aiven.klaw.clusterapi.models.connect;

import java.util.List;
import lombok.Data;

@Data
public class ConnectorsStatus {
  List<ConnectorState> connectorStateList;
}
