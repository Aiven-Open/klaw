package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Component
public class LoadDb {
    private static Logger LOG = LoggerFactory.getLogger(LoadDb.class);

    public Session session;

    public void createTables(){

        try (BufferedReader in = new BufferedReader(new FileReader("src/main/resources/scripts/base/cassandra/createcassandra.sql"))) {
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("create"))
                    session.execute(tmpLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOG.error("Exiting .. could not setup database tables "+e);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Exiting .. could not setup database tables "+e);
            System.exit(0);
        }


        LOG.info("Cassandra Create DB Tables setup done !! ");
    }

    public void insertData(){

        try (BufferedReader in = new BufferedReader(new FileReader("src/main/resources/scripts/base/cassandra/insertdata.sql"))) {
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("insert"))
                    session.execute(tmpLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            LOG.error("Exiting .. could not setup database tables "+e);
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Exiting .. could not setup database tables "+e);
            System.exit(0);
        }


        LOG.info("Cassandra Insert DB Tables setup done !! ");
    }
}
