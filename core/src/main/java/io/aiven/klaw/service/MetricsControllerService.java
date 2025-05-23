package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.KwMetrics;
import io.aiven.klaw.error.KlawException;
import io.aiven.klaw.model.charts.ChartsJsOverview;
import io.aiven.klaw.model.charts.JmxOverview;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableScheduling
public class MetricsControllerService {

  @Autowired ManageDatabase manageDatabase;

  @Value("${klaw.monitoring.metrics.enable:false}")
  private String enableMetrics;

  @Autowired ClusterApiService clusterApiService;

  @Autowired MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  private String getUserName() {
    return mailService.getUserName(commonUtilsService.getPrincipal());
  }

  // default 1 min
  @Scheduled(
      fixedRateString = "${klaw.monitoring.metrics.collectinterval.ms:60000}",
      initialDelay = 60000)
  private void loadMetricsScheduler() {
    if ("false".equals(enableMetrics)) {
      return;
    }

    log.info("Scheduled job : Collect metrics");

    String metricsType = "kafka.server:type=BrokerTopicMetrics";
    String metricsName = "name=MessagesInPerSec";
    String metricsObjectName = metricsType + "," + metricsName;
    String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9996/jmxrmi";

    try {
      Map<String, String> metrics = clusterApiService.retrieveMetrics(jmxUrl, metricsObjectName);
      KwMetrics kwMetrics =
          KwMetrics.builder()
              .metricsTime(new Date().getTime() + "")
              .metricsType(metricsType)
              .metricsName(metricsName)
              .metricsAttributes(metrics.get("Count"))
              .env("1")
              .build();
      manageDatabase.getHandleDbRequests().insertMetrics(kwMetrics);
    } catch (KlawException e) {
      log.error("Error from  retrieveMetrics {}", jmxUrl, e);
    }
  }

  public JmxOverview getBrokerTopMetrics() {
    int tenantId = commonUtilsService.getTenantId(getUserName());
    JmxOverview jmxOverview = new JmxOverview();
    //        String teamName = manageDatabase.getHandleDbRequests()
    //                .getUsersInfo(getUserDetails().getUsername()).getTeam();

    jmxOverview.setBrokerTopMetricsOverview(getBrokerTopMetricsOverview());

    return jmxOverview;
  }

  private ChartsJsOverview getBrokerTopMetricsOverview() {
    int numberOfDays = 30;
    List<CommonUtilsService.ChartsOverviewItem<String, Integer>> metricsCountList;
    String title = "Messages Per Sec";

    //        if(teamName != null) {
    //            activityCountList = manageDatabase.getHandleDbRequests()
    //                    .selectActivityLogByTeam(teamName, numberOfDays);
    //            title = title + " (" + teamName + ")";
    //        }
    //        else {
    // tenant filtering
    //            List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
    try {
      metricsCountList =
          manageDatabase
              .getHandleDbRequests()
              .getAllMetrics("kafka.server:type=BrokerTopicMetrics", "name=MessagesInPerSec", "1");
    } catch (Exception e) {
      log.error("No environments/clusters found.", e);
      metricsCountList = new ArrayList<>();
    }
    //        }

    return commonUtilsService.getChartsJsOverview(
        metricsCountList,
        title,
        "datetime",
        "DateTime",
        "Messages",
        commonUtilsService.getTenantId(getUserName()));
  }
}
