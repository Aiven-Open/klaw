package io.aiven.klaw.dao.test;

import io.aiven.klaw.dao.migration.DataMigration;
import io.aiven.klaw.dao.migration.MigrationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@DataMigration(version = "2.1.0", order = 0)
public class MigrationTestData2x1x0 {

  private boolean success;

  @MigrationRunner
  public boolean migrate() {
    return success;
  }
}
