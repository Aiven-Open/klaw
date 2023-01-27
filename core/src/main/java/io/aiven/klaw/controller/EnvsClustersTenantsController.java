package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.EnvModel;
import io.aiven.klaw.model.KwClustersModel;
import io.aiven.klaw.model.KwTenantModel;
import io.aiven.klaw.service.EnvsClustersTenantsControllerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class EnvsClustersTenantsController {

  @Autowired private EnvsClustersTenantsControllerService envsClustersTenantsControllerService;

  @RequestMapping(
      value = "/getClusters",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KwClustersModel>> getClusters(
      @RequestParam(value = "clusterType") String clusterType) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getClusters(clusterType), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getClustersPaginated",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KwClustersModel>> getClustersPaginated(
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
  public ResponseEntity<KwClustersModel> getClusterDetails(
      @RequestParam(value = "clusterId") String clusterId) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getClusterDetails(clusterId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsBaseCluster",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getEnvsBaseCluster() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsForRequestTopicsCluster(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsBaseClusterFilteredForTeam",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getEnvsBaseClusterFilteredForTeam() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsForRequestTopicsClusterFiltered(),
        HttpStatus.OK);
  }

  @PostMapping(value = "/deleteCluster")
  public ResponseEntity<ApiResponse> deleteCluster(@RequestParam("clusterId") String clusterId)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.deleteCluster(clusterId), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewCluster")
  public ResponseEntity<ApiResponse> addNewCluster(
      @Valid @RequestBody KwClustersModel kwClustersModel) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addNewCluster(kwClustersModel), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getEnvs() {
    return new ResponseEntity<>(envsClustersTenantsControllerService.getKafkaEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncConnectorsEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getSyncConnectorsEnv() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getConnectorEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsPaginated",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getEnvsPaginated(
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "envId", defaultValue = "") String envId,
      @RequestParam(value = "searchEnvParam", defaultValue = "") String searchEnvParam) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsPaginated(envId, pageNo, searchEnvParam),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvDetails",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<EnvModel> getEnvDetails(
      @RequestParam(value = "envSelected") String envSelected,
      @RequestParam(value = "envType") String envType) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvDetails(envSelected, envType), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<Map<String, String>>> getSyncEnv() {
    return new ResponseEntity<>(envsClustersTenantsControllerService.getSyncEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvParams",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, List<String>>> getEnvParams(
      @RequestParam(value = "envSelected") String envSelected) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvParams(envSelected), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSchemaRegEnvs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getSchemaRegEnvs() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getSchemaRegEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getEnvsForSchemaRequests",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getRequestForSchemas() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getEnvsForSchemaRequests(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKafkaConnectEnvs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getKafkaConnectEnvs() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getKafkaConnectEnvs(), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewEnv")
  public ResponseEntity<ApiResponse> addNewEnv(@Valid @RequestBody EnvModel newEnv)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addNewEnv(newEnv), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteEnvironmentRequest")
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

  @PostMapping(value = "/addTenantId")
  public ResponseEntity<ApiResponse> addTenantId(@Valid @RequestBody KwTenantModel kwTenantModel)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addTenantId(kwTenantModel, true), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteTenant")
  public ResponseEntity<ApiResponse> deleteTenant() throws KlawException {
    return new ResponseEntity<>(envsClustersTenantsControllerService.deleteTenant(), HttpStatus.OK);
  }

  // Pattern a-zA-z and/or spaces.
  @PostMapping(value = "/udpateTenant")
  public ResponseEntity<ApiResponse> udpateTenant(
      @RequestParam("orgName") @Pattern(message = "Invalid Organization.", regexp = "^[a-zA-z ]*$")
          String orgName)
      throws KlawException {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.updateTenant(orgName), HttpStatus.OK);
  }

  @PostMapping(value = "/udpateTenantExtension")
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
  public ResponseEntity<Map<String, String>> getAclCommand() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getAclCommands(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKwPubkey",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getKwPubkey() {

    Map<String, String> fileMap = envsClustersTenantsControllerService.getPublicKey();
    return new ResponseEntity<>(fileMap, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getUpdateEnvStatus",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getUpdateEnvStatus(
      @RequestParam(value = "envId") String envId) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getUpdateEnvStatus(envId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTenantsInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, Integer>> getTenantsInfo() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getTenantsInfo(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getClusterInfoFromEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> getClusterInfoFromEnv(
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
  public ResponseEntity<List<Map<String, String>>> getSupportedKafkaProtocols() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getSupportedKafkaProtocols(), HttpStatus.OK);
  }
}
