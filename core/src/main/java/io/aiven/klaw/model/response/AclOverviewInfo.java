package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.KafkaFlavors;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Data;

@Data
public class AclOverviewInfo {
  @NotNull private String req_no;
  @NotNull private String aclPatternType;
  @NotNull private String environment;
  @NotNull private String environmentName;
  @NotNull private KafkaFlavors kafkaFlavorType;
  @NotNull private boolean showDeleteAcl;
  @NotNull private int teamid;
  @NotNull private String teamname;
  @NotNull private String topicname;
  @NotNull private String topictype;

  private String acl_ip;
  private String acl_ssl;
  private Set<String> acl_ips;
  private Set<String> acl_ssls;
  private String consumergroup;
  private String transactionalId;
}
