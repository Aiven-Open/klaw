package com.kafkamgt.uiapi.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class TopicHistory implements Serializable {
  private String environmentName;

  private String teamName;

  private String requestedBy;

  private String requestedTime;

  private String approvedBy;

  private String approvedTime;

  private String remarks;
}
