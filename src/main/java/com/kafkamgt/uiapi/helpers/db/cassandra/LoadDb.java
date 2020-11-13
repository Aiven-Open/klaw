package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class LoadDb {
    private static Logger LOG = LoggerFactory.getLogger(LoadDb.class);

    private static String CREATE_SQL = "scripts/base/cassandra/createcassandra.sql";

    private static String INSERT_SQL = "scripts/base/cassandra/insertdata.sql";

    private static String DROP_SQL = "scripts/base/cassandra/dropcassandra.sql";

    @Value("${kafkawize.version:4.4}")
    private String kafkawizeVersion;

    @Autowired
    ResourceLoader resourceLoader;

    public Session session;

    public LoadDb(){}
    public LoadDb(Session session){
        this.session = session;
    }

    public void createTables(){

        try {
            BufferedReader in = getReader(CREATE_SQL);
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("create"))
                    session.execute(tmpLine.trim());
            }
            in.close();
        }catch (Exception e){
            LOG.error("Exiting .. could not setup create database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Cassandra Create DB Tables setup done !! ");

        String ALTER_SQL = "scripts/base/cassandra/"+kafkawizeVersion+"_updates/alter.sql";

        try{

            BufferedReader in = getReader(ALTER_SQL);
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("alter"))
                    session.execute(tmpLine.trim());
            }
            in.close();
        }catch (Exception e){
            //LOG.error("Could not setup alter database tables " + e.getMessage());
        }
        LOG.info("Cassandra Alter DB Tables setup done !! ");
    }

    public void insertData(){

        try{
            BufferedReader in = getReader(INSERT_SQL);
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("insert"))
                    session.execute(tmpLine.trim());
            }
            in.close();
        } catch (Exception e) {
            LOG.error("Exiting .. could not setup insert database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Cassandra Insert DB Tables setup done !! ");
    }

    public void dropTables(){

        try{
            BufferedReader in = getReader(DROP_SQL);
            String tmpLine = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("drop"))
                    session.execute(tmpLine.trim());
            }
            in.close();
        } catch (Exception e) {
            LOG.error("Exiting .. could not setup insert database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Cassandra drop DB Tables setup done !! ");
    }

    private BufferedReader getReader(String sql) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:"+sql);
        InputStream inputStream = resource.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new BufferedReader(inputStreamReader);
    }
}
