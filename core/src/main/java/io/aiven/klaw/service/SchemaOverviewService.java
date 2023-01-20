package io.aiven.klaw.service;

import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.KwClusters;
import io.aiven.klaw.helpers.HandleDbRequests;
import io.aiven.klaw.model.SchemaOverview;
import io.aiven.klaw.model.enums.KafkaClustersType;
import java.util.ArrayList;
import java.util.Arrays;
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

  public SchemaOverview getSchemaOfTopic(String topicNameSearch, String schemaVersionSearch) {
    HandleDbRequests handleDb = manageDatabase.getHandleDbRequests();
    int tenantId = commonUtilsService.getTenantId(getUserName());
    SchemaOverview schemaOverview = new SchemaOverview();
    schemaOverview.setTopicExists(true);
    boolean retrieveSchemas = true;
    updateAvroSchema(
        topicNameSearch, schemaVersionSearch, handleDb, retrieveSchemas, schemaOverview, tenantId);
    return schemaOverview;
  }

  private void updateAvroSchema(
      String topicNameSearch,
      String schemaVersionSearch,
      HandleDbRequests handleDb,
      boolean retrieveSchemas,
      SchemaOverview schemaOverview,
      int tenantId) {
    if (schemaOverview.isTopicExists() && retrieveSchemas) {
      List<Map<String, String>> schemaDetails = new ArrayList<>();
      schemaOverview.setSchemaDetails(schemaDetails);
      Map<String, List<Integer>> schemaVersions = new HashMap<>();
      schemaOverview.setAllSchemaVersions(schemaVersions);
      schemaOverview.setLatestVersion(new HashMap<>());
      List<Env> schemaEnvs = handleDb.selectAllSchemaRegEnvs(tenantId);
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
            // Set Promotion Details
            processSchemaPromotionDetails(schemaOverview, tenantId, schemaEnv);
            log.info("Getting schema details for: " + topicNameSearch);
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
      SchemaOverview schemaOverview, int tenantId, Env schemaEnv) {
    log.info("SchemaEnv Id {}", schemaEnv.getId());
    Map<String, String> promotionDetails = new HashMap<>();
    generatePromotionDetails(
        tenantId,
        promotionDetails,
        Arrays.asList(schemaEnv.getId()),
        mailService.getEnvProperty(tenantId, "ORDER_OF_SCHEMA_ENVS"));
    if (schemaOverview.getSchemaPromotionDetails() == null) {
      Map<String, Map<String, String>> searchOverviewPromotionDetails = new HashMap<>();
      schemaOverview.setSchemaPromotionDetails(searchOverviewPromotionDetails);
    }
    Map<String, Map<String, String>> existingPromoDetails =
        schemaOverview.getSchemaPromotionDetails();
    existingPromoDetails.put(schemaEnv.getName(), promotionDetails);
    schemaOverview.setSchemaPromotionDetails(existingPromoDetails);
  }
}
