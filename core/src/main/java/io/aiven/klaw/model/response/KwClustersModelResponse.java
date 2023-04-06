package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class KwClustersModelResponse implements Serializable {

  @NotNull private Integer clusterId;

  @NotNull private String clusterName;

  @NotNull private String bootstrapServers;

  @NotNull private KafkaSupportedProtocol protocol;

  @NotNull private String clusterType;

  @NotNull private String kafkaFlavor;

  @NotNull private boolean showDeleteCluster;

  @NotNull private String totalNoPages;

  @NotNull private List<String> allPageNos;

  private String clusterStatus;

  private String associatedServers;

  private String projectName;

  private String serviceName;

  private String publicKey;
}
