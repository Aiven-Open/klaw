package io.aiven.klaw.controller;

import io.aiven.klaw.model.response.SyncTopicsList;
import io.aiven.klaw.service.SchemaRegistrySyncControllerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class SchemaRegistrySyncController {

  @Autowired private SchemaRegistrySyncControllerService schemaRegistrySyncControllerService;

  @RequestMapping(
      value = "/getSchemasFromCluster",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<SyncTopicsList> getSchemasFromCluster(
      @RequestParam("env") String envId,
      @RequestParam("pageNo") String pageNo,
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch)
      throws Exception {
    return new ResponseEntity<>(
        schemaRegistrySyncControllerService.getSchemasFromCluster(
            envId, pageNo, currentPage, topicNameSearch),
        HttpStatus.OK);
  }
}
