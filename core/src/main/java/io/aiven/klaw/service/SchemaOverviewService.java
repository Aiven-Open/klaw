package io.aiven.klaw.service;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.model.SchemaOverview;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchemaOverviewService extends BaseOverviewService {

  public SchemaOverviewService(MailUtils mailService) {
    super(mailService);
  }

  public SchemaOverview getSchemaOfTopic(
      String topicNameSearch, String schemaVersionSearch, List<String> kafkaEnvIds) {
    String userName = getUserName();
    int tenantId = commonUtilsService.getTenantId(userName);

    SchemaOverview schemaOverview = new SchemaOverview();
    schemaOverview.setTopicExists(true);
    boolean retrieveSchemas = true;
    updateAvroSchema(
        topicNameSearch,
        schemaVersionSearch,
        kafkaEnvIds,
        retrieveSchemas,
        schemaOverview,
        userName,
        tenantId);
    return schemaOverview;
  }

  private boolean requestorOwnsTopic(
      String topicNameSearch, String kafkaEnv, String userName, int tenantId) {
    List<Topic> topic = manageDatabase.getHandleDbRequests().getTopics(topicNameSearch, tenantId);
    log.debug("topics : {} for kafkaEnv: {}", topic, kafkaEnv);
    topic = topic.stream().filter(t -> t.getEnvironment().equals(kafkaEnv)).toList();
    
    if (topic != null && topic.size() == 1) {
      Integer teamId = topic.get(0).getTeamId();
      return teamId.equals(commonUtilsService.getTeamId(userName));
    }
    // If topic isn't found or multiples found return false.
    return false;
  }

  private void updateAvroSchema(
      String topicNameSearch,
      String schemaVersionSearch,
      List<String> kafkaEnvIds,
      boolean retrieveSchemas,
      SchemaOverview schemaOverview,
      String userName,
      int tenantId) {
    if (schemaOverview.isTopicExists() && retrieveSchemas) {
      List<Map<String, String>> schemaDetails = new ArrayList<>();
      schemaOverview.setSchemaDetails(schemaDetails);
      Map<String, List<Integer>> schemaVersions = new HashMap<>();
      schemaOverview.setAllSchemaVersions(schemaVersions);
      schemaOverview.setLatestVersion(new HashMap<>());

      List<Env> schemaEnvs = new ArrayList<>();
      // Get first base kafka env
      Env kafkaEnv =
          manageDatabase.getHandleDbRequests().selectEnvDetails(kafkaEnvIds.get(0), tenantId);
      EnvTag associatedSchemaEnv = kafkaEnv.getAssociatedEnv();
      if (associatedSchemaEnv != null) {
        Env schemaEnv =
            manageDatabase
                .getHandleDbRequests()
                .selectEnvDetails(associatedSchemaEnv.getId(), tenantId);
        schemaEnvs.add(schemaEnv);
      }

      Object dynamicObj;
      Map<String, Object> hashMapSchemaObj;
      String schemaOfObj;

      for (Env schemaEnv : schemaEnvs) {
        try {
          log.debug("UpdateAvroSchema - Process env {}", schemaEnv);
          Map<String, String> schemaMap = new HashMap<>();
          KwClusters kwClusters =
              manageDatabase
                  .getClusters(KafkaClustersType.SCHEMA_REGISTRY, tenantId)
                  .get(schemaEnv.getClusterId());

          SortedMap<Integer, Map<String, Object>> schemaObjects =
              clusterApiService.getAvroSchema(
                  kwClusters.getBootstrapServers(),
                  kwClusters.getProtocol(),
                  kwClusters.getClusterName() + kwClusters.getClusterId(),
                  topicNameSearch,
                  tenantId);
          // If the schemaObject is null ie does not exist do not try to manipulate it.
          if (schemaObjects != null && !schemaObjects.isEmpty()) {

            Integer latestSchemaVersion = schemaObjects.firstKey();
            schemaOverview.getLatestVersion().put(schemaEnv.getName(), latestSchemaVersion);
            Set<Integer> allVersions = schemaObjects.keySet();
            List<Integer> allVersionsList = new ArrayList<>(allVersions);
            schemaOverview.getAllSchemaVersions().put(schemaEnv.getName(), allVersionsList);
            try {
              if (schemaVersionSearch != null
                  && latestSchemaVersion == Integer.parseInt(schemaVersionSearch)) {
                schemaVersionSearch = "";
              }
            } catch (NumberFormatException ignored) {
            }

            // get latest version
            if (schemaVersionSearch != null && schemaVersionSearch.equals("")) {
              hashMapSchemaObj = schemaObjects.get(latestSchemaVersion);
              schemaOfObj = (String) hashMapSchemaObj.get("schema");
              schemaMap.put("isLatest", "true");
              schemaMap.put("id", hashMapSchemaObj.get("id") + "");
              schemaMap.put("compatibility", hashMapSchemaObj.get("compatibility") + "");
              schemaMap.put("version", "" + latestSchemaVersion);

              if (schemaObjects.size() > 1) {
                schemaMap.put("showNext", "true");
                schemaMap.put("showPrev", "false");
                int indexOfVersion = allVersionsList.indexOf(latestSchemaVersion);
                schemaMap.put("nextVersion", "" + allVersionsList.get(indexOfVersion + 1));
              }
            } else {
              hashMapSchemaObj =
                  schemaObjects.get(Integer.parseInt(Objects.requireNonNull(schemaVersionSearch)));
              schemaOfObj = (String) hashMapSchemaObj.get("schema");
              schemaMap.put("isLatest", "false");
              schemaMap.put("id", hashMapSchemaObj.get("id") + "");
              schemaMap.put("compatibility", hashMapSchemaObj.get("compatibility") + "");
              schemaMap.put("version", "" + schemaVersionSearch);

              if (schemaObjects.size() > 1) {
                int indexOfVersion = allVersionsList.indexOf(Integer.parseInt(schemaVersionSearch));
                if (indexOfVersion + 1 == allVersionsList.size()) {
                  schemaMap.put("showNext", "false");
                  schemaMap.put("showPrev", "true");
                  schemaMap.put("prevVersion", "" + allVersionsList.get(indexOfVersion - 1));
                } else {
                  schemaMap.put("showNext", "true");
                  schemaMap.put("showPrev", "true");
                  schemaMap.put("prevVersion", "" + allVersionsList.get(indexOfVersion - 1));
                  schemaMap.put("nextVersion", "" + allVersionsList.get(indexOfVersion + 1));
                }
              }
            }

            schemaMap.put("env", schemaEnv.getName());
            dynamicObj = OBJECT_MAPPER.readValue(schemaOfObj, Object.class);
            schemaOfObj = WRITER_WITH_DEFAULT_PRETTY_PRINTER.writeValueAsString(dynamicObj);
            schemaMap.put("content", schemaOfObj);

            schemaDetails.add(schemaMap);
            schemaOverview.setSchemaExists(true);
            if (requestorOwnsTopic(
                topicNameSearch, schemaEnv.getAssociatedEnv().getId(), userName, tenantId)) {
              // Set Promotion Details
              processSchemaPromotionDetails(schemaOverview, tenantId, schemaEnv, kafkaEnvIds);
              log.info("Getting schema details for: " + topicNameSearch);
            }
          }
        } catch (Exception e) {
          log.error("Error ", e);
        }
      }

      if (schemaOverview.isSchemaExists()) {
        log.debug("SchemaDetails {}", schemaDetails);
        schemaOverview.setSchemaDetails(schemaDetails);
      }
    }
  }

  private void processSchemaPromotionDetails(
      SchemaOverview schemaOverview, int tenantId, Env schemaEnv, List<String> kafkaEnvIds) {
    log.info("SchemaEnv Id {}", schemaEnv.getId());
    Map<String, String> promotionDetails = new HashMap<>();
    generatePromotionDetails(
        tenantId,
        promotionDetails,
        Collections.singletonList(schemaEnv.getId()),
        commonUtilsService.getSchemaPromotionEnvsFromKafkaEnvs(tenantId));
    if (schemaOverview.getSchemaPromotionDetails() == null) {
      Map<String, Map<String, String>> searchOverviewPromotionDetails = new HashMap<>();
      schemaOverview.setSchemaPromotionDetails(searchOverviewPromotionDetails);
    }
    Map<String, Map<String, String>> existingPromoDetails =
        schemaOverview.getSchemaPromotionDetails();
    existingPromoDetails.put(schemaEnv.getName(), promotionDetails);
    // verify if topic exists in target env
    if (!verifyIfTopicExistsInTargetSchemaEnv(kafkaEnvIds, promotionDetails, tenantId)) {
      promotionDetails.put("status", "NO_PROMOTION");
    }
    schemaOverview.setSchemaPromotionDetails(existingPromoDetails);
  }

  private boolean verifyIfTopicExistsInTargetSchemaEnv(
      List<String> kafkaEnvIds, Map<String, String> promotionDetails, int tenantId) {
    if (!promotionDetails.containsKey("targetEnvId")) {
      return false;
    }
    // get kafka env of target schema env
    String kafkaEnvId =
        manageDatabase
            .getHandleDbRequests()
            .selectEnvDetails(promotionDetails.get("targetEnvId"), tenantId)
            .getAssociatedEnv()
            .getId();
    return kafkaEnvIds.contains(kafkaEnvId);
  }
}
