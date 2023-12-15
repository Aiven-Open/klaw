package io.aiven.klaw.service;

import static io.aiven.klaw.error.KlawErrorMessages.ANALYTICS_101;
import static io.aiven.klaw.error.KlawErrorMessages.ANALYTICS_102;
import static io.aiven.klaw.error.KlawErrorMessages.ANALYTICS_103;
import static io.aiven.klaw.error.KlawErrorMessages.ANALYTICS_104;
import static io.aiven.klaw.error.KlawErrorMessages.ANALYTICS_105;
import static io.aiven.klaw.error.KlawErrorMessages.ANALYTICS_106;
import static io.aiven.klaw.error.KlawErrorMessages.ANALYTICS_107;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.constants.MapConstants;
import io.aiven.klaw.dao.Acl;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.Topic;
import io.aiven.klaw.helpers.KwConstants;
import io.aiven.klaw.model.charts.ChartsJsOverview;
import io.aiven.klaw.model.charts.TeamOverview;
import io.aiven.klaw.model.enums.AclType;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.AclsCountPerEnv;
import io.aiven.klaw.model.response.TopicsCountPerEnv;
import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AnalyticsControllerService {

  // ColumnChart, PieChart, LineChart, Bar, ComboChart
  /*
  tc-chartjs
  tc-chartjs-line
  tc-chartjs-bar
  tc-chartjs-horizontalbar
  tc-chartjs-radar
  tc-chartjs-polararea
  tc-chartjs-pie
  tc-chartjs-doughnut
  tc-chartjs-bubble

  Options
  chart-data: series data
  chart-labels: series labels
  chart-options (default: {}): Chart.js options
  chart-click (optional): onclick event handler
  chart-hover (optional): onmousemove event handler
  chart-colors (default to global colors): colors for the chart
  chart-dataset-override (optional): override datasets individually
  */

  @Autowired ManageDatabase manageDatabase;

  @Autowired private CommonUtilsService commonUtilsService;

  public static final DateTimeFormatter DATE_TIME_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM-ddHH-mm-ss")
          .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, false)
          .toFormatter()
          .withZone(ZoneId.systemDefault());

  private String getCurrentUserName() {
    return commonUtilsService.getCurrentUserName();
  }

  public String getEnvName(String envId) {
    Optional<Env> envFound =
        manageDatabase
            .getKafkaEnvList(commonUtilsService.getTenantId(getCurrentUserName()))
            .stream()
            .filter(env -> Objects.equals(env.getId(), envId))
            .findFirst();
    return envFound.map(Env::getName).orElse(null);
  }

  // For Sync Back Acls
  public AclsCountPerEnv getAclsCountPerEnv(String sourceEnvSelected) {
    int tenantId = commonUtilsService.getTenantId(getCurrentUserName());

    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> aclsPerEnvList =
        manageDatabase.getHandleDbRequests().getAclsCountByEnv(null, tenantId);
    AclsCountPerEnv aclsCountPerEnv = new AclsCountPerEnv();

    // tenant filtering
    List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);
    if (aclsPerEnvList != null) {
      try {
        aclsPerEnvList =
            aclsPerEnvList.stream()
                .filter(
                    mapObj ->
                        allowedEnvIdList.contains(mapObj.getxValue())
                            && Objects.equals(mapObj.getxValue(), sourceEnvSelected))
                .collect(Collectors.toList());

        if (aclsPerEnvList.size() == 1) {
          aclsCountPerEnv.setStatus(ApiResultStatus.SUCCESS.value);
          aclsCountPerEnv.setAclsCount(aclsPerEnvList.get(0).getyValue() + "");
        }
      } catch (Exception e) {
        log.error("No environments/clusters found.", e);
      }
    }
    return aclsCountPerEnv;
  }

  // For Sync Back Topics
  public TopicsCountPerEnv getTopicsCountPerEnv(String sourceEnvSelected) {
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> topicsCountList =
        manageDatabase
            .getHandleDbRequests()
            .getTopicsCountByEnv(commonUtilsService.getTenantId(getCurrentUserName()));

    TopicsCountPerEnv topicsCountPerEnv = new TopicsCountPerEnv();
    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getCurrentUserName());
    try {
      if (topicsCountList != null) {
        topicsCountList =
            topicsCountList.stream()
                .filter(
                    mapObj ->
                        allowedEnvIdSet.contains(mapObj.getxValue())
                            && Objects.equals(mapObj.getxValue(), sourceEnvSelected))
                .collect(Collectors.toList());

        if (topicsCountList.size() == 1) {
          topicsCountPerEnv.setStatus(ApiResultStatus.SUCCESS.value);
          topicsCountPerEnv.setTopicsCount(topicsCountList.get(0).getyValue() + "");
        }
      }
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
    }
    return topicsCountPerEnv;
  }

  public ChartsJsOverview getProducerAclsTeamsOverview(Integer teamId, Integer tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> producerAclsPerTeamList =
        manageDatabase
            .getHandleDbRequests()
            .getAclsCountByTeams(AclType.PRODUCER.value, teamId, tenantId);

    String title = ANALYTICS_101;
    if (teamId != null) {
      title += " (" + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
    } else {
      title += " (all teams)";
    }

    return commonUtilsService.getChartsJsOverview(
        producerAclsPerTeamList, title, "teamid", "Teams", "Producer Acls", tenantId);
  }

  public ChartsJsOverview getConsumerAclsTeamsOverview(Integer teamId, Integer tenantId) {
    List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> consumerAclsPerTeamList =
        manageDatabase
            .getHandleDbRequests()
            .getAclsCountByTeams(AclType.CONSUMER.value, teamId, tenantId);

    String title = ANALYTICS_102;
    if (teamId != null) {
      title += " (" + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
    } else {
      title += " (all teams)";
    }

    return commonUtilsService.getChartsJsOverview(
        consumerAclsPerTeamList, title, "teamid", "Teams", "Consumer Acls", tenantId);
  }

  public ChartsJsOverview getTopicsTeamsOverview(Integer teamId, Integer tenantId) {

    List<CommonUtilsService.ChartsOverviewItem<Integer, Integer>> teamCountList =
        manageDatabase.getHandleDbRequests().getTopicsCountByTeams(teamId, tenantId);
    String title = ANALYTICS_103;
    if (teamId != null) {
      title += " (" + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
    } else {
      title += " (all teams)";
    }

    return commonUtilsService.getChartsJsOverview(
        teamCountList, title, "teamid", "Teams", "Topics", tenantId);
  }

  public ChartsJsOverview getTopicsEnvOverview(Integer tenantId, PermissionType permissionType) {
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> teamCountList =
        manageDatabase.getHandleDbRequests().getTopicsCountByEnv(tenantId);

    // tenant filtering
    try {
      final Set<String> allowedEnvIdSet =
          commonUtilsService.getEnvsFromUserId(getCurrentUserName());
      if (teamCountList != null) {
        teamCountList =
            teamCountList.stream()
                .filter(mapObj -> allowedEnvIdSet.contains(mapObj.getxValue()))
                .collect(Collectors.toList());
        teamCountList =
            teamCountList.stream()
                .map(item -> item.transformX(this::getEnvName))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      teamCountList = new ArrayList<>();
    }

    return commonUtilsService.getChartsJsOverview(
        teamCountList, ANALYTICS_104, MapConstants.CLUSTER_KEY, "Clusters", "Topics", tenantId);
  }

  public ChartsJsOverview getTopicsPerTeamEnvOverview(int tenantId) {
    final String currentUserName = getCurrentUserName();
    Integer userTeamId = commonUtilsService.getTeamId(currentUserName);
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> teamCountList = null;
    if (currentUserName != null) {
      teamCountList =
          manageDatabase.getHandleDbRequests().getAllTopicsForTeamGroupByEnv(userTeamId, tenantId);
    }

    String title =
        "Topics per cluster (" + manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId) + ")";

    return commonUtilsService.getChartsJsOverview(
        teamCountList, title, MapConstants.CLUSTER_KEY, "Clusters", "Topics", tenantId);
  }

  public ChartsJsOverview getPartitionsEnvOverview(Integer teamId, Integer tenantId) {

    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> partitionsCountList =
        manageDatabase.getHandleDbRequests().getPartitionsCountByEnv(teamId, tenantId);
    String title = ANALYTICS_105;
    if (teamId != null) {
      title += " (" + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
    }

    // tenant filtering
    try {
      List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);
      if (partitionsCountList != null) {
        partitionsCountList =
            partitionsCountList.stream()
                .filter(mapObj -> allowedEnvIdList.contains(mapObj.getxValue()))
                .collect(Collectors.toList());
        partitionsCountList =
            partitionsCountList.stream()
                .map(item -> item.transformX(x -> getEnvName(item.getxValue())))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      partitionsCountList = new ArrayList<>();
    }

    return commonUtilsService.getChartsJsOverview(
        partitionsCountList, title, MapConstants.CLUSTER_KEY, "Clusters", "Partitions", tenantId);
  }

  public ChartsJsOverview getAclsEnvOverview(Integer teamId, Integer tenantId) {

    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> aclsPerEnvList =
        manageDatabase.getHandleDbRequests().getAclsCountByEnv(teamId, tenantId);
    String title = ANALYTICS_106;
    if (teamId != null) {
      title += " (" + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
    }

    // tenant filtering
    try {
      List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);
      if (aclsPerEnvList != null) {
        aclsPerEnvList =
            aclsPerEnvList.stream()
                .filter(mapObj -> allowedEnvIdList.contains(mapObj.getxValue()))
                .collect(Collectors.toList());
        aclsPerEnvList =
            aclsPerEnvList.stream()
                .map(item -> item.transformX(x -> getEnvName(item.getxValue())))
                .collect(Collectors.toList());
      }
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      aclsPerEnvList = new ArrayList<>();
    }

    return commonUtilsService.getChartsJsOverview(
        aclsPerEnvList, title, MapConstants.CLUSTER_KEY, "Clusters", "Acls", tenantId);
  }

  public ChartsJsOverview getActivityLogOverview(Integer teamId, Integer tenantId) {
    int numberOfDays = 30;
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> activityCountList;
    String title = ANALYTICS_107;

    if (teamId != null) {
      activityCountList =
          manageDatabase.getHandleDbRequests().getActivityLogByTeam(teamId, numberOfDays, tenantId);
      title = title + " (" + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
    } else {
      // tenant filtering
      List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);

      try {
        activityCountList =
            manageDatabase
                .getHandleDbRequests()
                .getActivityLogForLastDays(
                    numberOfDays, allowedEnvIdList.toArray(new String[0]), tenantId);
      } catch (Exception e) {
        log.error("No environments/clusters found.", e);
        activityCountList = new ArrayList<>();
      }
    }

    return commonUtilsService.getChartsJsOverview(
        activityCountList, title, "dateofactivity", "Days", "Activities", tenantId);
  }

  public List<TeamOverview> getTeamsOverview(String forTeam) {
    List<TeamOverview> listTeamOverview = new ArrayList<>();
    TeamOverview teamOverview = new TeamOverview();

    final String currentUserName = getCurrentUserName();
    Integer userTeamId = commonUtilsService.getTeamId(currentUserName);

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS)) {
      int tenantId = commonUtilsService.getTenantId(currentUserName);

      teamOverview.setProducerAclsPerTeamsOverview(
          getProducerAclsTeamsOverview(userTeamId, tenantId));
      teamOverview.setConsumerAclsPerTeamsOverview(
          getConsumerAclsTeamsOverview(userTeamId, tenantId));

      teamOverview.setTopicsPerEnvOverview(getTopicsPerTeamEnvOverview(tenantId));
      teamOverview.setPartitionsPerEnvOverview(getPartitionsEnvOverview(userTeamId, tenantId));

      teamOverview.setActivityLogOverview(getActivityLogOverview(userTeamId, tenantId));
      teamOverview.setAclsPerEnvOverview(getAclsEnvOverview(userTeamId, tenantId));

      teamOverview.setTopicsPerTeamsOverview(getTopicsTeamsOverview(userTeamId, tenantId));
      listTeamOverview.add(teamOverview);
    } else {
      int tenantId = commonUtilsService.getTenantId(currentUserName);

      teamOverview.setTopicsPerEnvOverview(
          getTopicsEnvOverview(tenantId, PermissionType.ALL_TEAMS_REPORTS));
      teamOverview.setPartitionsPerEnvOverview(getPartitionsEnvOverview(null, tenantId));
      teamOverview.setAclsPerEnvOverview(getAclsEnvOverview(null, tenantId));

      teamOverview.setTopicsPerTeamsOverview(getTopicsTeamsOverview(null, tenantId));

      teamOverview.setProducerAclsPerTeamsOverview(getProducerAclsTeamsOverview(null, tenantId));
      teamOverview.setConsumerAclsPerTeamsOverview(getConsumerAclsTeamsOverview(null, tenantId));

      teamOverview.setActivityLogOverview(getActivityLogOverview(null, tenantId));

      listTeamOverview.add(teamOverview);
    }

    return listTeamOverview;
  }

  public TeamOverview getActivityLogForTeamOverview(String forTeam) {
    TeamOverview teamOverview = new TeamOverview();
    final String currentUserName = getCurrentUserName();
    Integer userTeamId = commonUtilsService.getTeamId(currentUserName);

    teamOverview.setTopicsPerTeamPerEnvOverview(
        getTopicsPerTeamEnvOverview(commonUtilsService.getTenantId(currentUserName)));
    if (forTeam != null && forTeam.equals("true")) {
      teamOverview.setActivityLogOverview(getActivityLogOverview(userTeamId, 101));
    }

    return teamOverview;
  }

  public File generateReport() {
    int tenantId = commonUtilsService.getTenantId(getCurrentUserName());
    String kwReportsLocation =
        manageDatabase.getKwPropertyValue(KwConstants.KW_REPORTS_TMP_LOCATION_KEY, tenantId);

    List<TeamOverview> totalOverviewList = getTeamsOverview(null);
    final Map<String, List<String>> topicNames = getTopicNames(tenantId);
    final Map<String, List<String>> consumerGroups = getConsumerGroups(tenantId);
    File zipFile =
        new File(
            kwReportsLocation + "KwReport" + DATE_TIME_FORMATTER.format(Instant.now()) + ".zip");
    ZipOutputStream zipOutputStream = null;
    try {
      zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
    } catch (FileNotFoundException e) {
      log.error("Exception:", e);
    }

    String actualFileName;
    List<File> reportFilesOfTenants = new ArrayList<>();
    for (TeamOverview totalOverview : totalOverviewList) {
      if (totalOverview.getTenantName() != null) {
        actualFileName = "Klaw-" + totalOverview.getTenantName() + ".xlsx";
      } else {
        actualFileName = "KlawReport" + ".xlsx";
      }

      String fileName = kwReportsLocation + actualFileName;
      File reportFile = new File(fileName);
      reportFilesOfTenants.add(reportFile);

      XSSFWorkbook workbook = new XSSFWorkbook();

      generateReportPerView(totalOverview.getTopicsPerEnvOverview(), workbook, reportFile);
      generateReportPerView(totalOverview.getPartitionsPerEnvOverview(), workbook, reportFile);
      generateReportPerView(totalOverview.getTopicsPerTeamsOverview(), workbook, reportFile);
      generateReportPerView(totalOverview.getAclsPerEnvOverview(), workbook, reportFile);
      generateReportPerView(totalOverview.getProducerAclsPerTeamsOverview(), workbook, reportFile);
      generateReportPerView(totalOverview.getConsumerAclsPerTeamsOverview(), workbook, reportFile);
      generateReportPerView(totalOverview.getActivityLogOverview(), workbook, reportFile);

      addTopicNamesPerEnvToReport(topicNames, workbook, reportFile, "Topics");
      addTopicNamesPerEnvToReport(consumerGroups, workbook, reportFile, "ConsumerGroups");

      log.info("Report generated");
      if (zipOutputStream != null) {
        writeToZipFile(zipOutputStream, fileName, actualFileName);
      }
    }

    try {
      if (zipOutputStream != null) {
        zipOutputStream.close();
        // delete xlsx report file after writing to zip file.
        for (File reportFilesOfTenant : reportFilesOfTenants) {
          reportFilesOfTenant.delete();
        }
      }
    } catch (IOException e) {
      log.error("Exception:", e);
    }

    return zipFile;
  }

  private void writeToZipFile(ZipOutputStream zos, String fullFileName, String fileName) {
    try {
      ZipEntry e = new ZipEntry(fileName);
      zos.putNextEntry(e);

      FileInputStream fis = new FileInputStream(fullFileName);

      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
        zos.write(bytes, 0, length);
      }

    } catch (IOException e) {
      log.error("Exception:", e);
    }
  }

  private void addTopicNamesPerEnvToReport(
      Map<String, List<String>> topicNames,
      XSSFWorkbook workbook,
      File reportFile,
      String sheetName) {
    XSSFSheet sheet = workbook.createSheet(sheetName);
    Map<Integer, Object[]> data = new TreeMap<>();
    int id = 1;
    List<String> envNames = new ArrayList<>(topicNames.keySet());

    // set header row
    data.put(id, new Object[] {"S.No", envNames});
    id++;

    int maxSize = 0;

    List<List<String>> allTopicLists = new ArrayList<>();
    for (String envName : envNames) {
      allTopicLists.add(topicNames.get(envName));
      if (topicNames.get(envName).size() > maxSize) {
        maxSize = topicNames.get(envName).size();
      }
    }

    // set content
    List<String> rowValList;
    for (int i = 0; i < maxSize; i++) {
      rowValList = new ArrayList<>();
      for (List<String> allTopicList : allTopicLists) {
        if (allTopicList.size() > i) {
          rowValList.add(allTopicList.get(i));
        } else {
          rowValList.add("");
        }
      }
      data.put(id, new Object[] {id - 1, rowValList});
      id++;
    }

    addData(sheet, data);
    writeToFile(sheetName, workbook, reportFile);
  }

  private void addData(XSSFSheet sheet, Map<Integer, Object[]> data) {
    int rownum = 0;
    org.apache.poi.ss.usermodel.Row rowXl;
    Set<Integer> keyset = data.keySet();
    for (Integer key : keyset) {
      rowXl = sheet.createRow(rownum++);
      Object[] objArr = data.get(key);
      int cellnum = 0;
      Cell cell;
      for (Object obj : objArr) {
        if (obj instanceof String) {
          cell = rowXl.createCell(cellnum++);
          cell.setCellValue((String) obj);
        } else if (obj instanceof Integer) {
          cell = rowXl.createCell(cellnum++);
          cell.setCellValue((Integer) obj);
        } else if (obj instanceof List) {
          List<String> strList = (List<String>) obj;
          for (String s : strList) {
            cell = rowXl.createCell(cellnum++);
            cell.setCellValue(s);
          }
        }
      }
    }
  }

  private void generateReportPerView(
      ChartsJsOverview chartsJsOverview, XSSFWorkbook workbook, File reportFile) {
    // Create a blank sheet
    XSSFSheet sheet = workbook.createSheet(chartsJsOverview.getTitleForReport());
    // This data needs to be written (Object[])
    Map<Integer, Object[]> data = new HashMap<>();
    List<Integer> data1 = chartsJsOverview.getData();
    List<String> labels = chartsJsOverview.getLabels();

    // header
    data.put(
        1,
        new Object[] {"S.No", chartsJsOverview.getXAxisLabel(), chartsJsOverview.getYAxisLabel()});
    int id = 2;

    // content
    for (int i = 0; i < data1.size(); i++) {
      if (!"".equals(labels.get(i))) {
        data.put(id, new Object[] {id - 1, labels.get(i), data1.get(i)});
        id++;
      }
    }

    addData(sheet, data);

    writeToFile(chartsJsOverview.getOptions().getTitle().toString(), workbook, reportFile);
  }

  private void writeToFile(String title, XSSFWorkbook workbook, File reportFile) {
    try {
      // Write the workbook in file system
      FileOutputStream out = new FileOutputStream(reportFile);
      workbook.write(out);
      out.close();
      log.info("Added Sheet {}", title);
    } catch (Exception e) {
      log.error("Error : writeToFile ", e);
    }
  }

  private Map<String, List<String>> getTopicNames(int tenantId) {
    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getCurrentUserName());

    Map<String, List<String>> topicsPerEnv = new HashMap<>();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS)) {
      // normal user
      Integer userTeamId = commonUtilsService.getTeamId(getCurrentUserName());
      List<Topic> topics =
          manageDatabase.getHandleDbRequests().getTopicsforTeam(userTeamId, tenantId);

      for (String env : allowedEnvIdSet) {
        topicsPerEnv.put(
            getEnvName(env),
            topics.stream()
                .filter(topic -> Objects.equals(topic.getEnvironment(), env))
                .map(Topic::getTopicname)
                .sorted()
                .collect(Collectors.toList()));
      }
    } else {
      // admin
      List<Topic> topics = manageDatabase.getHandleDbRequests().getAllTopics(tenantId);
      for (String env : allowedEnvIdSet) {
        topicsPerEnv.put(
            getEnvName(env),
            topics.stream()
                .filter(topic -> Objects.equals(topic.getEnvironment(), env))
                .map(Topic::getTopicname)
                .sorted()
                .collect(Collectors.toList()));
      }
    }

    return topicsPerEnv;
  }

  private Map<String, List<String>> getConsumerGroups(int tenantId) {
    // tenant filtering
    final Set<String> allowedEnvIdSet = commonUtilsService.getEnvsFromUserId(getCurrentUserName());

    Map<String, List<String>> aclsPerEnv = new HashMap<>();
    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS)) {
      // normal user
      Integer userTeamId = commonUtilsService.getTeamId(getCurrentUserName());
      List<Acl> acls =
          manageDatabase.getHandleDbRequests().getConsumerGroupsforTeam(userTeamId, tenantId);

      for (String env : allowedEnvIdSet) {
        aclsPerEnv.put(
            getEnvName(env),
            acls.stream()
                .filter(
                    acl ->
                        Objects.equals(acl.getEnvironment(), env) && acl.getConsumergroup() != null)
                .map(Acl::getConsumergroup)
                .sorted()
                .collect(Collectors.toList()));
      }
    } else {
      // admin
      List<Acl> acls = manageDatabase.getHandleDbRequests().getAllConsumerGroups(tenantId);
      for (String env : allowedEnvIdSet) {
        aclsPerEnv.put(
            getEnvName(env),
            acls.stream()
                .filter(
                    acl ->
                        Objects.equals(acl.getEnvironment(), env) && acl.getConsumergroup() != null)
                .map(Acl::getConsumergroup)
                .sorted()
                .collect(Collectors.toList()));
      }
    }

    return aclsPerEnv;
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
