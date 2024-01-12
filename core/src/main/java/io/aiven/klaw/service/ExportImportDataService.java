package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.INFRATEAM;
import static io.aiven.klaw.helpers.KwConstants.STAGINGTEAM;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.ProductDetails;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.dao.metadata.KwAdminConfig;
import io.aiven.klaw.dao.metadata.KwData;
import io.aiven.klaw.dao.metadata.KwRequests;
import io.aiven.klaw.helpers.HandleDbRequests;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/*
- Export Klaw metadata (Admin config, Core data, Requests data) to json files
- Import json files into Klaw metadata
 */
@Slf4j
@Service
public class ExportImportDataService {

  @Value("${klaw.export.users.pwd:WelcomeToKlaw!!}")
  private String pwdAllUsers;

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  @Value("${klaw.export.file.path:./export}")
  private String klawExportFilePath;

  @Value("${klaw.export.scheduler.enable:false}")
  private boolean exportMetadata;

  @Value("${klaw.import.enable:false}")
  private boolean importMetadata;

  @Value("${klaw.import.adminconfig.enable:false}")
  private boolean importAdminConfigMetadata;

  @Value("${klaw.import.adminconfig.file.path:path}")
  private String klawImportAdminConfigFilePath;

  @Value("${klaw.import.kwdata.enable:false}")
  private boolean importKwDataMetadata;

  @Value("${klaw.import.kwdata.file.path:path}")
  private String klawImportKwDataFilePath;

  @Value("${klaw.import.kwrequestsdata.enable:false}")
  private boolean importKwRequestsDataMetadata;

  @Value("${klaw.import.kwrequestsdata.file.path:path}")
  private String klawImportKwRequestsDataFilePath;

