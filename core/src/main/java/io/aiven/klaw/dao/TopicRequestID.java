package io.aiven.klaw.dao;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class TopicRequestID implements Serializable {

  private Integer topicid;

  private Integer tenantId;

  public TopicRequestID() {}

  public TopicRequestID(Integer topicid, Integer tenantId) {
    this.topicid = topicid;
    this.tenantId = tenantId;
  }
}
