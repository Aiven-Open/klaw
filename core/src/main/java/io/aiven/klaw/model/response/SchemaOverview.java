package io.aiven.klaw.model.response;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class SchemaOverview extends ResourceOverviewAttributes {
  private List<Integer> allSchemaVersions;
  private Integer latestVersion;
  private Map<String, PromotionStatus> schemaPromotionDetails;
  private SchemaDetailsPerEnv schemaDetailsPerEnv;
}
