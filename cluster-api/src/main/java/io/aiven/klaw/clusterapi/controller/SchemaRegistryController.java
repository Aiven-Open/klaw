package io.aiven.klaw.clusterapi.controller;

import io.aiven.klaw.clusterapi.models.ApiResponse;
import io.aiven.klaw.clusterapi.models.ClusterSchemaCacheResetRequest;
import io.aiven.klaw.clusterapi.models.ClusterSchemaRequest;
import io.aiven.klaw.clusterapi.models.ClusterTopicRequest;
import io.aiven.klaw.clusterapi.models.SchemasInfoOfClusterResponse;
import io.aiven.klaw.clusterapi.models.enums.KafkaSupportedProtocol;
import io.aiven.klaw.clusterapi.models.enums.SchemaCacheUpdateType;
import io.aiven.klaw.clusterapi.services.SchemaService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/topics")
@Slf4j
@AllArgsConstructor
public class SchemaRegistryController {

  SchemaService schemaService;

  @RequestMapping(
      value = "/getSchema/{bootstrapServers}/{protocol}/{clusterIdentification}/{topicName}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<Integer, Map<String, Object>>> getSchema(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String topicName,
      @PathVariable String clusterIdentification) {
    Map<Integer, Map<String, Object>> schema =
        schemaService.getSchema(bootstrapServers, protocol, clusterIdentification, topicName);
    return new ResponseEntity<>(schema, HttpStatus.OK);
  }

  /**
   * Return list of all available subjects (-value only), and schema versions on each subject, from
   * the schema registry cluster
   *
   * @param bootstrapServers schema host
   * @param protocol schema registry host protocol
   * @param clusterIdentification cluster id from cluster page
   * @return
   */
  @RequestMapping(
      value =
          "/schemas/bootstrapServers/{bootstrapServers}/protocol/{protocol}/clusterIdentification/{clusterIdentification}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<SchemasInfoOfClusterResponse> getSchemaInfoOfCluster(
      @PathVariable String bootstrapServers,
      @Valid @PathVariable KafkaSupportedProtocol protocol,
      @PathVariable String clusterIdentification) {
    return new ResponseEntity<>(
        schemaService.loadAllSchemasInfoFromCluster(
            bootstrapServers,
            protocol,
            clusterIdentification,
            false,
            SchemaCacheUpdateType.NONE,
            null),
        HttpStatus.OK);
  }

  /**
   * Register a schema on schema registry. If force register is enabled - Get subject compatibility
   * - Set subject compatibility to NONE, if it's not NONE or NOT SET - Register schema - If subject
   * compatibility is NOT_SET, get global compatibility - Apply global compatibility on subject
   * level If force register is not enabled - Register schema
   */
  @PostMapping(
      value = "/postSchema",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> postSchema(
      @RequestBody @Valid ClusterSchemaRequest clusterSchemaRequest) {
    try {
      return new ResponseEntity<>(
          schemaService.registerSchema(clusterSchemaRequest), HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  /** Delete the subject, all versions of schema on schema registry. */
  @PostMapping(
      value = "/schema/delete",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteSchema(
      @RequestBody @Valid ClusterTopicRequest clusterTopicRequest) {
    try {
      return new ResponseEntity<>(schemaService.deleteSchema(clusterTopicRequest), HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  /** Schema cache reset */
  @PostMapping(
      value = "/schema/resetCache",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> resetCache(
      @RequestBody @Valid ClusterSchemaCacheResetRequest clusterSchemaCacheResetRequest) {
    try {
      return new ResponseEntity<>(
          schemaService.resetCache(clusterSchemaCacheResetRequest), HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  @PostMapping(
      value = "/schema/validate/compatibility",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> schemaCompatibilityValidation(
      @RequestBody @Valid ClusterSchemaRequest clusterSchemaRequest) {
    try {
      return new ResponseEntity<>(
          schemaService.checkSchemaCompatibility(
              clusterSchemaRequest.getFullSchema(),
              clusterSchemaRequest.getTopicName(),
              clusterSchemaRequest.getProtocol(),
              clusterSchemaRequest.getEnv(),
              clusterSchemaRequest.getClusterIdentification(),
              clusterSchemaRequest.getSchemaType()),
          HttpStatus.OK);
    } catch (Exception e) {
      return handleException(e);
    }
  }

  private static ResponseEntity<ApiResponse> handleException(Exception e) {
    log.error("Exception:", e);
    return new ResponseEntity<>(
        ApiResponse.builder().success(false).message(e.getMessage()).build(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
