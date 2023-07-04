package io.aiven.klaw.model;

import io.aiven.klaw.model.response.EnvIdInfo;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class TopicInfo extends TopicBaseInfo {
  @NotNull private Integer topicid;

  @NotNull private String sequence;

  @NotNull private List<EnvIdInfo> environmentsList;

  @NotNull private String totalNoPages;

  @NotNull private String currentPage;

  @NotNull private List<String> allPageNos;

  private String documentation;
}
