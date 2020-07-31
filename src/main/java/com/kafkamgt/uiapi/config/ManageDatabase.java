package com.kafkamgt.uiapi.config;

import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.helpers.db.cassandra.CassandraDataSourceCondition;
import com.kafkamgt.uiapi.helpers.db.cassandra.HandleDbRequestsCassandra;
import com.kafkamgt.uiapi.helpers.db.rdbms.HandleDbRequestsJdbc;
import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import com.kafkamgt.uiapi.service.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

@Configuration
@Slf4j
public class ManageDatabase {

    @Value("${custom.db.storetype}")
    private
    String dbStore;

    private HandleDbRequests handleDbRequests;

    @Autowired
    private
    UtilService utils;

    @Value("${custom.license.key}")
    private
    String licenseKey;

    @Value("${custom.org.name}")
    private
    String orgName;

    @Autowired
    private
    Environment environment;

    @PostConstruct
    public void loadDb() throws Exception {

        if(orgName.equals("Your company name."))
        {
            System.exit(0);
        }

//        HashMap<String, String> licenseMap = utils.validateLicense();
//        if(! (environment.getActiveProfiles().length >0
//                && environment.getActiveProfiles()[0].equals("integrationtest"))) {
//            if (!licenseMap.get("LICENSE_STATUS").equals(Boolean.TRUE.toString())) {
//                log.info(invalidKeyMessage);
//                System.exit(0);
//            }
//        }

//        UtilService.licenceLoaded = true;

        if (dbStore != null && dbStore.equals("rdbms")) {
            handleDbRequests = handleJdbc();
        } else
            handleDbRequests = handleCassandra();

        handleDbRequests.connectToDb("licenseKey");

//        if(UtilService.licenceLoaded) {
//            handleDbRequests.connectToDb("licenseKey");
//        }else
//        {
////            log.info(invalidKeyMessage);
//            log.info("Herre....");
//            System.exit(0);
//        }
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
}
