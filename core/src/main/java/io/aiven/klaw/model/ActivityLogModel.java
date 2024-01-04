package io.aiven.klaw.model;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ActivityLogModel implements Serializable {

  @NotNull private Integer req_no;

  @NotNull private String activityName;

  @NotNull private String activityType;

  @NotNull private Timestamp activityTime;

  @NotNull private String activityTimeString;

  @NotNull private String details;

  @NotNull private String user;

  @NotNull private String team;

  @NotNull private Integer teamId;

  @NotNull private String env;

  @NotNull private int tenantId;

  @NotNull private String envName;

  @NotNull private String totalNoPages;

  @NotNull private List<String> allPageNos;

  @NotNull private String currentPage;
}
