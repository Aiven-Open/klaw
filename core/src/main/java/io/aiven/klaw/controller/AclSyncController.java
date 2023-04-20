package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.AclInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.SyncAclUpdates;
import io.aiven.klaw.model.SyncBackAcls;
import io.aiven.klaw.service.AclSyncControllerService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AclSyncController {

  @Autowired AclSyncControllerService aclSyncControllerService;

  @PostMapping(
      value = "/updateSyncAcls",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateSyncAcls(
      @RequestBody List<SyncAclUpdates> syncAclUpdates) throws KlawException {
    return new ResponseEntity<>(
        aclSyncControllerService.updateSyncAcls(syncAclUpdates), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getSyncBackAcls",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclInfo>> getSyncBackAcls(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "teamName", required = false) String teamName) {
    return new ResponseEntity<>(
        aclSyncControllerService.getSyncBackAcls(
            envId, pageNo, currentPage, topicNameSearch, teamName),
        HttpStatus.OK);
  }

  @PostMapping(
      value = "/updateSyncBackAcls",
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> updateSyncBackAcls(@RequestBody SyncBackAcls syncBackAcls)
      throws KlawException {
    return new ResponseEntity<>(
        aclSyncControllerService.updateSyncBackAcls(syncBackAcls), HttpStatus.OK);
  }

  // get acls from kafka cluster
  @RequestMapping(
      value = "/getSyncAcls",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<AclInfo>> getSyncAcls(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch,
      @RequestParam(value = "showAllAcls", defaultValue = "false", required = false)
          String showAllAcls)
      throws KlawException {
    return new ResponseEntity<>(
        aclSyncControllerService.getSyncAcls(
            envId, pageNo, currentPage, topicNameSearch, showAllAcls),
        HttpStatus.OK);
  }
}
