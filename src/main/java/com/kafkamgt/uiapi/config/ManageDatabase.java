package com.kafkamgt.uiapi.config;

import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.helpers.db.rdbms.HandleDbRequestsJdbc;
import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import com.kafkamgt.uiapi.service.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ManageDatabase implements ApplicationContextAware {

    @Value("${kafkawize.db.storetype}")
    private
    String dbStore;

    @Value("${kafkawize.envs.order}")
    private String orderOfEnvs;

    private HandleDbRequests handleDbRequests;

    public static HashMap<String, HashMap<String, List<String>>> envParamsMap;

    @Autowired
    private
    UtilService utils;

    @Value("${kafkawize.invalidkey.msg}")
    private
    String invalidKeyMessage;

    @Value("${kafkawize.org.name}")
    private
    String orgName;

    private ApplicationContext contextApp;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.contextApp = applicationContext;
    }

    private void shutdownApp(){
        ((ConfigurableApplicationContext) contextApp).close();
    }

    @Autowired
    private
    Environment environment;

    @PostConstruct
    public void loadDb() throws Exception {

        if(orgName.equals("Your company name."))
        {
            shutdownApp();
        }

        HashMap<String, String> licenseMap = utils.validateLicense();
        if(! (environment.getActiveProfiles().length >0
                && environment.getActiveProfiles()[0].equals("integrationtest"))) {
            if (!licenseMap.get("LICENSE_STATUS").equals(Boolean.TRUE.toString())) {
                log.info(invalidKeyMessage);
                shutdownApp();
            }
        }

        if (dbStore != null && dbStore.equals("rdbms")) {
            handleDbRequests = handleJdbc();
        }

        if(UtilService.licenceLoaded) {
            handleDbRequests.connectToDb("licenseKey");
            loadEnvParams();
        }else
        {
            log.info(invalidKeyMessage);
            shutdownApp();
        }

    }

    public HandleDbRequests getHandleDbRequests(){
        return handleDbRequests;
    }

    @Bean()
    @Conditional(JdbcDataSourceCondition.class)
    HandleDbRequestsJdbc handleJdbc() {
        return new HandleDbRequestsJdbc();
    }

    public List<UserInfo> selectAllUsersInfo(){
        return handleDbRequests.selectAllUsersInfo();
    }

    public void loadEnvParams() {
        envParamsMap = new HashMap<>();
        String[] orderedEnv = orderOfEnvs.split(",");
        for (String targetEnv : orderedEnv) {
            HashMap<String, List<String>> promotionParams = new HashMap<>();
            String envParams = handleDbRequests.selectAllKafkaEnvs()
                    .stream()
                    .filter(env -> env.getName().equals(targetEnv))
                    .collect(Collectors.toList()).get(0)
                    .getOtherParams();
            String[] params = envParams.split(",");
            String defaultPartitions = null;
            for (String param : params) {
                if (param.startsWith("default.partitions")) {
                    defaultPartitions = param.substring(param.indexOf("=") + 1);
                    List<String> defPartitionsList = new ArrayList<>();
                    defPartitionsList.add(defaultPartitions);
                    promotionParams.put("defaultPartitions", defPartitionsList);
                } else if (param.startsWith("max.partitions")) {
                    String maxPartitions = param.substring(param.indexOf("=") + 1);
                    int maxPartitionsInt = Integer.parseInt(maxPartitions);
                    List<String> partitions = new ArrayList<>();

                    for (int i = 1; i < maxPartitionsInt + 1; i++) {
                        if (defaultPartitions != null && defaultPartitions.equals(i + ""))
                            partitions.add(i + " (default)");
                        else
                            partitions.add(i + "");
                    }

                    promotionParams.put("partitionsList", partitions);
                } else if (param.startsWith("replication.factor")) {
                    String repFactor = param.substring(param.indexOf("=") + 1);
                    List<String> repFactorList = new ArrayList<>();
                    repFactorList.add(repFactor);
                    promotionParams.put("repFactor", repFactorList);
                }
            }

            envParamsMap.put(targetEnv, promotionParams);
        }
    }

}
