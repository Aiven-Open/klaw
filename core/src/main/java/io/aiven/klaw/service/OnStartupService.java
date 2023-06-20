package io.aiven.klaw.service;

import io.aiven.klaw.config.MigrationUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
- Export Klaw metadata (Admin config, Core data, Requests data) to json files
- Import json files into Klaw metadata
 */
@Slf4j
@Service
public class OnStartupService implements InitializingBean {

  @Autowired ExportImportDataService exportImportDataService;

  @Autowired MigrationUtility migrationUtility;

  @Override
  public void afterPropertiesSet() throws Exception {
    exportImportDataService.importData();
    migrationUtility.startMigration();
  }
}
