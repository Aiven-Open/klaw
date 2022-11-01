package io.aiven.klaw.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SyncBackAcls {

  private String[] aclIds;

  @NonNull private String sourceEnv;

  @NonNull private String targetEnv;

  @NonNull private String typeOfSync;
}
