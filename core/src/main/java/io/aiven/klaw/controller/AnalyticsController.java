package io.aiven.klaw.controller;

import io.aiven.klaw.error.KlawBadRequestException;
import io.aiven.klaw.model.charts.TeamOverview;
import io.aiven.klaw.model.response.AclsCountPerEnv;
import io.aiven.klaw.model.response.KwReport;
import io.aiven.klaw.model.response.TopicsCountPerEnv;
import io.aiven.klaw.service.AnalyticsControllerService;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
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

  public static final int DEFAULT_NUMBER_OF_DAYS = 30;
  @Autowired AnalyticsControllerService chartsProcessor;

  @RequestMapping(
      value = "/getTeamsOverview",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<TeamOverview>> getTeamsOverview() {
    return new ResponseEntity<>(
        chartsProcessor.getTeamsOverview(null, DEFAULT_NUMBER_OF_DAYS), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getActivityLogForTeamOverview",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TeamOverview> getActivityLogForTeamOverview(
      @RequestParam("activityLogForTeam") String activityLogForTeam,
      @RequestParam(value = "numberOfDays", required = false, defaultValue = "30") int numberOfDays)
      throws KlawBadRequestException {
    if (0 > numberOfDays || numberOfDays >= 90) {
      throw new KlawBadRequestException(
          "Only values between 1 and 90 are accepted for numberOfDays");
    }

    return new ResponseEntity<>(
        chartsProcessor.getActivityLogForTeamOverview(activityLogForTeam, numberOfDays),
        HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getTopicsCountPerEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<TopicsCountPerEnv> getTopicsCountPerEnv(
      @RequestParam("sourceEnvSelected") String sourceEnvSelected) {
    return new ResponseEntity<>(
        chartsProcessor.getTopicsCountPerEnv(sourceEnvSelected), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getAclsCountPerEnv",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<AclsCountPerEnv> getAclsCountPerEnv(
      @RequestParam("sourceEnvSelected") String sourceEnvSelected) {
    return new ResponseEntity<>(
        chartsProcessor.getAclsCountPerEnv(sourceEnvSelected), HttpStatus.OK);
  }

  @RequestMapping(
      value = "/getKwReport",
      method = RequestMethod.GET,
      produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<KwReport> getKwReport() {
    File file = chartsProcessor.generateReport(DEFAULT_NUMBER_OF_DAYS);
    try {
      byte[] arr = FileUtils.readFileToByteArray(file);
      String str = Base64.getEncoder().encodeToString(arr);
      KwReport kwReport = new KwReport();
      kwReport.setData(str);
      kwReport.setFilename(file.getName());

      return new ResponseEntity<>(kwReport, HttpStatus.OK);
    } catch (IOException e) {
      log.error("Exception:", e);
    }
    return null;
  }
}
