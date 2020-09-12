package com.kafkamgt.uiapi.config;

import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.helpers.db.cassandra.CassandraDataSourceCondition;
import com.kafkamgt.uiapi.helpers.db.cassandra.HandleDbRequestsCassandra;
import com.kafkamgt.uiapi.helpers.db.rdbms.HandleDbRequestsJdbc;
import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class ManageDatabase {

    @Value("${custom.db.storetype}")
    private
    String dbStore;

    @Value("${custom.envs.order}")
    private String orderOfEnvs;

    private HandleDbRequests handleDbRequests;

    public static HashMap<String, HashMap<String, List<String>>> envParamsMap;

    @Value("${custom.org.name}")
    private
    String orgName;

    @PostConstruct
    public void loadDb() throws Exception {

        if(orgName.equals("Your company name."))
        {
            System.exit(0);
        }

        if (dbStore != null && dbStore.equals("rdbms")) {
            handleDbRequests = handleJdbc();
        } else
            handleDbRequests = handleCassandra();

        handleDbRequests.connectToDb("licenseKey");
        loadEnvParams();

    }

    public HandleDbRequests getHandleDbRequests(){
        return handleDbRequests;
    }

    @Bean()
    @Conditional(JdbcDataSourceCondition.class)
    HandleDbRequestsJdbc handleJdbc() {
        return new HandleDbRequestsJdbc();
    }

    @Bean()
    @Conditional(CassandraDataSourceCondition.class)
    HandleDbRequestsCassandra handleCassandra() {
        return new HandleDbRequestsCassandra();
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
