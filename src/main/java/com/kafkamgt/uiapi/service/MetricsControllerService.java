package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.KwMetrics;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.charts.ChartsJsOverview;
import com.kafkamgt.uiapi.model.charts.JmxOverview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@EnableScheduling
public class MetricsControllerService {

    @Autowired
    ManageDatabase manageDatabase;

    @Value("${kafkawize.monitoring.metrics.enable:false}")
    private String enableMetrics;

    @Autowired
    ClusterApiService clusterApiService;

    @Autowired
    MailUtils mailService;

    @Autowired
    private CommonUtilsService commonUtilsService;

    private String getUserName(){
        return mailService.getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private List<String> getEnvsFromUserId(String userDetails) {
        Integer userTeamId = getMyTeamId(userDetails);
        return manageDatabase.getTeamsAndAllowedEnvs(userTeamId, commonUtilsService.getTenantId(userDetails));
    }

    private Integer getMyTeamId(String userName){
        return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
    }

    // default 1 min
    @Scheduled(fixedRateString = "${kafkawize.monitoring.metrics.collectinterval.ms:60000}", initialDelay=60000)
    private void loadMetricsScheduler(){
        if(enableMetrics.equals("false"))
            return;

        log.info("Scheduled job : Collect metrics");

        String metricsType = "kafka.server:type=BrokerTopicMetrics";
        String metricsName = "name=MessagesInPerSec";
        String metricsObjectName = metricsType + "," + metricsName;
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:9996/jmxrmi";

        try {
            HashMap<String, String> metrics = clusterApiService.retrieveMetrics(jmxUrl, metricsObjectName);
            KwMetrics kwMetrics = KwMetrics.builder()
                    .metricsTime(new Date().getTime() + "")
                    .metricsType(metricsType)
                    .metricsName(metricsName)
                    .metricsAttributes(metrics.get("Count"))
                    .env("1").build();
            manageDatabase.getHandleDbRequests().insertMetrics(kwMetrics);
        } catch (KafkawizeException e) {
            log.error("Error from  retrieveMetrics {} {}", jmxUrl, e.toString());
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
        List<HashMap<String, String>> metricsCountList;
        String title  = "Messages Per Sec";

//        if(teamName != null) {
//            activityCountList = manageDatabase.getHandleDbRequests()
//                    .selectActivityLogByTeam(teamName, numberOfDays);
//            title = title + " (" + teamName + ")";
//        }
//        else {
            // tenant filtering
//            List<String> allowedEnvIdList = getEnvsFromUserId(getUserName());
            try {
                metricsCountList = manageDatabase.getHandleDbRequests()
                        .selectAllMetrics("kafka.server:type=BrokerTopicMetrics",
                                "name=MessagesInPerSec","1");
            } catch (Exception exception) {
                log.error("No environments/clusters found.");
                metricsCountList = new ArrayList<>();
            }
//        }

        return commonUtilsService.getChartsJsOverview(metricsCountList, title, "messagescount", "datetime",
                "DateTime","Messages", commonUtilsService.getTenantId(getUserName()));
    }

    private Object getPrincipal(){
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
