package io.aiven.klaw.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.ProductDetails;
import io.aiven.klaw.dao.metadata.KwAdminConfig;
import io.aiven.klaw.dao.metadata.KwData;
import io.aiven.klaw.dao.metadata.KwRequests;
import io.aiven.klaw.helpers.HandleDbRequests;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
Export Klaw metadata (Admin config, Core data, Requests data) to json files
 */
@Component
@Slf4j
public class ExportImportDataService {
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String FILE_PREFIX = "kwmetadata";
  private static final String FILE_EXT = ".json";
  private static final String ADMIN_CONFIG_PREFIX = "admin_config";
  private static final String KW_DATA_PREFIX = "kwdata";
  private static final String KW_REQUEST_DATA_PREFIX = "kwrequests_data";
  @Autowired ManageDatabase manageDatabase;

  @Value("${klaw.export.file.path}")
  private String klawExportFilePath;

  @Value("${klaw.export.scheduler.enable}")
  private boolean exportMetadata;

  @Async("metadataExportTaskExecutor")
  @Scheduled(cron = "${klaw.export.cron.expression}")
  void exportKwMetadataScheduler() {
    if (!exportMetadata) {
      return;
    }
    exportKwMetadata();
  }

  // select data from database, write to files in json format
  private void exportKwMetadata() {
    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();
    KwAdminConfig adminConfig = getAdminConfig(handleDbRequests);
    KwData kwData = getKwData(handleDbRequests);
    KwRequests kwRequestData = getRequestsData(handleDbRequests);

    // write to files
    try {
      String timeStamp = getTimeStamp();
      OBJECT_MAPPER.writeValue(getFile(ADMIN_CONFIG_PREFIX, timeStamp), adminConfig);
      OBJECT_MAPPER.writeValue(getFile(KW_DATA_PREFIX, timeStamp), kwData);
      OBJECT_MAPPER.writeValue(getFile(KW_REQUEST_DATA_PREFIX, timeStamp), kwRequestData);
      log.info("Klaw metadata exported !!");
    } catch (IOException e) {
      log.error("Error during parsing/writing to files : ", e);
    }
  }

  private KwAdminConfig getAdminConfig(HandleDbRequests handleDbRequests) {
    log.info("Selecting Kw Admin Config --- STARTED");
    KwAdminConfig kwMetadata =
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
            .build();
    log.info("Selecting Kw Admin Config --- ENDED");
    return kwMetadata;
  }

  private KwData getKwData(HandleDbRequests handleDbRequests) {
    log.info("Selecting Kw Data --- STARTED");
    KwData kwMetadata =
        KwData.builder()
            .topics(handleDbRequests.getAllTopics())
            .subscriptions(handleDbRequests.getAllSubscriptions())
            .schemas(handleDbRequests.selectAllSchemas())
            .kafkaConnectors(handleDbRequests.getAllConnectors())
            .activityLogs(handleDbRequests.getAllActivityLog())
            .build();
    log.info("Selecting Kw Data --- STARTED");
    return kwMetadata;
  }

  private KwRequests getRequestsData(HandleDbRequests handleDbRequests) {
    log.info("Selecting Kw Requests Data --- STARTED");
    KwRequests kwMetadata =
        KwRequests.builder()
            .topicRequests(handleDbRequests.getAllTopicRequests())
            .subscriptionRequests(handleDbRequests.getAllAclRequests())
            .schemaRequests(handleDbRequests.getAllSchemaRequests())
            .connectorRequests(handleDbRequests.getAllConnectorRequests())
            .userRequests(handleDbRequests.getAllRegisterUsersInfo())
            .build();
    log.info("Selecting Kw Requests Data --- ENDED");
    return kwMetadata;
  }

  private File getFile(String fileName, String timeStamp) {
    return new File(klawExportFilePath + FILE_PREFIX + "-" + fileName + "-" + timeStamp + FILE_EXT);
  }

  private String getTimeStamp() {
    String dataPattern = "yyyy-MM-ddHH-mm-ssSSS";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dataPattern);
    return simpleDateFormat.format(new Date());
  }
}
