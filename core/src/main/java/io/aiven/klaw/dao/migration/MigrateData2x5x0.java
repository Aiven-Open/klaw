package io.aiven.klaw.dao.migration;

import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import io.aiven.klaw.model.enums.EntityType;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@DataMigration(version = "2.5.0", order = 3)
@Slf4j
@Configuration // Spring will automatically scan and instantiate this class for retrieval.
public class MigrateData2x5x0 {

  @Autowired private SelectDataJdbc selectDataJdbc;

  @Autowired private InsertDataJdbc insertDataJdbc;

  public MigrateData2x5x0() {}

  public MigrateData2x5x0(SelectDataJdbc selectDataJdbc, InsertDataJdbc insertDataJdbc) {
    this.selectDataJdbc = selectDataJdbc;
    this.insertDataJdbc = insertDataJdbc;
  }

  @MigrationRunner()
  public boolean migrate() {
    log.info("Start to migrate 2.5.0 data. Update sequences.");

    List<KwTenants> tenantsList = selectDataJdbc.getTenants();
    int defaultStartingSequence = 1;

    if ((tenantsList.size() * 3L) == selectDataJdbc.getDataFromKwEntitySequences()) {
      // Sequences already updated. Nothing to do.
      return true;
    }

    for (KwTenants kwTenants : tenantsList) {
      int tenantId = kwTenants.getTenantId();
      Integer lastId = selectDataJdbc.getNextClusterId(tenantId);
      lastId = getNextId(1, lastId);
      insertDataJdbc.insertIntoKwEntitySequence(EntityType.CLUSTER.name(), lastId, tenantId);

      lastId = selectDataJdbc.getNextEnvId(tenantId);
      lastId = getNextId(1, lastId);
      insertDataJdbc.insertIntoKwEntitySequence(EntityType.ENVIRONMENT.name(), lastId, tenantId);

      lastId = selectDataJdbc.getNextTeamId(tenantId);
      lastId = getNextId(1001, lastId);
      insertDataJdbc.insertIntoKwEntitySequence(EntityType.TEAM.name(), lastId, tenantId);
    }

    return true;
  }

  private static Integer getNextId(int defaultStartingSequence, Integer nextId) {
    if (nextId == null) {
      nextId = defaultStartingSequence;
    } else {
      nextId = nextId + 1;
    }
    return nextId;
  }
}
