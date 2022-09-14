package com.kafkamgt.uiapi.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class TopicID implements Serializable {

  private Integer topicid;

  private Integer tenantId;

  public TopicID() {}

  public TopicID(Integer topicid, Integer tenantId) {
    this.topicid = topicid;
    this.tenantId = tenantId;
  }
}
