package io.aiven.klaw.clusterapi.models.enums;

public enum ClusterStatus {
  OFFLINE("OFFLINE"),
  ONLINE("ONLINE"),
  NOT_KNOWN("NOT_KNOWN");

  public final String value;

  ClusterStatus(String value) {
    this.value = value;
  }
}
