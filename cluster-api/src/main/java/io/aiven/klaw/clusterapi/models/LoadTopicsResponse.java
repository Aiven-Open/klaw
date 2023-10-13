package io.aiven.klaw.clusterapi.models;

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
