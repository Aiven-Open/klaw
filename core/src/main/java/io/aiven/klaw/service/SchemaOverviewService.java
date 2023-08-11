package io.aiven.klaw.service;

import static io.aiven.klaw.helpers.KwConstants.ORDER_OF_TOPIC_ENVS;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.MessageSchema;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.enums.PromotionStatusType;
import io.aiven.klaw.model.enums.RequestOperationType;
import io.aiven.klaw.model.enums.RequestStatus;
import io.aiven.klaw.model.response.PromotionStatus;
import io.aiven.klaw.model.response.SchemaDetailsPerEnv;
import io.aiven.klaw.model.response.SchemaOverview;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchemaOverviewService extends BaseOverviewService {

  public static final String SCHEMA_ID = "id";
  public static final String SCHEMA_COMPATIBILITY = "compatibility";
  public static final String SCHEMA = "schema";

  public SchemaOverviewService(MailUtils mailService) {
    super(mailService);
  }

  public SchemaOverview getSchemaOfTopic(
      String topicNameSearch, int schemaVersionSearch, String kafkaEnvId) {
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    SchemaOverview schemaOverview = new SchemaOverview();
    schemaOverview.setTopicExists(true);
    boolean retrieveSchemas = true;
    updateAvroSchema(
        topicNameSearch,
        schemaVersionSearch,
        kafkaEnvId,
        retrieveSchemas,
        schemaOverview,
        userName,
        tenantId);
    return schemaOverview;
  }

  private void updateAvroSchema(
      String topicNameSearch,
      int schemaVersionSearch,
      String kafkaEnvId,
      boolean retrieveSchemas,
      SchemaOverview schemaOverview,
      String userName,
      int tenantId) {
    if (schemaOverview.isTopicExists() && retrieveSchemas) {
      SchemaDetailsPerEnv schemaDetailsPerEnv = new SchemaDetailsPerEnv();
      List<Integer> schemaVersions = new ArrayList<>();
      schemaOverview.setAllSchemaVersions(schemaVersions);

      Env schemaEnv;
      // Get first base kafka env
      Env kafkaEnv = manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvId, tenantId);
      EnvTag associatedSchemaEnv = kafkaEnv.getAssociatedEnv();
      if (associatedSchemaEnv != null) {
        schemaEnv =
            manageDatabase
                .getHandleDbRequests()
                .getEnvDetails(associatedSchemaEnv.getId(), tenantId);
      } else {
        return;
      }

      List<Topic> topics =
          manageDatabase
              .getHandleDbRequests()
              .getAllTopicsByTopicNameAndTeamIdAndTenantId(
                  topicNameSearch, commonUtilsService.getTeamId(userName), tenantId);

      Object dynamicObj;
      Map<String, Object> hashMapSchemaObj;
      String schemaOfObj;

      try {
        log.debug("UpdateAvroSchema - Process env {}", schemaEnv);

        KwClusters kwClusters =
            manageDatabase
                .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
                .get(schemaEnv.getClusterId());

        List<MessageSchema> topicSchemaVersionsInDb =
            manageDatabase
                .getHandleDbRequests()
                .getSchemaForTenantAndEnvAndTopic(tenantId, schemaEnv.getId(), topicNameSearch);
        boolean schemaUpdated = false;
        if (!topicSchemaVersionsInDb.isEmpty()) {
          schemaUpdated =
              topicSchemaVersionsInDb.stream()
                  .noneMatch(
                      msgSchema ->
                          (msgSchema.getSchemaversion() != null
                                  && msgSchema.getSchemaversion().contains("."))
                              || msgSchema.getCompatibility() == null
                              || msgSchema.getSchemaId() == null);
        }

        SortedMap<Integer, Map<String, Object>> schemaObjects = new TreeMap<>();
        schemaObjects =
            getSchemasMap(
                topicNameSearch,
                tenantId,
                kwClusters,
                topicSchemaVersionsInDb,
                schemaUpdated,
                schemaObjects);
        // If the schemaObject is null ie does not exist do not try to manipulate it.
        if (schemaObjects != null && !schemaObjects.isEmpty()) {
          Set<Integer> allVersions = schemaObjects.keySet();
          List<Integer> allVersionsList = new ArrayList<>(allVersions);
          allVersionsList.sort(Collections.reverseOrder());
          Integer latestSchemaVersion = allVersionsList.get(0);
          schemaOverview.setLatestVersion(latestSchemaVersion);

          schemaOverview.getAllSchemaVersions().addAll(allVersionsList);
          try {
            if (latestSchemaVersion == schemaVersionSearch) {
              schemaVersionSearch = 0;
            }
          } catch (NumberFormatException ignored) {
          }

          // get latest version
          if (schemaVersionSearch == 0) {
            hashMapSchemaObj = schemaObjects.get(latestSchemaVersion);
            schemaOfObj = (String) hashMapSchemaObj.get(SCHEMA);
            schemaDetailsPerEnv.setLatest(true);
            setSchemaDetailsPerEnvVersionAndCompatibility(
                tenantId, schemaDetailsPerEnv, hashMapSchemaObj, latestSchemaVersion, schemaEnv);
            if (schemaObjects.size() > 1) {
              schemaDetailsPerEnv.setShowNext(true);
              schemaDetailsPerEnv.setShowPrev(false);
              int indexOfVersion = allVersionsList.indexOf(latestSchemaVersion);
              schemaDetailsPerEnv.setNextVersion(allVersionsList.get(indexOfVersion + 1));
            }
          } else {
            hashMapSchemaObj = schemaObjects.get(schemaVersionSearch);
            schemaOfObj = (String) hashMapSchemaObj.get(SCHEMA);
            schemaDetailsPerEnv.setLatest(false);
            setSchemaDetailsPerEnvVersionAndCompatibility(
                tenantId, schemaDetailsPerEnv, hashMapSchemaObj, schemaVersionSearch, schemaEnv);
            if (schemaObjects.size() > 1) {
              int indexOfVersion = allVersionsList.indexOf(schemaVersionSearch);
              if (indexOfVersion + 1 == allVersionsList.size()) {
                schemaDetailsPerEnv.setShowNext(false);
                schemaDetailsPerEnv.setShowPrev(true);
                schemaDetailsPerEnv.setPrevVersion(allVersionsList.get(indexOfVersion - 1));
              } else {
                schemaDetailsPerEnv.setShowNext(true);
                schemaDetailsPerEnv.setShowPrev(true);
                schemaDetailsPerEnv.setPrevVersion(allVersionsList.get(indexOfVersion - 1));
                schemaDetailsPerEnv.setNextVersion(allVersionsList.get(indexOfVersion + 1));
              }
            }
          }

          schemaDetailsPerEnv.setEnv(schemaEnv.getName());
          dynamicObj = OBJECT_MAPPER.readValue(schemaOfObj, Object.class);
          schemaOfObj = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(dynamicObj);
          schemaDetailsPerEnv.setContent(schemaOfObj);
          schemaOverview.setSchemaExists(true);

          // A team owns a topic across all environments so we can assume if the search returned
          // one or more topics it is owned by this users team.
          if (topics.size() > 0) {

            // Set Promotion Details
            processSchemaPromotionDetails(
                topicNameSearch,
                schemaOverview,
                tenantId,
                schemaEnv,
                topics.stream().map(Topic::getEnvironment).toList());
            log.info("Getting schema details for: " + topicNameSearch);
          }
        }
      } catch (Exception e) {
        log.error("Error ", e);
      }

      if (schemaOverview.isSchemaExists()) {
        log.debug("SchemaDetails {}", schemaDetailsPerEnv);
        schemaOverview.setSchemaDetailsPerEnv(schemaDetailsPerEnv);
      }
      schemaOverview.setCreateSchemaAllowed(
          commonUtilsService.isCreateNewSchemaAllowed(schemaEnv.getId(), tenantId));
    }
  }

  private void setSchemaDetailsPerEnvVersionAndCompatibility(
      int tenantId,
      SchemaDetailsPerEnv schemaDetailsPerEnv,
      Map<String, Object> hashMapSchemaObj,
      Integer latestSchemaVersion,
      Env schemaEnv) {
    updateIdAndCompatibility(schemaDetailsPerEnv, hashMapSchemaObj);
    schemaDetailsPerEnv.setVersion(latestSchemaVersion);
  }

  private static void updateIdAndCompatibility(
      SchemaDetailsPerEnv schemaDetailsPerEnv, Map<String, Object> hashMapSchemaObj) {
    if (hashMapSchemaObj.containsKey(SCHEMA_ID) && hashMapSchemaObj.get(SCHEMA_ID) != null) {
      schemaDetailsPerEnv.setId((Integer) hashMapSchemaObj.get(SCHEMA_ID));
    }

    if (hashMapSchemaObj.containsKey(SCHEMA_COMPATIBILITY)
        && hashMapSchemaObj.get(SCHEMA_COMPATIBILITY) != null) {
      schemaDetailsPerEnv.setCompatibility(hashMapSchemaObj.get(SCHEMA_COMPATIBILITY) + "");
    } else {
      schemaDetailsPerEnv.setCompatibility("Couldn't retrieve");
    }
  }

  private SortedMap<Integer, Map<String, Object>> getSchemasMap(
      String topicNameSearch,
      int tenantId,
      KwClusters kwClusters,
      List<MessageSchema> topicSchemaVersionsInDb,
      boolean schemaUpdated,
      SortedMap<Integer, Map<String, Object>> schemaObjects)
      throws Exception {
    if (schemaUpdated) {
      log.info("GetSchema {} from DB", topicNameSearch);
      for (MessageSchema messageSchema : topicSchemaVersionsInDb) {
        Map<String, Object> schemaObj = new HashMap<>();
        schemaObj.put(SCHEMA, messageSchema.getSchemafull());
        schemaObj.put(SCHEMA_ID, messageSchema.getSchemaId());
        schemaObj.put(SCHEMA_COMPATIBILITY, messageSchema.getCompatibility());
        schemaObjects.put(Integer.parseInt(messageSchema.getSchemaversion()), schemaObj);
      }
    } else {
      schemaObjects = getSchemaFromAPI(topicNameSearch, tenantId, kwClusters);

      if (schemaObjects != null) {
        Boolean saveChanges = null;
        for (MessageSchema messageSchema : topicSchemaVersionsInDb) {

          // The Key for the SchemaObjects map is the version number.
          Map<String, Object> schemaObj =
              schemaObjects.get(Integer.valueOf(messageSchema.getSchemaversion()));
          if (schemaObj != null) {
            if (messageSchema.getSchemaId() == null) {
              saveChanges = true;
              messageSchema.setSchemaId((Integer) schemaObj.get(SCHEMA_ID));
            }
            if (messageSchema.getCompatibility() == null) {
              saveChanges = true;
              messageSchema.setCompatibility((String) schemaObj.get(SCHEMA_COMPATIBILITY));
            }
            if (messageSchema.getSchemafull() == null) {
              saveChanges = true;
              messageSchema.setSchemafull((String) schemaObj.get(SCHEMA));
            }
          }
        }
        log.debug("Updated topic {} schemaId, compatibility and schema.", topicNameSearch);
        // Save that back into the DB
        if (saveChanges != null) {
          manageDatabase.getHandleDbRequests().insertIntoMessageSchemaSOT(topicSchemaVersionsInDb);
        }
      }
    }
    return schemaObjects;
  }

  private SortedMap<Integer, Map<String, Object>> getSchemaFromAPI(
      String topicNameSearch, int tenantId, KwClusters kwClusters) throws Exception {
    SortedMap<Integer, Map<String, Object>> schemaObjects;
    log.info("GetSchema {} from API", topicNameSearch);
    schemaObjects =
        clusterApiService.getAvroSchema(
            kwClusters.getBootstrapServers(),
            kwClusters.getProtocol(),
            kwClusters.getClusterName() + kwClusters.getClusterId(),
            topicNameSearch,
            tenantId);
    return schemaObjects;
  }

  private void processSchemaPromotionDetails(
      String topicNameSearch,
      SchemaOverview schemaOverview,
      int tenantId,
      Env schemaEnv,
      List<String> kafkaEnvIds) {
    log.debug("SchemaEnv Id {} KafkaEnvIds {}", schemaEnv.getId(), kafkaEnvIds);
    PromotionStatus promotionDetails = new PromotionStatus();
    String orderEnvs = commonUtilsService.getEnvProperty(tenantId, ORDER_OF_TOPIC_ENVS);
    generatePromotionDetails(
        tenantId,
        promotionDetails,
        schemaEnv.getAssociatedEnv() != null
            ? Collections.singletonList(schemaEnv.getAssociatedEnv().getId())
            : null,
        orderEnvs);
    if (schemaOverview.getSchemaPromotionDetails() == null) {
      PromotionStatus searchOverviewPromotionDetails = new PromotionStatus();
      schemaOverview.setSchemaPromotionDetails(searchOverviewPromotionDetails);
    }

    // verify if topic exists in target env
    if (!verifyIfTopicExistsInTargetSchemaEnv(kafkaEnvIds, promotionDetails, tenantId)) {
      promotionDetails.setStatus(PromotionStatusType.NO_PROMOTION);
    } else if (isSchemaPromoteRequestOpen(
        topicNameSearch, promotionDetails.getTargetEnvId(), tenantId)) {
      promotionDetails.setStatus(PromotionStatusType.REQUEST_OPEN);
    }
    schemaOverview.setSchemaPromotionDetails(promotionDetails);
  }

  private boolean verifyIfTopicExistsInTargetSchemaEnv(
      List<String> kafkaEnvIds, PromotionStatus promotionDetails, int tenantId) {
    if (promotionDetails.getTargetEnvId() == null) {
      return false;
    }
    // Get kafka env and ensure that it has an associated env with it.
    Env promotedEnv =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(promotionDetails.getTargetEnvId(), tenantId);

    String kafkaEnvId = promotedEnv.getId();
    return kafkaEnvIds.contains(kafkaEnvId) && promotedEnv.getAssociatedEnv() != null;
  }

  private boolean isSchemaPromoteRequestOpen(String topicName, String envId, int tenantId) {
    return manageDatabase
        .getAssociatedSchemaEnvIdFromTopicId(envId, tenantId)
        .filter(
            s ->
                manageDatabase
                    .getHandleDbRequests()
                    .existsSchemaRequest(
                        topicName,
                        RequestStatus.CREATED.value,
                        RequestOperationType.CREATE.value,
                        s,
                        tenantId))
        .isPresent();
  }
}
