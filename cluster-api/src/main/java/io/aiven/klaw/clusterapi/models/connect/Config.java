package io.aiven.klaw.clusterapi.models.connect;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
public class Config {
  public String connector;
  public String file;
  public String tasks;
  public String topics;
  public String name;
  public String quickstart;
  public String kafka;
}
