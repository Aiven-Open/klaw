package io.aiven.klaw.clusterapi.models.confluentcloud;

import java.util.ArrayList;
import lombok.Data;

@Data
public class ListAclsResponse {
  public String kind;
  public Metadata metadata;
  public ArrayList<ConfluentCloudAclObject> data;
}
