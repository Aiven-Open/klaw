package io.aiven.klaw.dao;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DBSaveResponse<T> {

  private List<T> entities;
  private String resultStatus;
}
