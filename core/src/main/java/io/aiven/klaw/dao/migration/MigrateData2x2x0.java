package io.aiven.klaw.dao.migration;

import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawDataMigrationException;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.UpdateDataJdbc;
import io.aiven.klaw.model.enums.RequestOperationType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@DataMigration(version = "2.2.0", order = 0)
@Slf4j
@Configuration // Spring will automatically scan and instantiate this class for retrieval.
public class MigrateData2x2x0 {

  @Autowired private SelectDataJdbc selectDataJdbc;

  @Autowired private UpdateDataJdbc updateDataJdbc;

  public MigrateData2x2x0() {}

  public MigrateData2x2x0(SelectDataJdbc selectDataJdbc, UpdateDataJdbc updateDataJdbc) {
    this.selectDataJdbc = selectDataJdbc;
    this.updateDataJdbc = updateDataJdbc;
  }

  @MigrationRunner()
  public boolean migrate() throws KlawDataMigrationException {
    List<UserInfo> allUserInfo = selectDataJdbc.selectAllUsersAllTenants();
    Set<Integer> tenantIds =
        allUserInfo.stream().map(UserInfo::getTenantId).collect(Collectors.toSet());

    for (int tenantId : tenantIds) {
      List<Team> teams = selectDataJdbc.selectAllTeams(tenantId);

      List<Integer> teamIds =
          teams.stream().map(team -> team.getTeamId()).collect(Collectors.toList());
      migrateTopics(teamIds, tenantId);
      migrateConnectors(teamIds, tenantId);
    }

    return true;
  }

  private void migrateConnectors(List<Integer> teams, int tenantId) {
    int numberOfRequests = 0, numberOfRequestsUpdated = 0;
    List<KafkaConnectorRequest> kcRequests =
        selectDataJdbc.getAllConnectorRequestsByTenantId(tenantId);

    kcRequests =
        kcRequests.stream()
            .filter(
                topicRequest ->
                    RequestOperationType.CLAIM.value.equals(topicRequest.getRequestOperationType()))
            .collect(Collectors.toList());
    numberOfRequests = kcRequests.size();
    for (KafkaConnectorRequest req : kcRequests) {

      if (req.getApprovingTeamId() == null
          && NumberUtils.isCreatable(req.getDescription())
          && teams.contains(Integer.valueOf(req.getDescription()))) {
        log.info(
            "Connector Request : {} of Type {} migrated to latest Database Schema.",
            req.getConnectorId(),
            req.getRequestOperationType());
        req.setApprovingTeamId(req.getDescription());
        updateDataJdbc.updateConnectorRequest(req);
        numberOfRequestsUpdated++;
      }
    }
    log.info(
        "Kafka Connector Claim Request migration completed, of {} Connector Claim requests {} were migrated",
        numberOfRequests,
        numberOfRequestsUpdated);
  }

  private void migrateTopics(List<Integer> teams, int tenantId) {
    int numberOfRequests = 0, numberOfRequestsUpdated = 0;
    List<TopicRequest> topicRequests = selectDataJdbc.getAllTopicRequestsByTenantId(tenantId);

    topicRequests =
        topicRequests.stream()
            .filter(
                topicRequest ->
                    RequestOperationType.CLAIM.value.equals(topicRequest.getRequestOperationType()))
            .collect(Collectors.toList());
    numberOfRequests = topicRequests.size();
    for (TopicRequest req : topicRequests) {
      if (req.getApprovingTeamId() == null
          && NumberUtils.isCreatable(req.getDescription())
          && teams.contains(Integer.valueOf(req.getDescription()))) {
        log.info(
            "Topic Request : {} of Type {} migrated to latest Database Schema.",
            req.getTopicid(),
            req.getRequestOperationType());
        req.setApprovingTeamId(req.getDescription());
        updateDataJdbc.updateTopicRequest(req);
        numberOfRequestsUpdated++;
      }
    }
    log.info(
        "Topic Claim Request migration completed, of {} topic Claim requests {} were migrated",
        numberOfRequests,
        numberOfRequestsUpdated);
  }
}
