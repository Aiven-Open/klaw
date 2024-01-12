package io.aiven.klaw.dao.migration;

import static io.aiven.klaw.helpers.KwConstants.KLAW_OPTIONAL_PERMISSION_NEW_TOPIC_CREATION_KEY;
import static io.aiven.klaw.model.enums.PermissionType.APPROVE_TOPICS_CREATE;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@DataMigration(version = "2.8.0", order = 4)
@Slf4j
@Configuration // Spring will automatically scan and instantiate this class for retrieval.
public class MigrateData2x8x0 {

  @Autowired private InsertDataJdbc insertDataJdbc;
  @Autowired private SelectDataJdbc selectDataJdbc;

  @Autowired private ManageDatabase manageDatabase;

  public MigrateData2x8x0() {}

  @MigrationRunner()
  public boolean migrate() {
    log.info(
        "Start to migrate 2.8.0 data. Add new server property "
            + KLAW_OPTIONAL_PERMISSION_NEW_TOPIC_CREATION_KEY);

    List<UserInfo> allUserInfo = selectDataJdbc.selectAllUsersAllTenants();
    Set<Integer> tenantIds =
        allUserInfo.stream().map(UserInfo::getTenantId).collect(Collectors.toSet());

    for (int tenantId : tenantIds) {
      List<KwProperties> kwPropertiesList = selectDataJdbc.selectAllKwPropertiesPerTenant(tenantId);
      if (kwPropertiesList.stream()
          .noneMatch(
              kwProperties ->
                  kwProperties
                      .getKwKey()
                      .equals(KLAW_OPTIONAL_PERMISSION_NEW_TOPIC_CREATION_KEY))) {
        KwProperties kwProperties38 =
            new KwProperties(
                KLAW_OPTIONAL_PERMISSION_NEW_TOPIC_CREATION_KEY,
                tenantId,
                "false",
                "Enforce extra permission " + APPROVE_TOPICS_CREATE + " to create new topics");
        kwPropertiesList.add(kwProperties38);

        insertDataJdbc.insertDefaultKwProperties(List.of(kwProperties38));
        manageDatabase.loadEnvMapForOneTenant(tenantId);
        manageDatabase.loadKwPropsPerOneTenant(null, tenantId);
      }
    }

    return true;
  }
}
