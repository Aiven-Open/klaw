package io.aiven.klaw.dao.migration;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@DataMigration(version = "2.4.0", order = 2)
@Slf4j
@Configuration // Spring will automatically scan and instantiate this class for retrieval.
public class MigrateData2x4x0 {

  @Autowired private SelectDataJdbc selectDataJdbc;

  @Autowired private InsertDataJdbc insertDataJdbc;

  @Autowired private ManageDatabase manageDatabase;

  public MigrateData2x4x0() {}

  public MigrateData2x4x0(
      SelectDataJdbc selectDataJdbc, InsertDataJdbc insertDataJdbc, ManageDatabase manageDatabase) {
    this.selectDataJdbc = selectDataJdbc;
    this.insertDataJdbc = insertDataJdbc;
    this.manageDatabase = manageDatabase;
  }

  @MigrationRunner()
  public boolean migrate() {
    log.info("Start to migrate 2.4.0 data");
    List<UserInfo> allUserInfo = selectDataJdbc.selectAllUsersAllTenants();
    Set<Integer> tenantIds =
        allUserInfo.stream().map(UserInfo::getTenantId).collect(Collectors.toSet());

    for (int tenantId : tenantIds) {
      migrateKwProperties(tenantId);
      manageDatabase.loadEnvMapForOneTenant(tenantId);
    }

    return true;
  }

  private void migrateKwProperties(Integer tenantId) {
    log.info("Migrate KwProperties for tenant {}", tenantId);
    List<KwProperties> properties = selectDataJdbc.selectAllKwPropertiesPerTenant(tenantId);

    try {

      // Setting for every property as this is the first introduction of the new column with eanbled
      // true/false.
      // In the future an if statement to only change the property being deprecated should be used.
      properties.forEach(
          property -> property.setEnabled(!deprecatedKwProperties().contains(property.getKwKey())));
      insertDataJdbc.insertDefaultKwProperties(properties);
    } catch (Exception ex) {
      log.error("Exception caught: ", ex);
    }
  }

  private static List<String> deprecatedKwProperties() {

    // When removing properties ensure it's also removed from DefaultDataService
    return Arrays.asList("klaw.superuser.mailid");
  }
}
