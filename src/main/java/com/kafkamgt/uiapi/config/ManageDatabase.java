package com.kafkamgt.uiapi.config;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.helpers.db.cassandra.CassandraDataSourceCondition;
import com.kafkamgt.uiapi.helpers.db.cassandra.HandleDbRequestsCassandra;
import com.kafkamgt.uiapi.helpers.db.rdbms.HandleDbRequestsJdbc;
import com.kafkamgt.uiapi.helpers.db.rdbms.JdbcDataSourceCondition;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.service.UtilService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

@Configuration
@Slf4j
public class ManageDatabase {

    @Value("${custom.db.storetype}")
    String dbStore;

    HandleDbRequests handleDbRequests;

    @Autowired
    UtilService utils;

    @Value("${custom.license.key}")
    String licenseKey;

    @Value("${custom.org.name}")
    String orgName;

    @PostConstruct
    public void loadDb() throws Exception {

        if(orgName.equals("Your company name."))
        {
            System.exit(0);
        }
        if(!utils.validateLicense(licenseKey, orgName)) {
            log.info("Invalid License !! Please contact info@kafkawize.com for FREE license key.");
            System.exit(0);
        }

        if(dbStore !=null && dbStore.equals("rdbms")){
            handleDbRequests = handleJdbc();
        }else
            handleDbRequests = handleCassandra();

        handleDbRequests.connectToDb();
    }

    public HandleDbRequests getHandleDbRequests(){
        return this.handleDbRequests;
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
