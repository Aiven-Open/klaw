package io.aiven.klaw.dao.migration;

import io.aiven.klaw.dao.KafkaConnectorRequest;
import io.aiven.klaw.dao.Team;
import io.aiven.klaw.dao.TopicRequest;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.error.KlawDataMigrationException;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.UpdateDataJdbc;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RolesType;
import java.util.List;
import java.util.Optional;
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
      List<UserInfo> users = selectDataJdbc.selectAllUsersInfo(tenantId);
      Optional<UserInfo> superAdmin =
          users.stream()
              .filter(
                  user ->
                      user.getRole().equalsIgnoreCase(RolesType.SUPERADMIN.name())
                          || user.getRole().equalsIgnoreCase(RolesType.ADMIN.name()))
              .findFirst();
      if (!superAdmin.isPresent()) {

        throw new KlawDataMigrationException("Unable to find Superadmin or admin to run queries.");
      }

      List<Integer> teamIds =
          teams.stream().map(team -> team.getTeamId()).collect(Collectors.toList());
      migrateTopics(teamIds, tenantId, superAdmin.get().getUsername());
      migrateConnectors(teamIds, tenantId, superAdmin.get().getUsername());
    }

    return true;
  }

  private void migrateConnectors(List<Integer> teams, Integer tenantId, String superadmin) {
    int numberOfRequests = 0, numberOfRequestsUpdated = 0;
    List<KafkaConnectorRequest> kcRequests =
        selectDataJdbc.selectFilteredKafkaConnectorRequests(
            false, superadmin, null, null, true, Integer.valueOf(tenantId), null, null, false);

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

  private void migrateTopics(List<Integer> teams, Integer tenantId, String superadmin) {
    int numberOfRequests = 0, numberOfRequestsUpdated = 0;
    log.debug("Superuser {} used to execute instructions for tenantId: {}", superadmin, tenantId);
    List<TopicRequest> topicRequests =
        selectDataJdbc.selectFilteredTopicRequests(
            false,
            superadmin,
            null,
            true,
            Integer.valueOf(tenantId),
            null,
            null,
            null,
            null,
            false);

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
