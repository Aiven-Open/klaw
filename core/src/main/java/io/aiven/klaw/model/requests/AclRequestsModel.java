package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AclRequestsModel extends BaseRequestModel implements Serializable {

  @NotNull
  @Pattern(message = "Invalid topic name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String topicname;

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

  // Producer/Consumer
  @NotNull private AclType aclType;

  @NotNull private AclIPPrincipleType aclIpPrincipleType;

  private Integer requestingteam;

  private String requestingTeamName;

  // Always TOPIC (for now)
  private String aclResourceType;

  private String otherParams;
}
