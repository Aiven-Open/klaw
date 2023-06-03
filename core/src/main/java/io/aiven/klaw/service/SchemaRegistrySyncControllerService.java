package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.SCH_SYNC_ERR_101;
import static io.aiven.klaw.error.KlawErrorMessages.SCH_SYNC_ERR_102;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.SchemaRequest;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SchemaResetCache;
import io.aiven.klaw.model.SyncSchemaUpdates;
import io.aiven.klaw.model.cluster.SchemaInfoOfTopic;
import io.aiven.klaw.model.cluster.SchemasInfoOfClusterResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.SchemaDetailsResponse;
import io.aiven.klaw.model.response.SchemaSubjectInfoResponse;
import io.aiven.klaw.model.response.SyncSchemasList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SchemaRegistrySyncControllerService {

  public static final String NOT_IN_SYNC = "NOT_IN_SYNC";
  public static final String IN_SYNC = "IN_SYNC";
  public static final String SOURCE_METADATA = "metadata";
  public static final String SOURCE_CLUSTER = "cluster";
  private static final String LEGACY_TOPIC_VERSION = "1.0";
  @Autowired ManageDatabase manageDatabase;

  @Autowired ClusterApiService clusterApiService;

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static final ObjectWriter WRITER_WITH_DEFAULT_PRETTY_PRINTER =
      OBJECT_MAPPER.writerWithDefaultPrettyPrinter();

  public SchemaRegistrySyncControllerService(
      ClusterApiService clusterApiService, MailUtils mailService) {
    this.clusterApiService = clusterApiService;
    this.mailService = mailService;
  }

  // schema versions only
  public SyncSchemasList getSchemasOfEnvironment(
      String kafkaEnvId,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      boolean showAllTopics,
      String source,
      int teamId)
      throws Exception {

    if (source.equals(SOURCE_METADATA)) {
      return getSchemasOfEnvironmentFromMetadataDb(
          kafkaEnvId, pageNo, currentPage, topicNameSearch, teamId);
    } else if (source.equals(SOURCE_CLUSTER)) {
      return getSchemaOfEnvironmentFromCluster(
          kafkaEnvId, pageNo, currentPage, topicNameSearch, showAllTopics);
    } else {
      return new SyncSchemasList();
    }
  }

  // schema versions only
  public SyncSchemasList getSchemasOfEnvironmentFromMetadataDb(
      String kafkaEnvId,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      Integer teamId) {
    SyncSchemasList syncSchemasList = new SyncSchemasList();
    List<SchemaSubjectInfoResponse> schemaInfoList;
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_BACK_SCHEMAS)) {
      return syncSchemasList;
    }

    Env kafkaEnv = manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvId, tenantId);
    if (kafkaEnv.getAssociatedEnv() == null) {
      return syncSchemasList;
    }
    String schemaEnvId = kafkaEnv.getAssociatedEnv().getId();
    schemaInfoList = getSchemasFromDb(kafkaEnvId, tenantId, schemaEnvId);
    syncSchemasList.setAllTopicsCount(schemaInfoList.size());

    if (topicNameSearch != null && topicNameSearch.trim().length() > 0) {
      schemaInfoList =
          schemaInfoList.stream()
              .filter(schema -> schema.getTopic().contains(topicNameSearch.trim()))
              .toList();
    }

    if (teamId != 0) {
      schemaInfoList =
          schemaInfoList.stream().filter(schema -> schema.getTeamId() == teamId).toList();
    }

    schemaInfoList = getPagedResponse(pageNo, currentPage, schemaInfoList, tenantId);
    syncSchemasList.setSchemaSubjectInfoResponseList(schemaInfoList);
    return syncSchemasList;
  }

  private List<SchemaSubjectInfoResponse> getSchemasFromDb(
      String kafkaEnvId, int tenantId, String schemaEnvId) {
    List<SchemaSubjectInfoResponse> schemaInfoList = new ArrayList<>();
    List<Topic> topicList =
        manageDatabase.getTopicsForTenant(tenantId).stream()
            .filter(topic -> topic.getEnvironment().equals(kafkaEnvId))
            .toList();

    Map<String, Set<String>> topicSchemaVersionsInDb =
        manageDatabase
            .getHandleDbRequests()
            .getTopicAndVersionsForEnvAndTenantId(schemaEnvId, tenantId);

    for (Topic topic : topicList) {
      if (topicSchemaVersionsInDb.containsKey(topic.getTopicname())) {
        Set<String> schemaVersions = topicSchemaVersionsInDb.get(topic.getTopicname());

        if (schemaVersions.contains(LEGACY_TOPIC_VERSION)) {
          continue;
        }

        Set<Integer> schemaVersionsInt = new TreeSet<>();
        schemaVersions.forEach(ver -> schemaVersionsInt.add(Integer.parseInt(ver)));

        SchemaSubjectInfoResponse schemaInfo = new SchemaSubjectInfoResponse();
        schemaInfo.setTopic(topic.getTopicname());
        schemaInfo.setSchemaVersions(schemaVersionsInt);
        schemaInfo.setTeamId(topic.getTeamId());

        schemaInfoList.add(schemaInfo);
      }
    }

    return schemaInfoList;
  }

  // schema versions only
  private SyncSchemasList getSchemaOfEnvironmentFromCluster(
      String kafkaEnvId,
      String pageNo,
      String currentPage,
      String topicNameSearch,
      boolean showAllTopics)
      throws KlawException {
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);
    SyncSchemasList syncSchemasList = new SyncSchemasList();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_SCHEMAS)) {
      return syncSchemasList;
    }

    List<SchemaSubjectInfoResponse> schemaSubjectInfoResponseList = new ArrayList<>();

    Env kafkaEnv = manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvId, tenantId);
    if (kafkaEnv.getAssociatedEnv() == null) {
      return syncSchemasList;
    }
    Env schemaEnvSelected =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(kafkaEnv.getAssociatedEnv().getId(), tenantId);
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
            .get(schemaEnvSelected.getClusterId());
    try {
      SchemasInfoOfClusterResponse schemasInfoOfClusterResponse =
          clusterApiService.getSchemasFromCluster(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              tenantId);

      List<Topic> topicsFromSOT =
          manageDatabase.getHandleDbRequests().getSyncTopics(kafkaEnvId, null, tenantId);

      List<SchemaInfoOfTopic> schemaInfoOfTopicList =
          schemasInfoOfClusterResponse.getSchemaInfoOfTopicList();
      for (SchemaInfoOfTopic schemaInfoOfTopic : schemaInfoOfTopicList) {
        SchemaSubjectInfoResponse schemaSubjectInfoResponse = new SchemaSubjectInfoResponse();
        schemaSubjectInfoResponse.setSchemaVersions(schemaInfoOfTopic.getSchemaVersions());
        schemaSubjectInfoResponse.setTopic(schemaInfoOfTopic.getTopic());
        schemaSubjectInfoResponseList.add(schemaSubjectInfoResponse);
      }

      schemaSubjectInfoResponseList =
          filterTopicsNotInDb(
              schemaSubjectInfoResponseList, topicsFromSOT, schemaEnvSelected.getId(), tenantId);

      if (!showAllTopics) {
        schemaSubjectInfoResponseList =
            schemaSubjectInfoResponseList.stream()
                .filter(schema -> schema.getRemarks().equals(NOT_IN_SYNC))
                .toList();
      }

      if (topicNameSearch != null && topicNameSearch.trim().length() > 0) {
        schemaSubjectInfoResponseList =
            schemaSubjectInfoResponseList.stream()
                .filter(schema -> schema.getTopic().contains(topicNameSearch.trim()))
                .toList();
      }

      syncSchemasList.setSchemaSubjectInfoResponseList(
          getPagedResponse(pageNo, currentPage, schemaSubjectInfoResponseList, tenantId));
      return syncSchemasList;
    } catch (Exception e) {
      log.error("Exception:", e);
      throw new KlawException(e.getMessage());
    }
  }

  private List<SchemaSubjectInfoResponse> filterTopicsNotInDb(
      List<SchemaSubjectInfoResponse> schemaSubjectInfoResponseList,
      List<Topic> topicsFromSOT,
      String schemaEnvId,
      int tenantId) {
    List<SchemaSubjectInfoResponse> updatedList = new ArrayList<>();
    Map<String, Set<String>> topicSchemaVersionsInDb =
        manageDatabase
            .getHandleDbRequests()
            .getTopicAndVersionsForEnvAndTenantId(schemaEnvId, tenantId);
    for (SchemaSubjectInfoResponse schemaSubjectInfoResponse : schemaSubjectInfoResponseList) {
      Optional<Topic> optionalTopic =
          topicsFromSOT.stream()
              .filter(t -> t.getTopicname().equals(schemaSubjectInfoResponse.getTopic()))
              .findAny();
      if (optionalTopic.isPresent()) {
        schemaSubjectInfoResponse.setTeamId(optionalTopic.get().getTeamId());
        validateSchemas(
            schemaSubjectInfoResponse,
            topicSchemaVersionsInDb.get(schemaSubjectInfoResponse.getTopic()));
        updatedList.add(schemaSubjectInfoResponse);
      }
    }
    return updatedList;
  }

  private void validateSchemas(
      SchemaSubjectInfoResponse schemaSubjectInfoResponse, Set<String> schemaVersionsOnDb) {
    Set<Integer> schemaVersionsOnCluster = schemaSubjectInfoResponse.getSchemaVersions();

    if (schemaVersionsOnDb == null) {
      schemaSubjectInfoResponse.setRemarks(NOT_IN_SYNC);
      return;
    }

    if (schemaVersionsOnCluster.size() != schemaVersionsOnDb.size()) {
      schemaSubjectInfoResponse.setRemarks(NOT_IN_SYNC);
      return;
    } else {
      schemaSubjectInfoResponse.setRemarks(IN_SYNC);
    }

    schemaVersionsOnCluster.forEach(
        ver -> {
          if (!schemaVersionsOnDb.contains(ver + "")) {
            schemaSubjectInfoResponse.setRemarks(NOT_IN_SYNC);
          }
        });
  }

  public ApiResponse updateSyncSchemas(SyncSchemaUpdates syncSchemaUpdates) throws Exception {
    log.info("syncSchemaUpdates {}", syncSchemaUpdates);

    if (syncSchemaUpdates.getTypeOfSync().equals(PermissionType.SYNC_SCHEMAS.name())) {
      return updateSyncSchemasToMetadata(syncSchemaUpdates);
    } else if (syncSchemaUpdates.getTypeOfSync().equals(PermissionType.SYNC_BACK_SCHEMAS.name())) {
      return updateSyncSchemasToCluster(syncSchemaUpdates);
    } else {
      return ApiResponse.builder().success(false).build();
    }
  }

  private ApiResponse updateSyncSchemasToCluster(SyncSchemaUpdates syncSchemaUpdates)
      throws KlawException {
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_BACK_SCHEMAS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }
    List<String> logArray = new ArrayList<>();
    logArray.add("Topics/Schemas result");

    Env kafkaEnv =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(syncSchemaUpdates.getSourceKafkaEnvSelected(), tenantId);

    if (kafkaEnv.getAssociatedEnv() == null) {
      return ApiResponse.builder().success(false).message(SCH_SYNC_ERR_101).build();
    }

    if (syncSchemaUpdates.getTopicsSelectionType().equals("ALL_TOPICS")) {
      List<SchemaSubjectInfoResponse> schemaInfoList =
          getSchemasFromDb(kafkaEnv.getId(), tenantId, kafkaEnv.getAssociatedEnv().getId());
      List<String> topicList = new ArrayList<>();
      schemaInfoList.forEach(schemaInfo -> topicList.add(schemaInfo.getTopic()));
      syncSchemaUpdates.setTopicList(topicList);
    }

    for (String topicName : syncSchemaUpdates.getTopicList()) {
      // delete all schemas
      ResponseEntity<ApiResponse> apiResponseEntity =
          clusterApiService.deleteSchema(
              topicName, syncSchemaUpdates.getTargetKafkaEnvSelected(), tenantId);

      // check for success or schema may not exist
      if (apiResponseEntity.getBody() != null
          && (apiResponseEntity.getBody().isSuccess()
              || (!apiResponseEntity.getBody().isSuccess()
                  && apiResponseEntity.getBody().getMessage().contains(SCH_SYNC_ERR_102)))) {
        logArray.add("Schemas deleted for " + topicName);
        // create new schemas
        List<MessageSchema> schemaList =
            manageDatabase
                .getHandleDbRequests()
                .getSchemaForTenantAndEnvAndTopic(
                    tenantId, kafkaEnv.getAssociatedEnv().getId(), topicName);
        schemaList =
            schemaList.stream()
                .sorted(Comparator.comparing(a -> Integer.parseInt(a.getSchemaversion())))
                .toList();

        List<MessageSchema> schemaListUpdated = new ArrayList<>();
        for (MessageSchema messageSchema : schemaList) {
          SchemaRequest schemaRequest = new SchemaRequest();
          schemaRequest.setForceRegister(syncSchemaUpdates.isForceRegisterSchema());
          schemaRequest.setSchemafull(messageSchema.getSchemafull());

          ResponseEntity<ApiResponse> apiResponseCreateEntity =
              clusterApiService.postSchema(
                  schemaRequest, kafkaEnv.getAssociatedEnv().getId(), topicName, tenantId);
          ApiResponse apiResponse = apiResponseCreateEntity.getBody();
          Map<String, Object> registerSchemaCustomResponse = null;
          boolean schemaRegistered = false;
          if (apiResponse != null
              && apiResponse.getData() != null
              && apiResponse.getData() instanceof Map<?, ?>) {
            registerSchemaCustomResponse = (Map) apiResponse.getData();
            schemaRegistered = (Boolean) registerSchemaCustomResponse.get("schemaRegistered");
          }
          if (registerSchemaCustomResponse != null
              && (registerSchemaCustomResponse.containsKey("id")
                  && schemaRegistered
                  && registerSchemaCustomResponse.containsKey("compatibility"))) {

            Integer schemaVersion = (Integer) registerSchemaCustomResponse.get("version");
            messageSchema.setSchemaversion(schemaVersion + "");
            schemaRequest.setSchemaId((Integer) registerSchemaCustomResponse.get("id"));
            schemaRequest.setCompatibility(
                (String) registerSchemaCustomResponse.get("compatibility"));
            schemaListUpdated.add(messageSchema);

            logArray.add(
                "Schemas registered on cluster for " + topicName + " Version " + schemaVersion);
          } else {
            logArray.add("Schema NOT updated :" + topicName);
          }
        }

        manageDatabase.getHandleDbRequests().updateDbWithUpdatedVersions(schemaListUpdated);
      } else {
        logArray.add("Schema NOT updated :" + topicName);
      }
    }

    return ApiResponse.builder()
        .success(true)
        .message(ApiResultStatus.SUCCESS.value)
        .data(logArray)
        .build();
  }

  private ApiResponse updateSyncSchemasToMetadata(SyncSchemaUpdates syncSchemaUpdates)
      throws Exception {
    String userDetails = getUserName();
    int tenantId = commonUtilsService.getTenantId(userDetails);

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.SYNC_SCHEMAS)) {
      return ApiResponse.builder()
          .success(false)
          .message(ApiResultStatus.NOT_AUTHORIZED.value)
          .build();
    }
    Env kafkaEnv =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(syncSchemaUpdates.getSourceKafkaEnvSelected(), tenantId);

    if (kafkaEnv.getAssociatedEnv() == null) {
      return ApiResponse.builder().success(false).message(SCH_SYNC_ERR_101).build();
    }

    Env schemaEnvSelected =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(kafkaEnv.getAssociatedEnv().getId(), tenantId);
    KwClusters kwClusters =
        manageDatabase
            .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
            .get(schemaEnvSelected.getClusterId());

    for (String topicName : syncSchemaUpdates.getTopicList()) {
      TreeMap<Integer, Map<String, Object>> schemaObject =
          clusterApiService.getAvroSchema(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              topicName,
              tenantId);

      int teamId = commonUtilsService.getTopicsForTopicName(topicName, tenantId).get(0).getTeamId();

      // delete all versions of topic
      Topic topic = new Topic();
      topic.setEnvironment(schemaEnvSelected.getId());
      topic.setTenantId(tenantId);
      topic.setTopicname(topicName);
      manageDatabase.getHandleDbRequests().deleteSchemas(topic);

      // insert all versions of topic
      List<MessageSchema> schemaList = new ArrayList<>();
      for (Integer schemaVersion : schemaObject.keySet()) {
        MessageSchema messageSchema = new MessageSchema();
        messageSchema.setEnvironment(schemaEnvSelected.getId());
        messageSchema.setTopicname(topicName);
        messageSchema.setTenantId(tenantId);
        messageSchema.setSchemaversion(schemaVersion + "");
        messageSchema.setSchemafull((String) schemaObject.get(schemaVersion).get("schema"));
        messageSchema.setTeamId(teamId);
        messageSchema.setSchemaId((Integer) schemaObject.get(schemaVersion).get("id"));
        messageSchema.setCompatibility(
            (String) schemaObject.get(schemaVersion).get("compatibility"));
        schemaList.add(messageSchema);
      }

      manageDatabase.getHandleDbRequests().insertIntoMessageSchemaSOT(schemaList);
    }

    return ApiResponse.builder()
        .success(true)
        .message("Topics/Schemas " + syncSchemaUpdates.getTopicList())
        .build();
  }

  // schema content either from metadata or cluster
  public SchemaDetailsResponse getSchemaOfTopicFromSource(
      String source, String topicName, int schemaVersion, String kafkaEnvId) throws Exception {
    String userName = getUserName();
    SchemaDetailsResponse schemaDetailsResponse = new SchemaDetailsResponse();
    int tenantId = commonUtilsService.getTenantId(userName);

    Env kafkaEnv = manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvId, tenantId);
    if (kafkaEnv.getAssociatedEnv() == null) {
      return schemaDetailsResponse;
    }

    TreeMap<Integer, Map<String, Object>> schemaObject = null;
    Env schemaEnvSelected =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(kafkaEnv.getAssociatedEnv().getId(), tenantId);
    Object dynamicObj = null;

    if (source.equals(SOURCE_CLUSTER)) {
      KwClusters kwClusters =
          manageDatabase
              .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
              .get(schemaEnvSelected.getClusterId());

      schemaObject =
          clusterApiService.getAvroSchema(
              kwClusters.getBootstrapServers(),
              kwClusters.getProtocol(),
              kwClusters.getClusterName() + kwClusters.getClusterId(),
              topicName,
              tenantId);
      if (schemaObject != null && schemaObject.containsKey(schemaVersion)) {
        dynamicObj =
            OBJECT_MAPPER.readValue(
                (String) schemaObject.get(schemaVersion).get("schema"), Object.class);
      }
    } else if (source.equals(SOURCE_METADATA)) {
      List<MessageSchema> messageSchemaList =
          manageDatabase
              .getHandleDbRequests()
              .getSchemaForTenantAndEnvAndTopicAndVersion(
                  tenantId, kafkaEnv.getAssociatedEnv().getId(), topicName, schemaVersion + "");
      if (!messageSchemaList.isEmpty()) {
        String schemaString = messageSchemaList.get(0).getSchemafull();
        dynamicObj = OBJECT_MAPPER.readValue(schemaString, Object.class);
      }
    }

    if (dynamicObj != null) {
      String jsonSchema = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(dynamicObj);
      schemaDetailsResponse.setSchemaContent(jsonSchema);
      schemaDetailsResponse.setSchemaVersion(schemaVersion + "");
      schemaDetailsResponse.setTopicName(topicName);
      schemaDetailsResponse.setEnvName(kafkaEnv.getName());
    }

    return schemaDetailsResponse;
  }

  private List<SchemaSubjectInfoResponse> getPagedResponse(
      String pageNo,
      String currentPage,
      List<SchemaSubjectInfoResponse> schemaInfoOfTopicList,
      int tenantId) {
    List<SchemaSubjectInfoResponse> pagedTopicSyncList = new ArrayList<>();

    int totalRecs = schemaInfoOfTopicList.size();
    int recsPerPage = 20;

    int totalPages =
        schemaInfoOfTopicList.size() / recsPerPage
            + (schemaInfoOfTopicList.size() % recsPerPage > 0 ? 1 : 0);

    pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);
    int requestPageNo = Integer.parseInt(pageNo);
    int startVar = (requestPageNo - 1) * recsPerPage;
    int lastVar = (requestPageNo) * (recsPerPage);

    List<String> numList = new ArrayList<>();
    commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

    for (int i = 0; i < totalRecs; i++) {

      if (i >= startVar && i < lastVar) {
        SchemaSubjectInfoResponse mp = schemaInfoOfTopicList.get(i);

        mp.setTotalNoPages(totalPages + "");
        mp.setAllPageNos(numList);
        mp.setCurrentPage(pageNo);
        mp.setTeamname(manageDatabase.getTeamNameFromTeamId(tenantId, mp.getTeamId()));
        pagedTopicSyncList.add(mp);
      }
    }
    return pagedTopicSyncList;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  public ApiResponse resetCacheClusterApi(SchemaResetCache schemaResetCache) throws KlawException {
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);
    ResponseEntity<ApiResponse> apiResponseResponseEntity =
        clusterApiService.resetSchemaInfoCache(schemaResetCache.getKafkaEnvId(), tenantId);
    return apiResponseResponseEntity.getBody();
  }
}
