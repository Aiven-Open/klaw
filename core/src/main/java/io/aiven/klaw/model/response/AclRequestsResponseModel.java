package io.aiven.klaw.model.response;

import io.aiven.klaw.model.enums.AclIPPrincipleType;
import io.aiven.klaw.model.enums.AclType;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AclRequestsResponseModel extends BaseRequestsResponseModel implements Serializable {

  @NotNull private String topicname;

  // prefixed acls or Literal(default)
  @NotNull private String aclPatternType;

  // Producer/Consumer
  @NotNull private AclType aclType;

  @NotNull private AclIPPrincipleType aclIpPrincipleType;

  @NotNull private Integer req_no;

  @NotNull private Integer requestingteam;

  @NotNull private String requestingTeamName;

  @NotNull private String consumergroup;

  @NotNull private ArrayList<String> acl_ip = new ArrayList<>();

  @NotNull private ArrayList<String> acl_ssl = new ArrayList<>();

  private String transactionalId;

  // Always TOPIC (for now)
  private String aclResourceType;

  private String otherParams;
}
