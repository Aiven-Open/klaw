package com.kafkamgt.uiapi.config;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

@Component
public class DbConfig {
    private static Logger LOG = LoggerFactory.getLogger(DbConfig.class);
    Cluster cluster;
    Session session;

    @Value("${cassandradb.url}")
    String clusterConnHost;

    @Value("${cassandradb.port}")
    int clusterConnPort;

    @Value("${cassandradb.keyspace}")
    String keyspace;

    @PostConstruct
    public void connectToCassandra() {

        CodecRegistry myCodecRegistry;
        myCodecRegistry = CodecRegistry.DEFAULT_INSTANCE;
        myCodecRegistry.register(InstantCodec.instance);

        cluster = Cluster
                .builder()
                .addContactPoint(clusterConnHost)
                .withPort(clusterConnPort)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withCodecRegistry(myCodecRegistry)
                .withoutJMXReporting()
                .build();
        session = cluster.connect();

        createTables();
        insertData();
        cluster.close();
    }


    public void createTables(){

        try (BufferedReader in = new BufferedReader(new FileReader("src/main/resources/scripts/base/createcassandra.sql"))) {
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


        LOG.info("Cassandra Create DB Tables setup done !!");
    }

    public void insertData(){

        try (BufferedReader in = new BufferedReader(new FileReader("src/main/resources/scripts/base/insertdata.sql"))) {
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


        LOG.info("Cassandra Insert DB Tables setup done !!");
    }
}
