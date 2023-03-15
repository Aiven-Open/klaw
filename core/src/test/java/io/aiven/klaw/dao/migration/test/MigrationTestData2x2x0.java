package io.aiven.klaw.dao.migration.test;

import io.aiven.klaw.dao.migration.DataMigration;
import io.aiven.klaw.dao.migration.MigrationRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@DataMigration(version = "2.2.0", order =1)
public class MigrationTestData2x2x0 {

    private boolean success;

    @MigrationRunner
    public boolean migrate(){
        return success;
    }

}
