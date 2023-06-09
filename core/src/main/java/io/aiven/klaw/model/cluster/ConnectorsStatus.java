package io.aiven.klaw.model.cluster;

import java.util.List;
import lombok.Data;

@Data
public class ConnectorsStatus {
  List<ConnectorState> connectorStateList;
}
