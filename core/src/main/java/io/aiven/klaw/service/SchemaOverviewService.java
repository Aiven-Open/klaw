package io.aiven.klaw.service;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.EnvTag;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.model.enums.KafkaClustersType;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SchemaOverviewService extends BaseOverviewService {

  public SchemaOverviewService(MailUtils mailService) {
    super(mailService);
  }

  public SchemaOverview getSchemaOfTopic(
      String topicNameSearch, int schemaVersionSearch, List<String> kafkaEnvIds) {
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

  private void updateAvroSchema(
      String topicNameSearch,
      int schemaVersionSearch,
      List<String> kafkaEnvIds,
      boolean retrieveSchemas,
      SchemaOverview schemaOverview,
      String userName,
      int tenantId) {
    if (schemaOverview.isTopicExists() && retrieveSchemas) {
      List<SchemaDetailsPerEnv> schemaDetails = new ArrayList<>();
      schemaOverview.setSchemaDetails(schemaDetails);
      Map<String, List<Integer>> schemaVersions = new HashMap<>();
      schemaOverview.setAllSchemaVersions(schemaVersions);
      schemaOverview.setLatestVersion(new HashMap<>());

      List<Env> schemaEnvs = new ArrayList<>();
      // Get first base kafka env
      Env kafkaEnv =
          manageDatabase.getHandleDbRequests().getEnvDetails(kafkaEnvIds.get(0), tenantId);
      EnvTag associatedSchemaEnv = kafkaEnv.getAssociatedEnv();
      if (associatedSchemaEnv != null) {
        Env schemaEnv =
            manageDatabase
                .getHandleDbRequests()
                .getEnvDetails(associatedSchemaEnv.getId(), tenantId);
        schemaEnvs.add(schemaEnv);
      }

      List<Topic> topics =
          manageDatabase
              .getHandleDbRequests()
              .getAllTopicsByTopicNameAndTeamIdAndTenantId(
                  topicNameSearch, commonUtilsService.getTeamId(userName), tenantId);

      Object dynamicObj;
      Map<String, Object> hashMapSchemaObj;
      String schemaOfObj;

      for (Env schemaEnv : schemaEnvs) {
        try {
          log.debug("UpdateAvroSchema - Process env {}", schemaEnv);
          SchemaDetailsPerEnv schemaDetailsPerEnv = new SchemaDetailsPerEnv();

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
              if (latestSchemaVersion == schemaVersionSearch) {
                schemaVersionSearch = 0;
              }
            } catch (NumberFormatException ignored) {
            }

            // get latest version
            if (schemaVersionSearch == 0) {
              hashMapSchemaObj = schemaObjects.get(latestSchemaVersion);
              schemaOfObj = (String) hashMapSchemaObj.get("schema");
              schemaDetailsPerEnv.setLatest(true);
              schemaDetailsPerEnv.setId((Integer) hashMapSchemaObj.get("id"));
              schemaDetailsPerEnv.setCompatibility(hashMapSchemaObj.get("compatibility") + "");
              schemaDetailsPerEnv.setVersion(latestSchemaVersion);

              if (schemaObjects.size() > 1) {
                schemaDetailsPerEnv.setShowNext(true);
                schemaDetailsPerEnv.setShowPrev(false);
                int indexOfVersion = allVersionsList.indexOf(latestSchemaVersion);
                schemaDetailsPerEnv.setNextVersion(allVersionsList.get(indexOfVersion + 1));
              }
            } else {
              hashMapSchemaObj = schemaObjects.get(schemaVersionSearch);
              schemaOfObj = (String) hashMapSchemaObj.get("schema");
              schemaDetailsPerEnv.setLatest(false);
              schemaDetailsPerEnv.setId((Integer) hashMapSchemaObj.get("id"));
              schemaDetailsPerEnv.setCompatibility(hashMapSchemaObj.get("compatibility") + "");
              schemaDetailsPerEnv.setVersion(schemaVersionSearch);

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

            schemaDetails.add(schemaDetailsPerEnv);
            schemaOverview.setSchemaExists(true);
            // A team owns a topic across all environments so we can assume if the search returned
            // one or more topics it is owned by this users team.
            if (topics.size() > 0) {

              // Set Promotion Details
              processSchemaPromotionDetails(
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
      }

      if (schemaOverview.isSchemaExists()) {
        log.debug("SchemaDetails {}", schemaDetails);
        schemaOverview.setSchemaDetails(schemaDetails);
      }
    }
  }

  private void processSchemaPromotionDetails(
      SchemaOverview schemaOverview, int tenantId, Env schemaEnv, List<String> kafkaEnvIds) {
    log.debug("SchemaEnv Id {} KafkaEnvIds {}", schemaEnv.getId(), kafkaEnvIds);
    PromotionStatus promotionDetails = new PromotionStatus();
    generatePromotionDetails(
        tenantId,
        promotionDetails,
        Collections.singletonList(schemaEnv.getId()),
        commonUtilsService.getSchemaPromotionEnvsFromKafkaEnvs(tenantId));
    if (schemaOverview.getSchemaPromotionDetails() == null) {
      Map<String, PromotionStatus> searchOverviewPromotionDetails = new HashMap<>();
      schemaOverview.setSchemaPromotionDetails(searchOverviewPromotionDetails);
    }
    Map<String, PromotionStatus> existingPromoDetails = schemaOverview.getSchemaPromotionDetails();
    existingPromoDetails.put(schemaEnv.getName(), promotionDetails);
    // verify if topic exists in target env
    if (!verifyIfTopicExistsInTargetSchemaEnv(kafkaEnvIds, promotionDetails, tenantId)) {
      promotionDetails.setStatus(NO_PROMOTION);
    }
    schemaOverview.setSchemaPromotionDetails(existingPromoDetails);
  }

  private boolean verifyIfTopicExistsInTargetSchemaEnv(
      List<String> kafkaEnvIds, PromotionStatus promotionDetails, int tenantId) {
    if (promotionDetails.getTargetEnvId() == null) {
      return false;
    }
    // get kafka env of target schema env
    String kafkaEnvId =
        manageDatabase
            .getHandleDbRequests()
            .getEnvDetails(promotionDetails.getTargetEnvId(), tenantId)
            .getAssociatedEnv()
            .getId();
    return kafkaEnvIds.contains(kafkaEnvId);
  }
}
