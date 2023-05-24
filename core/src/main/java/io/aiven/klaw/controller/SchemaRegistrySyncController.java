package io.aiven.klaw.controller;

import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncSchemaUpdates;
import io.aiven.klaw.model.response.SchemaDetailsResponse;
import io.aiven.klaw.model.response.SyncSchemasList;
import io.aiven.klaw.service.SchemaRegistrySyncControllerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Slf4j
public class SchemaRegistrySyncController {

  @Autowired SchemaRegistrySyncControllerService schemaRegistrySyncControllerService;

  /**
   * Retrieve schema subjects and its versions in a given kafka env (associated schema env)
   *
   * @param kafkaEnvId kafka env
   * @return
   * @throws Exception
   */
  @RequestMapping(
      value = "/schemas",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<SyncSchemasList> getSchemasOfEnvironment(
      @RequestParam(value = "envId") String kafkaEnvId,
      @RequestParam(value = "pageNo") String pageNo,
      @RequestParam(value = "showAllTopics", defaultValue = "false") boolean showAllTopics,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch)
      throws Exception {
    return new ResponseEntity<>(
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            kafkaEnvId, pageNo, currentPage, topicNameSearch, showAllTopics),
        HttpStatus.OK);
  }

  /**
   * Store schemas of all versions in metadata db
   *
   * @param syncSchemaUpdates topic list and environment
   * @return
   * @throws Exception
   */
  @PostMapping(
      value = "/schemas",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateSyncSchemas(
      @RequestBody SyncSchemaUpdates syncSchemaUpdates) throws Exception {
    return new ResponseEntity<>(
        schemaRegistrySyncControllerService.updateDbFromCluster(syncSchemaUpdates), HttpStatus.OK);
  }

  /**
   * Retrieve schema of a topic and specific version
   *
   * @param topicName
   * @param schemaVersion
   * @param kafkaEnvId kafka environment id
   * @return
   * @throws Exception
   */
  @RequestMapping(
      value = "/schemas/kafkaEnv/{kafkaEnvId}/topic/{topicName}/schemaVersion/{schemaVersion}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<SchemaDetailsResponse> getSchemaOfTopicFromCluster(
      @PathVariable(value = "topicName") String topicName,
      @PathVariable(value = "schemaVersion") int schemaVersion,
      @PathVariable(value = "kafkaEnvId") String kafkaEnvId)
      throws Exception {
    return new ResponseEntity<>(
        schemaRegistrySyncControllerService.getSchemaOfTopicFromCluster(
            topicName, schemaVersion, kafkaEnvId),
        HttpStatus.OK);
  }
}
