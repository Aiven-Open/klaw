package io.aiven.klaw.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.ProductDetails;
import io.aiven.klaw.dao.metadata.KwAdminConfig;
import io.aiven.klaw.dao.metadata.KwMetadata;
import io.aiven.klaw.helpers.HandleDbRequests;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExportImportDataService {
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  @Autowired ManageDatabase manageDatabase;

  @Value("${klaw.export.file.path}")
  private String klawExportFilePath;

  @Value("${klaw.export.file.prefix}")
  private String klawExportFileNamePrefix;

  @Value("${klaw.export.file.type}")
  private String klawExportFileType;

  @Value("${klaw.export.scheduler.enable}")
  private boolean exportMetadata;

  @Scheduled(cron = "${klaw.export.cron.expression}")
  private void exportKwMetadata() {
    if (!exportMetadata) {
      return;
    }
    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();
    KwMetadata kwMetadata =
        KwMetadata.builder()
            .kwAdminConfig(
                KwAdminConfig.builder()
                    .tenants(handleDbRequests.getTenants())
                    .clusters(handleDbRequests.getClusters())
                    .environments(handleDbRequests.selectEnvs())
                    .rolesPermissions(handleDbRequests.getRolesPermissions())
                    .teams(handleDbRequests.selectTeams())
                    .users(handleDbRequests.selectAllUsersAllTenants())
                    .properties(handleDbRequests.selectKwProperties())
                    .productDetails(
                        handleDbRequests.selectProductDetails("Klaw").orElse(new ProductDetails()))
                    .build())
            .build();
    try {
      OBJECT_MAPPER.writeValue(getFile(), kwMetadata);
      log.info("KwMetadata loaded");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private File getFile() {
    String dataPattern = "yyyy-MM-ddHH-mm-ssSSS";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dataPattern);
    return new File(
        klawExportFilePath
            + klawExportFileNamePrefix
            + "-"
            + simpleDateFormat.format(new Date())
            + "."
            + klawExportFileType);
  }
}
