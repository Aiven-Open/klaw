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
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class LoadDbJdbc implements ApplicationContextAware {

    @Autowired(required=false)
    private JdbcTemplate jdbcTemplate;

    @Value("${kafkawize.version:1.0.1}")
    private String kafkawizeVersion;

    @Value("${kafkawize.dbscripts.location}")
    private String scriptsLocation;

    @Value("${kafkawize.dbscripts.location.type:internal}")
    private String scriptsLocationType;

//    @Value("${kafkawize.dbscripts.insert.basicdata.file:basicinsertdata.sql}")
//    private String basicInsertSqlFile;

    String scriptsDefaultLocation = "scripts/base/rdbms/";

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

        createTables("ddl-jdbc.sql");
        log.debug("Create DB Tables setup done !! ");
//        createTables("alter");
//        log.debug("Alter DB Tables setup done !! ");
    }

    private void createTables(String fileName){

        try{
            BufferedReader in;

            if(scriptsLocationType.equals("internal"))
                in = getReader(scriptsDefaultLocation + fileName);
            else
                in = getReader(scriptsLocation + fileName);

//            else if(sqlType.equals("alter"))
//                in = getReader(ALTER_SQL);

            String tmpLine;
            StringBuilder execQuery = new StringBuilder();
            while((tmpLine = Objects.requireNonNull(in).readLine())!=null){
                if(tmpLine.toLowerCase().startsWith("create") && tmpLine.toLowerCase().endsWith(";")){
                    execQuery = new StringBuilder(tmpLine);
                    log.debug("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.toString().trim());
                    execQuery = new StringBuilder();
                }
                else if(tmpLine.toLowerCase().startsWith("create"))
                    execQuery = new StringBuilder(tmpLine);
                else if(tmpLine.toLowerCase().endsWith(";"))
                {
                    execQuery.append(tmpLine);
                    log.debug("Executing query : "+ execQuery);
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
    }

    public void insertData(){
//        if(ifDataAlreadyExists())
//            return;
//        insertData(basicInsertSqlFile);
    }

    private boolean ifDataAlreadyExists() {
        String selectQuery = "select * from kwproductdetails where version='" + kafkawizeVersion + "'";
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList(selectQuery);
        return mapList.size() != 0;
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
                    log.debug("Executing query : "+ execQuery);
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
//                    log.debug("Executing query : "+ execQuery);
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
        log.debug("Insert DB Tables setup done !! ");
    }

    private BufferedReader getReader(String sql) throws IOException {
        if(scriptsLocationType.equals("internal")) {
            Resource resource = resourceLoader.getResource("classpath:" + sql);
            InputStream inputStream = resource.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            return new BufferedReader(inputStreamReader);
        }
        else
            return new BufferedReader(new FileReader(sql));
    }
}
