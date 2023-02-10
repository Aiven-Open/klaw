package io.aiven.klaw.clusterapi.models.confluentcloud;

import java.util.ArrayList;

public class ListTopicsResponse {
  public String kind;
  public Metadata metadata;
  public ArrayList<TopicObject> data;
}
