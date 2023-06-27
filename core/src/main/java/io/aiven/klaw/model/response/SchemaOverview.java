package io.aiven.klaw.model.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class SchemaOverview extends ResourceOverviewAttributes {
  private List<Integer> allSchemaVersions;
  private Integer latestVersion;
  @NotNull private PromotionStatus schemaPromotionDetails;
  private SchemaDetailsPerEnv schemaDetailsPerEnv;
}
