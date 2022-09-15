package com.kafkamgt.uiapi.controller;

import com.kafkamgt.uiapi.model.KwRolesPermissionsModel;
import com.kafkamgt.uiapi.service.RolesPermissionsControllerService;
import java.util.HashMap;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class RolesPermissionsController {

  @Autowired private RolesPermissionsControllerService rolesPermissionsControllerService;

  @RequestMapping(
      value = "/getRoles",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getRoles() {
    return new ResponseEntity<>(rolesPermissionsControllerService.getRoles(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getRolesFromDb",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<String>> getRolesFromDb() {
    return new ResponseEntity<>(rolesPermissionsControllerService.getRolesFromDb(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getPermissionDescriptions",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getPermissionDescriptions() {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.getPermissionDescriptions(), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getPermissions",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, List<HashMap<String, Boolean>>>> getPermissions() {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.getPermissions(true), HttpStatus.OK);
  }

  @PostMapping(value = "/deleteRole")
  public ResponseEntity<HashMap<String, String>> deleteRole(@RequestParam("roleId") String roleId) {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.deleteRole(roleId), HttpStatus.OK);
  }

  @PostMapping(value = "/addRoleId")
  public ResponseEntity<HashMap<String, String>> addRoleId(@RequestParam("roleId") String roleId) {
    return new ResponseEntity<>(rolesPermissionsControllerService.addRoleId(roleId), HttpStatus.OK);
  }

  @PostMapping(value = "/updatePermissions")
  public ResponseEntity<HashMap<String, String>> updatePermissions(
      @RequestBody KwRolesPermissionsModel[] kwRolesPermissionsModels) {
    return new ResponseEntity<>(
        rolesPermissionsControllerService.updatePermissions(kwRolesPermissionsModels),
        HttpStatus.OK);
  }
}
