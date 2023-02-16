package io.aiven.klaw.clusterapi.models.confluentcloud;

import lombok.Data;

@Data
public class AclObject {
  public String kind;
  public Metadata metadata;
  public String cluster_id;
  public String resource_type;
  public String resource_name;
  public String pattern_type;
  public String principal;
  public String host;
  public String operation;
  public String permission;
}
