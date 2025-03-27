package io.aiven.klaw.dao.migration;

import static io.aiven.klaw.service.MailUtils.NEW_USER_ADDED_V2_KEY;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.KwTenants;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@DataMigration(version = "2.10.0", order = 5)
@Slf4j
@Configuration // Spring will automatically scan and instantiate this class for retrieval.
public class MigrateData2x10x0 {

  @Autowired private InsertDataJdbc insertDataJdbc;
  @Autowired private SelectDataJdbc selectDataJdbc;

  @Autowired private ManageDatabase manageDatabase;

  public MigrateData2x10x0() {}

  protected MigrateData2x10x0(
      SelectDataJdbc selectDataJdbc, ManageDatabase manageDatabase, InsertDataJdbc insertDataJdbc) {
    this.selectDataJdbc = selectDataJdbc;
    this.manageDatabase = manageDatabase;
    this.insertDataJdbc = insertDataJdbc;
  }

  @MigrationRunner()
  public boolean migrate() {
    log.info("Start to migrate 2.10.0 data. Add new user email content property ");

    List<Integer> tenantIds =
        selectDataJdbc.getTenants().stream().map(KwTenants::getTenantId).toList();

    for (int tenantId : tenantIds) {
      List<KwProperties> kwPropertiesList =
          new ArrayList<>(selectDataJdbc.selectAllKwPropertiesPerTenant(tenantId));

      if (kwPropertiesList.stream()
          .noneMatch(kwProperties -> kwProperties.getKwKey().equals(NEW_USER_ADDED_V2_KEY))) {
        log.info("Add new property {} to the database", NEW_USER_ADDED_V2_KEY);
        KwProperties kwProperties =
            new KwProperties(
                NEW_USER_ADDED_V2_KEY,
                tenantId,
                KwConstants.MAIL_NEWUSERADDED_V2_CONTENT,
                "Email notification body after a new user is added");
        kwPropertiesList.add(kwProperties);

        insertDataJdbc.insertDefaultKwProperties(List.of(kwProperties));
        manageDatabase.loadEnvMapForOneTenant(tenantId);
        manageDatabase.loadKwPropsPerOneTenant(null, tenantId);
      }
    }

    return true;
  }
}
