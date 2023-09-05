package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.error.KlawValidationException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwTenantModel;
import io.aiven.klaw.model.enums.KafkaClustersType;
import io.aiven.klaw.model.requests.EnvModel;
import io.aiven.klaw.model.requests.KwClustersModel;
import io.aiven.klaw.model.response.AclCommands;
import io.aiven.klaw.model.response.ClusterInfo;
import io.aiven.klaw.model.response.EnvIdInfo;
import io.aiven.klaw.model.response.EnvModelResponse;
import io.aiven.klaw.model.response.EnvParams;
import io.aiven.klaw.model.response.EnvUpdatedStatus;
import io.aiven.klaw.model.response.KwClustersModelResponse;
import io.aiven.klaw.model.response.KwReport;
import io.aiven.klaw.model.response.SupportedProtocolInfo;
import io.aiven.klaw.model.response.TenantInfo;
import io.aiven.klaw.service.EnvsClustersTenantsControllerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class EnvsClustersTenantsController {

  @Autowired private EnvsClustersTenantsControllerService envsClustersTenantsControllerService;

  @RequestMapping(
      value = "/getClusters",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KwClustersModelResponse>> getClusters(
      @RequestParam(value = "clusterType") String clusterType) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getClusters(clusterType), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getClustersPaginated",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KwClustersModelResponse>> getClustersPaginated(
      @RequestParam(value = "clusterType") String clusterType,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "clusterId", defaultValue = "") String clusterId,
      @RequestParam(value = "searchClusterParam", defaultValue = "") String searchClusterParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getClustersPaginated(
            clusterType, clusterId, pageNo, searchClusterParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getClusterDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<KwClustersModelResponse> getClusterDetails(
      @RequestParam(value = "clusterId") String clusterId) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getClusterDetails(clusterId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsBaseCluster",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getEnvsBaseCluster() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsForRequestTopicsCluster(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsBaseClusterFilteredForTeam",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getEnvsBaseClusterFilteredForTeam() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsForRequestTopicsClusterFiltered(),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/deleteCluster",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteCluster(@RequestParam("clusterId") String clusterId)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.deleteCluster(clusterId), HttpStatus.OK);
  }

  @PostMapping(
      value = "/addNewCluster",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> addNewCluster(
      @Valid @RequestBody KwClustersModel kwClustersModel) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addNewCluster(kwClustersModel), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getEnvs() {
    return new ResponseEntity<>(envsClustersTenantsControllerService.getKafkaEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncConnectorsEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getSyncConnectorsEnv() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getConnectorEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsPaginated",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getEnvsPaginated(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "envId", defaultValue = "") String envId,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(
            KafkaClustersType.KAFKA, envId, pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EnvModelResponse> getEnvDetails(
      @RequestParam(value = "envSelected") String envSelected,
      @RequestParam(value = "envType") String envType) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvDetails(envSelected, envType), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvIdInfo>> getSyncEnv() {
    return new ResponseEntity<>(envsClustersTenantsControllerService.getSyncEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvParams",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EnvParams> getEnvParams(
      @RequestParam(value = "envSelected") String envSelected) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvParams(envSelected), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/environments/kafka",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getKafkaEnvsPaginated(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(
            KafkaClustersType.KAFKA, "", pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/environments/kafka/{envId}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getKafkaEnv(
      @RequestParam("pageNo") String pageNo,
      @PathVariable(value = "envId") String envId,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(
            KafkaClustersType.KAFKA, envId, pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSchemaRegEnvs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getSchemaRegEnvs() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getSchemaRegEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/environments/schemaRegistry",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getSchemaRegEnvsPaginated(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(
            KafkaClustersType.SCHEMA_REGISTRY, "", pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/environments/schemaRegistry/{envId}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getSchemaRegEnv(
      @RequestParam("pageNo") String pageNo,
      @PathVariable(value = "envId") String envId,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(
            KafkaClustersType.SCHEMA_REGISTRY, envId, pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsForSchemaRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getEnvsForSchemaRequests() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsForSchemaRequests(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKafkaConnectEnvs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getKafkaConnectEnvs() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getKafkaConnectEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/environments/kafkaconnect",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getKafkaConnectEnvsPaginated(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(
            KafkaClustersType.KAFKA_CONNECT, "", pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/environments/kafkaconnect/{envId}",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModelResponse>> getKafkaConnectEnv(
      @RequestParam("pageNo") String pageNo,
      @PathVariable(value = "envId") String envId,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(
            KafkaClustersType.KAFKA_CONNECT, envId, pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/addNewEnv",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> addNewEnv(@Valid @RequestBody EnvModel newEnv)
      throws KlawException, KlawValidationException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addNewEnv(newEnv), HttpStatus.OK);
  }

  @PostMapping(
      value = "/deleteEnvironmentRequest",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteEnvironment(
      @RequestParam("envId") String envId, @RequestParam("envType") String envType)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.deleteEnvironment(envId, envType), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getStandardEnvNames",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getStandardEnvNames() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getStandardEnvNames(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getExtensionPeriods",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getExtensionPeriods() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getExtensionPeriods(), HttpStatus.OK);
  }

  @PostMapping(
      value = "/addTenantId",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> addTenantId(@Valid @RequestBody KwTenantModel kwTenantModel)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addTenantId(kwTenantModel, true), HttpStatus.OK);
  }

  @PostMapping(
      value = "/deleteTenant",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> deleteTenant() throws KlawException {
    return new ResponseEntity<>(envsClustersTenantsControllerService.deleteTenant(), HttpStatus.OK);
  }

  // Pattern a-zA-z and/or spaces.
  @PostMapping(
      value = "/udpateTenant",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> udpateTenant(
      @RequestParam("orgName") @Pattern(message = "Invalid Organization.", regexp = "^[a-zA-z ]*$")
          String orgName)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.updateTenant(orgName), HttpStatus.OK);
  }

  @PostMapping(
      value = "/udpateTenantExtension",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> udpateTenantExtension(
      @RequestParam("selectedTenantExtensionPeriod") String selectedTenantExtensionPeriod) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.udpateTenantExtension(selectedTenantExtensionPeriod),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTenants",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KwTenantModel>> getTenants() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getAllTenants(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getMyTenantInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<KwTenantModel> getMyTenantInfo() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getMyTenantInfo(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAclCommands",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<AclCommands> getAclCommand() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getAclCommands(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKwPubkey",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<KwReport> getKwPubkey() {
    return new ResponseEntity<>(envsClustersTenantsControllerService.getPublicKey(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getUpdateEnvStatus",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EnvUpdatedStatus> getUpdateEnvStatus(
      @RequestParam(value = "envId") String envId) throws KlawBadRequestException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getUpdateEnvStatus(envId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTenantsInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TenantInfo> getTenantsInfo() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getTenantsInfo(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getClusterInfoFromEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ClusterInfo> getClusterInfoFromEnv(
      @RequestParam(value = "envSelected") String envSelected,
      @RequestParam(value = "envType") String envType) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getClusterInfoFromEnv(envSelected, envType),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKafkaProtocols",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<SupportedProtocolInfo>> getSupportedKafkaProtocols() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getSupportedKafkaProtocols(), HttpStatus.OK);
  }
}