  @Autowired BuildProperties buildProperties;

  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true)
          .configure(SerializationFeature.WRAP_ROOT_VALUE, true);
  ;
  private static final String FILE_EXT = ".json";
  private static final String FILE_PREFIX = "kwmetadata";
  private static final String ADMIN_CONFIG_PREFIX = "admin_config";
  private static final String KW_DATA_PREFIX = "kwdata";
  private static final String KW_REQUEST_DATA_PREFIX = "kwrequests_data";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss").withZone(ZoneId.systemDefault());

  @Autowired ManageDatabase manageDatabase;

  @SchedulerLock(
      name = "TaskScheduler_ImportKlawData",
      lockAtLeastFor = "${klaw.shedlock.lockAtLeastFor:PT30M}",
      lockAtMostFor = "${klaw.shedlock.lockAtMostFor:PT60M}")
  void importData() {
    try {
      if (importMetadata) {
        HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();
        importKlawAdminConfig(handleDbRequests);
        importKwData(handleDbRequests);
        importKwRequestsData(handleDbRequests);
      }
    } catch (IOException e) {
      log.error("Error during parsing/writing to files : ", e);
    }
  }

  private void importKwRequestsData(HandleDbRequests handleDbRequests) throws IOException {
    if (importKwRequestsDataMetadata) {
      KwRequests kwRequests =
          OBJECT_MAPPER.readValue(new File(klawImportKwRequestsDataFilePath), KwRequests.class);
      kwRequests.getTopicRequests().forEach(handleDbRequests::requestForTopic);
      kwRequests.getSubscriptionRequests().forEach(handleDbRequests::requestForAcl);
      kwRequests.getSchemaRequests().forEach(handleDbRequests::requestForSchema);
      kwRequests.getConnectorRequests().forEach(handleDbRequests::requestForConnector);
      log.info("Klaw KwRequestsData metadata imported !!");
    }
  }

  private void importKwData(HandleDbRequests handleDbRequests) throws IOException {
    if (importKwDataMetadata) {
      KwData kwData = OBJECT_MAPPER.readValue(new File(klawImportKwDataFilePath), KwData.class);
      handleDbRequests.addToSynctopics(kwData.getTopics());
      handleDbRequests.addToSyncacls(kwData.getSubscriptions());
      handleDbRequests.insertIntoMessageSchemaSOT(kwData.getSchemas());
      handleDbRequests.addToSyncConnectors(kwData.getKafkaConnectors());
      log.info("Klaw KwData metadata imported !!");
    }
  }

  void importKlawAdminConfig(HandleDbRequests handleDbRequests) throws IOException {
    if (importAdminConfigMetadata) {
      KwAdminConfig kwAdminConfig =
          OBJECT_MAPPER.readValue(new File(klawImportAdminConfigFilePath), KwAdminConfig.class);
      kwAdminConfig.getTenants().forEach(handleDbRequests::addNewTenant);
      handleDbRequests.insertDefaultRolesPermissions(kwAdminConfig.getRolesPermissions());
      handleDbRequests.insertDefaultKwProperties(kwAdminConfig.getProperties());
      handleDbRequests.insertProductDetails(kwAdminConfig.getProductDetails());

      kwAdminConfig.getClusters().forEach(handleDbRequests::addNewCluster);
      kwAdminConfig.getEnvironments().forEach(handleDbRequests::addNewEnv);

      kwAdminConfig
          .getTeams()
          .forEach(
              team -> {
                if (!team.getTeamname().equals(STAGINGTEAM)
                    && !team.getTeamname().equals(INFRATEAM)) {
                  handleDbRequests.addNewTeam(team);
                }
              });
      kwAdminConfig.getUsers().forEach(handleDbRequests::addNewUser);

      log.info("Klaw Admin config metadata imported !!");
    }
  }

  @Async("metadataExportTaskExecutor")
  @Scheduled(cron = "${klaw.export.cron.expression:0 0 0 * * ?}")
  void exportKwMetadataScheduler() {
    if (!exportMetadata) {
      return;
    }

    exportKwMetadata();
  }

  // select data from database, write to files in json format
  private void exportKwMetadata() {
    String timeStamp = getTimeStamp();
    HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();
    KwAdminConfig adminConfig = getAdminConfig(handleDbRequests, timeStamp);
    KwData kwData = getKwData(handleDbRequests, timeStamp);
    KwRequests kwRequestData = getRequestsData(handleDbRequests, timeStamp);

    // write to files
    try {
      OBJECT_MAPPER
          .writerWithDefaultPrettyPrinter()
          .writeValue(getFile(ADMIN_CONFIG_PREFIX, timeStamp), adminConfig);
      OBJECT_MAPPER
          .writerWithDefaultPrettyPrinter()
          .writeValue(getFile(KW_DATA_PREFIX, timeStamp), kwData);
      OBJECT_MAPPER
          .writerWithDefaultPrettyPrinter()
          .writeValue(getFile(KW_REQUEST_DATA_PREFIX, timeStamp), kwRequestData);
      log.info("Klaw metadata exported !!");
    } catch (IOException e) {
      log.error("Error during parsing/writing to files : ", e);
    }
  }

  // tenants, clusters, environments, roles, permissions, teams, users, properties
  public KwAdminConfig getAdminConfig(HandleDbRequests handleDbRequests, String timeStamp) {
    log.info(
        "Selecting Kw Admin Config (tenants, clusters, environments, roles, permissions, teams, users, properties) --- STARTED");
    List<UserInfo> userList = getUpdatedUserList(handleDbRequests.getAllUsersAllTenants());

    KwAdminConfig kwMetadata =
        KwAdminConfig.builder()
            .tenants(handleDbRequests.getTenants())
            .clusters(handleDbRequests.getClusters())
            .environments(handleDbRequests.getEnvs())
            .rolesPermissions(handleDbRequests.getRolesPermissions())
            .teams(handleDbRequests.getTeams())
            .users(userList)
            .properties(handleDbRequests.getKwProperties())
            .productDetails(handleDbRequests.getProductDetails("Klaw").orElse(new ProductDetails()))
            .klawVersion(buildProperties.getVersion())
            .createdTime(timeStamp)
            .build();
    log.info("Selecting Kw Admin Config --- ENDED");
    return kwMetadata;
  }

  private List<UserInfo> getUpdatedUserList(List<UserInfo> userList) {
    BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());
    String encryptedPwd = textEncryptor.encrypt(pwdAllUsers);
    userList.forEach(user -> user.setPwd(encryptedPwd));

    return userList;
  }

  // Get core configuration of topics, acls, schemas, connectors
  public KwData getKwData(HandleDbRequests handleDbRequests, String timeStamp) {
    log.info("Selecting Kw Data (topics, acls, schemas, connectors) --- STARTED");
    KwData kwMetadata =
        KwData.builder()
            .topics(handleDbRequests.getAllTopics())
            .subscriptions(handleDbRequests.getAllSubscriptions())
            .schemas(handleDbRequests.getAllSchemas())
            .kafkaConnectors(handleDbRequests.getAllConnectors())
            .klawVersion(buildProperties.getVersion())
            .createdTime(timeStamp)
            .build();
    log.info("Selecting Kw Data --- STARTED");
    return kwMetadata;
  }

  // Get requests data and activity log
  public KwRequests getRequestsData(HandleDbRequests handleDbRequests, String timeStamp) {
    log.info(
        "Selecting Kw Requests Data (topic, subscription, schema and connector requests, activity log)--- STARTED");
    KwRequests kwMetadata =
        KwRequests.builder()
            .topicRequests(handleDbRequests.getAllTopicRequests())
            .subscriptionRequests(handleDbRequests.getAllAclRequests())
            .schemaRequests(handleDbRequests.getAllSchemaRequests())
            .connectorRequests(handleDbRequests.getAllConnectorRequests())
            .klawVersion(buildProperties.getVersion())
            .createdTime(timeStamp)
            .build();
    log.info("Selecting Kw Requests Data --- ENDED");
    return kwMetadata;
  }

  private File getFile(String fileName, String timeStamp) {
    String filePath =
        klawExportFilePath
            .concat("/")
            .concat(FILE_PREFIX)
            .concat("-")
            .concat(fileName)
            .concat("-")
            .concat(timeStamp)
            .concat(FILE_EXT);
    File file = new File(filePath);
    log.info("File : {}", filePath);
    return file;
  }

  private String getTimeStamp() {
    return DATE_TIME_FORMATTER.format(Instant.now());
  }
}
