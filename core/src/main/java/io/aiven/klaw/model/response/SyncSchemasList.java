package io.aiven.klaw.model.response;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class SyncSchemasList implements Serializable {
  private List<SchemaSubjectInfoResponse> schemaSubjectInfoResponseList;
  private int allTopicsCount;
}
