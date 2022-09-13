package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.KwClusters;
import com.kafkamgt.uiapi.model.EnvModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.springframework.beans.BeanUtils.copyProperties;

@EnableScheduling
@Service
@Slf4j
public class EnvControllerService {

    @Autowired
    private ClusterApiService clusterApiService;

    @Autowired
    ManageDatabase manageDatabase;

    // every 1 hour
    @Scheduled(fixedRateString = "PT1H", initialDelay=60000)
    public void loadTenantActiveStatus(){
        try {
            for (Integer tenantId : manageDatabase.getTenantMap().keySet()) {
                long expiryLong = manageDatabase.getTenantFullConfig(tenantId).getLicenseExpiry().getTime();
                long currentTime = System.currentTimeMillis();
                long diffTime = expiryLong - currentTime;
                if(diffTime < 0) {
                    manageDatabase.getHandleDbRequests().setTenantActivestatus(tenantId, false);
                    manageDatabase.loadOneTenant(tenantId);
                }
            }
        } catch (Exception e) {
            log.info("loadTenantActiveStatus : ", e);
        }
    }

    //30 mins in milliseconds 1800000  // ${kafkawize.reloadclusterstatus.interval:120000}
    @Scheduled(fixedRateString = "PT1H", initialDelay=60000)
    public void loadEnvsWithStatus(){
        updateEnvsStatus();
    }

    public void updateEnvsStatus() {
        log.info("Scheduled job (kafkawize.reloadclusterstatus.interval) : Update cluster status");
        try {
            Set<Integer> tenants = manageDatabase.getTenantMap().keySet();

            for (Integer tenantId : tenants) {
                List<Env> envList = manageDatabase.getKafkaEnvListAllTenants(tenantId);

                for (Env env : envList) {
                    updateEnvStatusPerEnv(tenantId, env);
                }
                manageDatabase.loadEnvMapForOneTenant(tenantId);
            }

            for (Integer tenantId : tenants) {
                List<Env> envList = manageDatabase.getSchemaRegEnvList(tenantId);

                for (Env env : envList) {
                    updateEnvStatusPerEnv(tenantId, env);
                }
                manageDatabase.loadEnvMapForOneTenant(tenantId);
            }

            for (Integer tenantId : tenants) {
                List<Env> envList = manageDatabase.getKafkaConnectEnvList(tenantId);

                for (Env env : envList) {
                    updateEnvStatusPerEnv(tenantId, env);
                }
                manageDatabase.loadEnvMapForOneTenant(tenantId);
            }
        } catch (Exception e) {
            log.error("Error in loading cluster status ", e);
        }
    }

    private void updateEnvStatusPerEnv(Integer tenantId, Env env) {
        String status;
        status = clusterApiService.getKafkaClusterStatus(manageDatabase.getClusters(env.getType(), tenantId)
                        .get(env.getClusterId()).getBootstrapServers(),
                manageDatabase.getClusters(env.getType(), tenantId)
                        .get(env.getClusterId()).getProtocol(),
                manageDatabase.getClusters(env.getType(), tenantId)
                        .get(env.getClusterId()).getClusterName(), env.getType(), tenantId);

        env.setEnvStatus(status);
        manageDatabase.getHandleDbRequests().addNewEnv(env);
    }
}
