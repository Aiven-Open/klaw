package io.aiven.klaw.model.cluster;

import io.aiven.klaw.model.response.TopicConfig;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoadTopicsResponse {
  @NotNull private boolean loadingInProgress;

  private Set<TopicConfig> topicConfigSet;
}
