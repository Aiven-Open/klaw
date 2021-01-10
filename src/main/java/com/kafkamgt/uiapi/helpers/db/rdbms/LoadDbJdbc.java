package com.kafkamgt.uiapi.helpers.db.rdbms;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Objects;

@Service
@Slf4j
public class LoadDbJdbc  implements ApplicationContextAware {
    private static Logger LOG = LoggerFactory.getLogger(LoadDbJdbc.class);

    private static String CREATE_SQL = "scripts/base/rdbms/ddl-jdbc.sql";

    private static String DROP_SQL = "scripts/base/rdbms/dropjdbc.sql";

    @Autowired(required=false)
    private JdbcTemplate jdbcTemplate;

    @Value("${kafkawize.version:4.5}")
    private String kafkawizeVersion;

    @Value("${kafkawize.dbscripts.location:scripts/base/rdbms/}")
    private String scriptsLocation;

    @Autowired
    ResourceLoader resourceLoader;

    private ApplicationContext contextApp;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.contextApp = applicationContext;
    }

    private void shutdownApp(){
        ((ConfigurableApplicationContext) contextApp).close();
    }

    public void createTables(){

        createTables("create");
        LOG.info("Create DB Tables setup done !! ");
//        createTables("alter");
//        LOG.info("Alter DB Tables setup done !! ");
    }

    private void createTables(String sqlType){

//        String ALTER_SQL = "scripts/base/rdbms/"+kafkawizeVersion+"_updates/alter.sql";
        String CREATE_SQL = scriptsLocation + "ddl-jdbc.sql";
        try{
            BufferedReader in = null;
            if(sqlType.equals("create"))
                 in = getReader(CREATE_SQL);
//            else if(sqlType.equals("alter"))
//                in = getReader(ALTER_SQL);

            String tmpLine = "";
            StringBuilder execQuery = new StringBuilder();
            while((tmpLine= Objects.requireNonNull(in).readLine())!=null){
                if(tmpLine.toLowerCase().startsWith(sqlType) && tmpLine.toLowerCase().endsWith(";")){
                    execQuery = new StringBuilder(tmpLine);
                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.toString().trim());
                    execQuery = new StringBuilder();
                }
                else if(tmpLine.toLowerCase().startsWith(sqlType))
                    execQuery = new StringBuilder(tmpLine);
                else if(tmpLine.toLowerCase().endsWith(";"))
                {
                    execQuery.append(tmpLine);
                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.toString().trim());
                    execQuery = new StringBuilder();
                }
                else{
                    execQuery.append(tmpLine);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            LOG.error("Could not create/alter database tables " + e.getMessage());
            if(sqlType.equals("create"))
                shutdownApp();
        }
    }

    public void dropTables(){
        try{
            BufferedReader in = getReader(DROP_SQL);
            String tmpLine = "";
            String execQuery = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("drop")
                        && tmpLine.toLowerCase().endsWith(";")){
                    execQuery = tmpLine;
                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.trim());
                    execQuery = "";
                }
                else if(tmpLine.toLowerCase().startsWith("drop"))
                    execQuery = tmpLine;
                else if(tmpLine.toLowerCase().endsWith(";"))
                {
                    execQuery = execQuery + tmpLine;
                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.trim());
                    execQuery = "";
                }
                else{
                    execQuery = execQuery + tmpLine;
                }

            }
        }catch (Exception e){
            LOG.error("Exiting .. could not setup create database tables " + e.getMessage());
            shutdownApp();
        }
        LOG.info("Drop DB Tables setup done !! ");
    }

    public void insertData(){

        try{
            String INSERT_SQL = scriptsLocation + "insertdata.sql";
            BufferedReader in = getReader(INSERT_SQL);
            String tmpLine = "";
            String execQuery = "";
            while((tmpLine=in.readLine())!=null){
                boolean isInsert = tmpLine.toLowerCase().startsWith("insert") || tmpLine.toLowerCase().startsWith("update");
                if((isInsert) && tmpLine.toLowerCase().endsWith(";")){
                    execQuery = tmpLine;
                    log.info("Executing query : "+ execQuery);
                    try {
                        jdbcTemplate.execute(execQuery.trim());
                    }catch (Exception sqlException){
                        if(sqlException instanceof DuplicateKeyException){
                            //do nothing
                        }else throw sqlException;
                    }
                    execQuery = "";
                }
                else if(isInsert)
                    execQuery = tmpLine;
                else if(tmpLine.toLowerCase().endsWith(";"))
                {
                    execQuery = execQuery + tmpLine;
                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.trim());
                    execQuery = "";
                }
                else{
                    execQuery = execQuery + tmpLine;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            LOG.error("Exiting .. could not insert data into tables" + e.getMessage());
            shutdownApp();
        }
        LOG.info("Insert DB Tables setup done !! ");
    }

    private BufferedReader getReader(String sql) throws IOException {
        Resource resource = resourceLoader.getResource(sql);
        InputStream inputStream = resource.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new BufferedReader(inputStreamReader);
    }

}
