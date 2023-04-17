package io.aiven.klaw.model.response;

import lombok.Data;

@Data
public class DashboardStats {
  private int producerCount;
  private int consumerCount;
  private int teamMembersCount;
}
