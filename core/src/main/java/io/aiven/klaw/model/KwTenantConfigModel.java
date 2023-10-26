package io.aiven.klaw.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
// Added to ignore unknown filed after removing schema information.
@JsonIgnoreProperties(ignoreUnknown = true)
public class KwTenantConfigModel implements Serializable {

  private String tenantName;

  private String baseSyncEnvironment;

  private List<String> orderOfTopicPromotionEnvsList;

  private List<String> requestTopicsEnvironmentsList;

  private String baseSyncKafkaConnectCluster;

  private List<String> orderOfConnectorsPromotionEnvsList;

  private List<String> requestConnectorsEnvironmentsList;
}
