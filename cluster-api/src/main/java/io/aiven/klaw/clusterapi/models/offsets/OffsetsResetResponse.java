package io.aiven.klaw.clusterapi.models.offsets;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/*
  Keys : BEFORE_OFFSET_RESET, AFTER_OFFSET_RESET.
  Values is a map with topic partitions as keys and offset ids as values.
*/
@Data
public class OffsetsResetResponse {
  private Map<String, Map<String, Long>> consumerGroupOffsets = new HashMap<>();
}
