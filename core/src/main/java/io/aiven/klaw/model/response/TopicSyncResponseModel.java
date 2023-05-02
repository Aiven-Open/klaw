package io.aiven.klaw.model.response;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TopicSyncResponseModel extends TopicRequestsResponseModel implements Serializable {

  private String validationStatus;

  private boolean validTopicName;
}
