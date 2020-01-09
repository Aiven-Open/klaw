package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

@Component
public class LoadDb {
    private static Logger LOG = LoggerFactory.getLogger(LoadDb.class);

    private static String CREATE_SQL = "src/main/resources/scripts/base/cassandra/createcassandra.sql";

    private static String INSERT_SQL = "src/main/resources/scripts/base/cassandra/insertdata.sql";

    private static String DROP_SQL = "src/main/resources/scripts/base/cassandra/dropcassandra.sql";

    public Session session;

    public LoadDb(){}
    public LoadDb(Session session){
        this.session = session;
    }

    public void createTables(){

        try (BufferedReader in = new BufferedReader(new FileReader(CREATE_SQL))) {
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("create"))
                    session.execute(tmpLine.trim());
            }
        }catch (Exception e){
            LOG.error("Exiting .. could not setup create database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Cassandra Create DB Tables setup done !! ");
    }

    public void insertData(){

        try (BufferedReader in = new BufferedReader(new FileReader(INSERT_SQL))) {
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("insert"))
                    session.execute(tmpLine.trim());
            }
        } catch (Exception e) {
            LOG.error("Exiting .. could not setup insert database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Cassandra Insert DB Tables setup done !! ");
    }

    public void dropTables(){

        try (BufferedReader in = new BufferedReader(new FileReader(DROP_SQL))) {
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("drop"))
                    session.execute(tmpLine.trim());
            }
        } catch (Exception e) {
            LOG.error("Exiting .. could not setup insert database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Cassandra drop DB Tables setup done !! ");
    }
}
