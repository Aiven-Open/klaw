package com.kafkamgt.uiapi.helpers.db.rdbms;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Objects;

@Service
@Slf4j
public class LoadDbJdbc implements ApplicationContextAware {

    @Autowired(required=false)
    private JdbcTemplate jdbcTemplate;

    @Value("${kafkawize.version:4.5.1}")
    private String kafkawizeVersion;

    @Value("${kafkawize.dbscripts.location}")
    private String scriptsLocation;

    @Value("${kafkawize.dbscripts.location.type:internal}")
    private String scriptsLocationType;

    @Value("${kafkawize.dbscripts.insert.basicdata.file:insertdata.sql}")
    private String basicInsertSqlFile;

    @Value("${kafkawize.dbscripts.location}")
    private String scriptsDefaultLocation;

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

        try{
            BufferedReader in;

            if(scriptsLocationType.equals("internal"))
                in = getReader(scriptsDefaultLocation + "ddl-jdbc.sql");
            else
                in = getReader(scriptsLocation + "ddl-jdbc.sql");

            String tmpLine;
            StringBuilder execQuery = new StringBuilder();
            while((tmpLine = Objects.requireNonNull(in).readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("create") && tmpLine.toLowerCase().endsWith(";")){
                    execQuery = new StringBuilder(tmpLine);
                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.toString().trim());
                    execQuery = new StringBuilder();
                }
                else if(tmpLine.toLowerCase().startsWith("create"))
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
            log.error("Could not create database tables. Shutting down. " + e.getMessage());
            if("create".equals("create"))
                shutdownApp();
        }
        log.info("Create DB Tables setup done !! ");
    }

    public void insertData(){
        insertData(basicInsertSqlFile);
    }

    private void insertData(String fileName){

        try{
            BufferedReader in ;
            if(scriptsLocationType.equals("internal"))
                in = getReader(scriptsDefaultLocation + fileName);
            else
                in = getReader(scriptsLocation + fileName);

            String tmpLine;
            StringBuilder execQuery = new StringBuilder();
            while((tmpLine=in.readLine())!=null){
                boolean isInsert = tmpLine.toLowerCase().startsWith("insert")
                        || tmpLine.toLowerCase().startsWith("update");
                if((isInsert) && tmpLine.toLowerCase().endsWith(";")){
                    execQuery = new StringBuilder(tmpLine);
                    log.info("Executing query : "+ execQuery);
                    try {
                        jdbcTemplate.execute(execQuery.toString().trim());
                    }catch (Exception sqlException){
                        if(!(sqlException instanceof DuplicateKeyException)){
                            throw sqlException;
                        }
                    }
                    execQuery = new StringBuilder();
                }
                else if(isInsert)
                    execQuery = new StringBuilder(tmpLine);
                else if(tmpLine.toLowerCase().endsWith(";"))
                {
                    execQuery.append(tmpLine);
//                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.toString().trim());
                    execQuery = new StringBuilder();
                }
                else{
                    execQuery.append(tmpLine);
                }
            }
        }catch (Exception e){
            log.error("Exiting .. could not insert data " + e.getMessage());
            shutdownApp();
        }
        log.info("Insert DB Tables setup done !! ");
    }

    private BufferedReader getReader(String sqlPath) throws IOException {
        if(scriptsLocationType.equals("internal")) {
            try {
                Resource resource;
                if(sqlPath.startsWith("file:///")){
                    resource = resourceLoader.getResource("classpath:" + sqlPath);
                }else{
                    File f = new File("");
                    log.info("File path" + f.getAbsolutePath());
                    resource = resourceLoader.getResource("classpath:" + sqlPath);
                }
                InputStream inputStream = resource.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                return new BufferedReader(inputStreamReader);
            }catch (Exception e){
                Resource resource;
                if(sqlPath.startsWith("file:///")){
                    resource = resourceLoader.getResource(sqlPath);
                }
                else{
                    File f = new File("");
                    log.info("File path" + f.getAbsolutePath());
                    resource = resourceLoader.getResource("file:///" + f.getAbsolutePath() + sqlPath);
                }
                InputStream inputStream = resource.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                return new BufferedReader(inputStreamReader);
            }
        }
        else
            return new BufferedReader(new FileReader(sqlPath));
    }
}
