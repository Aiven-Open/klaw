package io.aiven.klaw.controller;

import io.aiven.klaw.model.response.SyncSchemasList;
import io.aiven.klaw.service.SchemaRegistrySyncControllerService;
import lombok.extern.slf4j.Slf4j;
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
      @RequestParam(value = "currentPage", defaultValue = "") String currentPage,
      @RequestParam(value = "topicnamesearch", required = false) String topicNameSearch)
      throws Exception {
    return new ResponseEntity<>(
        schemaRegistrySyncControllerService.getSchemasOfEnvironment(
            kafkaEnvId, pageNo, currentPage),
        HttpStatus.OK);
  }
}
