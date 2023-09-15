package io.aiven.klaw.model.requests;

import io.aiven.klaw.model.cluster.consumergroup.OffsetResetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ConsumerOffsetResetRequestModel extends BaseOperationalRequestModel
    implements Serializable {

  @NotNull
  @Pattern(message = "Invalid topic name", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String topicname;

  @NotNull
  @Pattern(message = "Invalid consumer group", regexp = "^[a-zA-Z0-9._-]{3,}$")
  private String consumerGroup;

  @NotNull private OffsetResetType offsetResetType;

  private String resetTimeStampStr;
}
