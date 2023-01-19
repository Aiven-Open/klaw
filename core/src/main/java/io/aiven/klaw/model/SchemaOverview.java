package io.aiven.klaw.model;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class SchemaOverview extends Overview {
  Map<String, List<Integer>> allSchemaVersions;
  Map<String, Integer> latestVersion;
  Map<String, Map<String, String>> schemaPromotionDetails;
  List<Map<String, String>> schemaDetails;
}
