package io.aiven.klaw.dao.migration;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwProperties;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.helpers.db.rdbms.InsertDataJdbc;
import io.aiven.klaw.helpers.db.rdbms.SelectDataJdbc;
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
      addKwProperties(tenantId);
      manageDatabase.loadEnvMapForOneTenant(tenantId);
      manageDatabase.loadKwPropsPerOneTenant(null, tenantId);
    }

    return true;
  }

  private void migrateKwProperties(Integer tenantId) {
    log.info("Update email KwProperties for tenant {}", tenantId);
    List<KwProperties> properties = selectDataJdbc.selectAllKwPropertiesPerTenant(tenantId);

    try {
      if (manageDatabase.getKwPropertyValue("klaw.mail.passwordchanged.content", tenantId) == "") {
        KwProperties kwProperty =
            new KwProperties(
                "klaw.mail.passwordchanged.content",
                tenantId,
                KwConstants.MAIL_PASSWORDCHANGED_CONTENT,
                "Email notification body for password update.");
        properties.add(kwProperty);
      }
      String propertyToUpdate =
          manageDatabase.getKwPropertyValue("klaw.mail.passwordreset.content", tenantId);
      // If it has been customized do not update automatically.
      if (propertyToUpdate.equals(
          "Dear User, \\nYou have requested for reset password on your Klaw account. \\n\\nUser name : %s \\nYour new Password : %s ")) {
        log.info("Updated Password Reset email.");
        KwProperties passwordResetProperty =
            new KwProperties(
                "klaw.mail.passwordreset.content",
                tenantId,
                KwConstants.MAIL_PASSWORDRESET_CONTENT,
                "Email notification body for password reset");
        properties.remove(propertyToUpdate);
        properties.add(passwordResetProperty);
      } else {
        log.info("Password Reset Property not updated.");
      }

      insertDataJdbc.insertDefaultKwProperties(properties);

    } catch (Exception ex) {
      log.error("Exception caught: ", ex);
    }
  }

  private void addKwProperties(Integer tenantId) {
    log.info("Update promotion and update email KwProperties for tenant {}", tenantId);
    List<KwProperties> properties = selectDataJdbc.selectAllKwPropertiesPerTenant(tenantId);

    try {
      KwProperties kwProperties37 = null, kwProperties36 = null;
      if (manageDatabase.getKwPropertyValue("klaw.mail.topicpromotionrequest.content", tenantId)
          == "") {
        kwProperties36 =
            new KwProperties(
                "klaw.mail.topicpromotionrequest.content",
                tenantId,
                KwConstants.MAIL_TOPICPROMOTIONREQUEST_CONTENT,
                "Email notification body for a new Topic Promotion Request");
        properties.add(kwProperties36);
      }
      if (manageDatabase.getKwPropertyValue("klaw.mail.topicupdaterequest.content", tenantId)
          == "") {
        kwProperties37 =
            new KwProperties(
                "klaw.mail.topicupdaterequest.content",
                tenantId,
                KwConstants.MAIL_TOPICUPDATEREQUEST_CONTENT,
                "Email notification body for a new Topic Update Request");
        log.info("loaded kw properties");
        properties.add(kwProperties37);
      }

      if (kwProperties37 != null || kwProperties36 != null) {
        log.info("Updated email messages for Promotion and Update topic requests.");
        insertDataJdbc.insertDefaultKwProperties(properties);
      } else {
        log.info("Did not update email messages for Promotion and Update topic requests.");
      }

    } catch (Exception ex) {
      log.error("Exception caught: ", ex);
    }
  }
}
