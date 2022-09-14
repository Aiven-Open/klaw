package com.kafkamgt.uiapi.model;

import lombok.Data;

@Data
public class SyncAclUpdates {
  private String sequence;
  private String req_no;
  private String topicName;
  private String teamSelected;
  private String consumerGroup;
  private String aclIp;
  private String aclSsl;
  private String aclType;
  private String envSelected;
}
