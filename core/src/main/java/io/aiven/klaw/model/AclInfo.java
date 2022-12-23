package io.aiven.klaw.model;

import io.aiven.klaw.model.enums.KafkaFlavors;
import java.util.List;
import lombok.Data;

@Data
public class AclInfo {

  private String sequence;
  private String req_no;
  private String acl_ip;
  private String acl_ssl;
  private String topicname;
  private String topictype;
  private String consumergroup;
  private String environment;
  private String environmentName;
  private String teamname;
  private int teamid;
  private String operation;
  private String permission;
  private String transactionalId;
  private String aclPatternType;
  private String totalNoPages;
  private List<String> allPageNos;
  private List<String> possibleTeams;
  private String currentPage;
  private boolean showDeleteAcl;
  private KafkaFlavors kafkaFlavorType;
}
