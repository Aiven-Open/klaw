package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.model.EnvModel;
import com.kafkamgt.uiapi.model.KwClustersModel;
import com.kafkamgt.uiapi.model.KwTenantModel;
import com.kafkamgt.uiapi.service.EnvsClustersTenantsControllerService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
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
  public ResponseEntity<String> deleteCluster(@RequestParam("clusterId") String clusterId) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.deleteCluster(clusterId), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewCluster")
  public ResponseEntity<HashMap<String, String>> addNewCluster(
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
  public ResponseEntity<List<HashMap<String, String>>> getSyncEnv() {
    return new ResponseEntity<>(envsClustersTenantsControllerService.getSyncEnvs(), HttpStatus.OK);
  }

  //    @RequestMapping(value = "/getEnvsStatus", method = RequestMethod.GET, produces =
  // {MediaType.APPLICATION_JSON_VALUE})
  //    public ResponseEntity<HashMap<String, List<EnvModel>>> getEnvsStatus() {
  //        return new ResponseEntity<>(envsClustersTenantsControllerService.getEnvsStatus(),
  // HttpStatus.OK);
  //    }

  @RequestMapping(
      value = "/getEnvParams",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, List<String>>> getEnvParams(
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
      value = "/getKafkaConnectEnvs",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getKafkaConnectEnvs() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getKafkaConnectEnvs(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSchemaRegEnvsStatus",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<EnvModel>> getSchemaRegEnvsStatus() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getSchemaRegEnvsStatus(), HttpStatus.OK);
  }

  @PostMapping(value = "/addNewEnv")
  public ResponseEntity<String> addNewEnv(@Valid @RequestBody EnvModel newEnv) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addNewEnv(newEnv), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteEnvironmentRequest")
  public ResponseEntity<HashMap<String, String>> deleteEnvironment(
      @RequestParam("envId") String envId, @RequestParam("envType") String envType) {
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
  public ResponseEntity<HashMap<String, String>> addTenantId(
      @Valid @RequestBody KwTenantModel kwTenantModel) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.addTenantId(kwTenantModel, true), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteTenant")
  public ResponseEntity<HashMap<String, String>> deleteTenant() {
    return new ResponseEntity<>(envsClustersTenantsControllerService.deleteTenant(), HttpStatus.OK);
  }

  // Pattern a-zA-z and/or spaces.
  @PostMapping(value = "/udpateTenant")
  public ResponseEntity<HashMap<String, String>> udpateTenant(
      @RequestParam("orgName") @Pattern(message = "Invalid Organization.", regexp = "^[a-zA-z ]*$")
          String orgName) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.updateTenant(orgName), HttpStatus.OK);
  }

  @PostMapping(value = "/udpateTenantExtension")
  public ResponseEntity<HashMap<String, String>> udpateTenantExtension(
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
  public ResponseEntity<HashMap<String, String>> getAclCommand() {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getAclCommands(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKwPubkey",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getKwPubkey() {

    HashMap<String, String> fileMap = envsClustersTenantsControllerService.getPublicKey();
    return new ResponseEntity<>(fileMap, HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getUpdateEnvStatus",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getUpdateEnvStatus(
      @RequestParam(value = "envId") String envId) {
    return new ResponseEntity<>(
        envsClustersTenantsControllerService.getUpdateEnvStatus(envId), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTenantsInfo",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, Integer>> getTenantsInfo() {
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
}
