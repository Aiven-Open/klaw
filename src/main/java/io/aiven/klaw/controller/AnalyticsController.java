package io.aiven.klaw.controller;

import io.aiven.klaw.model.charts.TeamOverview;
import io.aiven.klaw.service.AnalyticsControllerService;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
public class AnalyticsController {

  @Autowired AnalyticsControllerService chartsProcessor;

  @RequestMapping(
      value = "/getTeamsOverview",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TeamOverview>> getTeamsOverview() {
    return new ResponseEntity<>(chartsProcessor.getTeamsOverview(null), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getActivityLogForTeamOverview",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TeamOverview> getActivityLogForTeamOverview(
      @RequestParam("activityLogForTeam") String activityLogForTeam) {
    return new ResponseEntity<>(
        chartsProcessor.getActivityLogForTeamOverview(activityLogForTeam), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicsCountPerEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getTopicsCountPerEnv(
      @RequestParam("sourceEnvSelected") String sourceEnvSelected) {
    return new ResponseEntity<>(
        chartsProcessor.getTopicsCountPerEnv(sourceEnvSelected), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAclsCountPerEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getAclsCountPerEnv(
      @RequestParam("sourceEnvSelected") String sourceEnvSelected) {
    return new ResponseEntity<>(
        chartsProcessor.getAclsCountPerEnv(sourceEnvSelected), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKwReport",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<HashMap<String, String>> getKwReport() {

    File file = chartsProcessor.generateReport();

    try {
      byte[] arr = FileUtils.readFileToByteArray(file);
      String str = Base64.getEncoder().encodeToString(arr);
      HashMap<String, String> map = new HashMap<>();
      map.put("data", str);
      map.put("filename", file.getName());

      return new ResponseEntity<>(map, HttpStatus.OK);
    } catch (IOException e) {
      log.error("Exception:", e);
    }
    return null;
  }
}
