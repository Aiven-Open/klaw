package io.aiven.klaw.dao;

import io.aiven.klaw.model.enums.ApiResultStatus;
import java.util.List;
import lombok.Builder;
import lombok.Data;

/**
 * This Class allows the return of the entities that are saved and stored in the database to be
 * returned when required to be actioned on further e.g. addition or removal from caches.
 *
 * @param <T> The Object type being operated on by the database.
 */
@Data
@Builder
public class CRUDResponse<T> {

  private List<T> entities;
  private String resultStatus;

  public static final <T> CRUDResponse<T> ok(List<T> entities) {
    return CRUDResponse.<T>builder()
        .resultStatus(ApiResultStatus.SUCCESS.value)
        .entities(entities)
        .build();
  }

  public static final <T> CRUDResponse<T> notOk(List<T> entities) {
    return CRUDResponse.<T>builder()
        .resultStatus(ApiResultStatus.FAILURE.value)
        .entities(entities)
        .build();
  }
}
