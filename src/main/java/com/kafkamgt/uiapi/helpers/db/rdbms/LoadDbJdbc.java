package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;

@Component
public class LoadDbJdbc {
    private static Logger LOG = LoggerFactory.getLogger(LoadDbJdbc.class);

    private static String INSERT_SQL = "src/main/resources/scripts/base/rdbms/insertdata.sql";

    public Session session;

    public LoadDbJdbc(){}
    public LoadDbJdbc(Session session){
        this.session = session;
    }

    public void insertData(){

        try (BufferedReader in = new BufferedReader(new FileReader(INSERT_SQL))) {
            String tmpLine;
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("insert"))
                    session.execute(tmpLine);
            }
        } catch (Exception e) {
            LOG.error("Exiting .. could not setup database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Jdbc Insert DB Tables setup done !! ");
    }
}
