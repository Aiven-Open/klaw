package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.KwPropertiesModel;
import io.aiven.klaw.model.ServerConfigProperties;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.ConnectivityStatus;
import io.aiven.klaw.model.response.KwPropertiesResponse;
import io.aiven.klaw.service.ServerConfigService;
import io.aiven.klaw.validation.PermissionAllowed;
import java.util.Collection;
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
public class ServerConfigController {

  @Autowired private ServerConfigService serverConfigService;

  @PermissionAllowed(permissionAllowed = {PermissionType.UPDATE_SERVERCONFIG})
  @RequestMapping(
      value = "/getAllServerConfig",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Collection<ServerConfigProperties>> getAllProperties() {
    return new ResponseEntity<>(serverConfigService.getAllProps(), HttpStatus.OK);
  }

  @PermissionAllowed(permissionAllowed = {PermissionType.UPDATE_SERVERCONFIG})
  @RequestMapping(
      value = "/getAllServerEditableConfig",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<KwPropertiesResponse>> getAllEditableProps() {
    return new ResponseEntity<>(serverConfigService.getAllEditableProps(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/resetCache",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Map<String, String>> resetCache() {
    return new ResponseEntity<>(serverConfigService.resetCache(), HttpStatus.OK);
  }

  @PermissionAllowed(permissionAllowed = {PermissionType.UPDATE_SERVERCONFIG})
  @PostMapping(
      value = "/updateKwCustomProperty",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateKwCustomProperty(
      @RequestBody KwPropertiesModel kwPropertiesModel) throws KlawException {
    return new ResponseEntity<>(
        serverConfigService.updateKwCustomProperty(kwPropertiesModel), HttpStatus.OK);
  }

  // The permission update_servcerconfig is required to gain access to this feature and so it is
  // also required to be protected from miss use.
  @PermissionAllowed(permissionAllowed = {PermissionType.UPDATE_SERVERCONFIG})
  @RequestMapping(
      value = "/testClusterApiConnection",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ConnectivityStatus> testClusterApiConnection(
      @RequestParam("clusterApiUrl") String clusterApiUrl) throws KlawException {
    return new ResponseEntity<>(
        serverConfigService.testClusterApiConnection(clusterApiUrl), HttpStatus.OK);
  }
}
