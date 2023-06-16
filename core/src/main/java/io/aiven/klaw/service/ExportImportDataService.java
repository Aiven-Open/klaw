package io.aiven.klaw.service;

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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/*
Export Klaw metadata (Admin config, Core data, Requests data) to json files
 */
@Slf4j
@Service
public class ExportImportDataService implements InitializingBean {

  @Value("${klaw.export.users.pwd:WelcomeToKlaw!!}")
  private String pwdAllUsers;

  @Value("${klaw.jasypt.encryptor.secretkey}")
  private String encryptorSecretKey;

  @Value("${klaw.export.file.path:./export}")
  private String klawExportFilePath;

  @Value("${klaw.export.scheduler.enable:false}")
  private boolean exportMetadata;

  @Value("${klaw.import.adminconfig.scheduler.enable:false}")
  private boolean importAdminConfigMetadata;

  @Value("${klaw.import.adminconfig.file.path:path}")
  private String klawImportAdminConfigFilePath;

  @Value("${klaw.import.kwdata.scheduler.enable:false}")
  private boolean importKwDataMetadata;

  @Value("${klaw.import.kwdata.file.path:path}")
  private String klawImportKwDataFilePath;

  @Value("${klaw.import.kwrequestsdata.scheduler.enable:false}")
  private boolean importKwRequestsDataMetadata;

  @Value("${klaw.import.kwrequestsdata.file.path:path}")
  private String klawImportKwRequestsDataFilePath;

  @Value("${klaw.version}")
  private String klawVersion;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final String FILE_EXT = ".json";
  private static final String FILE_PREFIX = "kwmetadata";
  private static final String ADMIN_CONFIG_PREFIX = "admin_config";
  private static final String KW_DATA_PREFIX = "kwdata";
  private static final String KW_REQUEST_DATA_PREFIX = "kwrequests_data";
  @Autowired ManageDatabase manageDatabase;

  @Override
  public void afterPropertiesSet() throws Exception {
    importData();
  }

  private void importData() {
    try {
      log.info("Klaw metadata import started !!");
      OBJECT_MAPPER.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
      HandleDbRequests handleDbRequests = manageDatabase.getHandleDbRequests();
      importKlawAdminConfig(handleDbRequests);
      importKwData(handleDbRequests);
      importKwRequestsData(handleDbRequests);
      log.info("Klaw metadata import finished !!");
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

  private void importKlawAdminConfig(HandleDbRequests handleDbRequests) throws IOException {
    if (importAdminConfigMetadata) {
      KwAdminConfig kwAdminConfig =
          OBJECT_MAPPER.readValue(new File(klawImportAdminConfigFilePath), KwAdminConfig.class);
      kwAdminConfig.getTenants().forEach(handleDbRequests::addNewTenant);
      handleDbRequests.insertDefaultRolesPermissions(kwAdminConfig.getRolesPermissions());
      handleDbRequests.insertDefaultKwProperties(kwAdminConfig.getProperties());
      handleDbRequests.insertProductDetails(kwAdminConfig.getProductDetails());

      kwAdminConfig.getClusters().forEach(handleDbRequests::addNewCluster);
      kwAdminConfig.getEnvironments().forEach(handleDbRequests::addNewEnv);

      kwAdminConfig.getTeams().forEach(handleDbRequests::addNewTeam);
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
    OBJECT_MAPPER.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

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
            .klawVersion(klawVersion)
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
            .klawVersion(klawVersion)
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
            .klawVersion(klawVersion)
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
    String dataPattern = "yyyy-MM-dd-HH-mm-ss";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dataPattern);
    return simpleDateFormat.format(new Date());
  }
}
