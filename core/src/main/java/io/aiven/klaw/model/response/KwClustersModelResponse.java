package io.aiven.klaw.model.response;

import static org.springframework.beans.BeanUtils.copyProperties;

import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.helpers.Pager;
import io.aiven.klaw.model.enums.ClusterStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.KafkaFlavors;
import io.aiven.klaw.model.enums.KafkaSupportedProtocol;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

  @NotNull private KafkaClustersType clusterType;

  @NotNull private KafkaFlavors kafkaFlavor;

  @NotNull private boolean showDeleteCluster;

  @NotNull private String totalNoPages;

  @NotNull private List<String> allPageNos;

  @NotNull private ClusterStatus clusterStatus;

  @NotNull private String currentPage;

  private String associatedServers;

  private String projectName;

  private String serviceName;

  public KwClustersModelResponse() {}

  public KwClustersModelResponse(KwClusters kwClusters) {
    copyProperties(kwClusters, this);
    this.kafkaFlavor = KafkaFlavors.of(kwClusters.getKafkaFlavor());
    this.clusterType = KafkaClustersType.of(kwClusters.getClusterType());
  }

  public KwClustersModelResponse loadPageContext(Pager.PageContext context) {
    totalNoPages = context.getTotalPages();
    int totalPages = Integer.parseInt(context.getTotalPages());
    List<String> pageNos =
        IntStream.range(1, totalPages + 1).mapToObj(String::valueOf).collect(Collectors.toList());
    allPageNos = pageNos;
    currentPage = context.getPageNo();
    return this;
  }
}
