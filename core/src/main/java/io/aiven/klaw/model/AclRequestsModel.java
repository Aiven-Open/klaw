package io.aiven.klaw.model;

import io.aiven.klaw.model.enums.AclIPPrincipleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AclRequestsModel implements Serializable {

  @Pattern(message = "Invalid remarks", regexp = "^$|^[a-zA-Z 0-9_.,-]{3,}$")
  private String remarks;

  @Pattern(message = "Invalid consumer group", regexp = "^$|^[a-zA-Z0-9_.-]{3,}$")
  private String consumergroup;

  private ArrayList<
          @Pattern(
              message = "Invalid IP Address",
              regexp = "^$|^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$")
          String>
      acl_ip;

  private ArrayList<
          @Pattern(message = "Invalid Principle", regexp = "^$|^[a-zA-Z 0-9_.,=-]{3,}$") String>
      acl_ssl;

  // prefixed acls or Literal(default)
  @NotNull private String aclPatternType;

  @Pattern(message = "Invalid transactionalID", regexp = "^$|^[a-zA-Z0-9_.-]{3,}$")
  private String transactionalId;

  private Integer req_no;

  @NotNull private String topicname;

  @NotNull private String environment;

  @NotNull private String teamname;

  @NotNull private String topictype;

  @NotNull private AclIPPrincipleType aclIpPrincipleType;

  private String environmentName;

  private Integer teamId;

  private Integer requestingteam;

  private String requestingTeamName;

  private String appname;

  private String username;

  private Timestamp requesttime;

  private String requesttimestring;

  private String aclstatus;

  private String approver;

  private Timestamp approvingtime;

  private String aclType;

  // Always TOPIC (for now)
  private String aclResourceType;

  private String currentPage;

  private String otherParams;

  private String totalNoPages;

  private List<String> allPageNos;

  private String approvingTeamDetails;
}
