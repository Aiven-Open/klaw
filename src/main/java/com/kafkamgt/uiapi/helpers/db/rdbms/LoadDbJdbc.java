package com.kafkamgt.uiapi.helpers.db.rdbms;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLIntegrityConstraintViolationException;

@Service
@Slf4j
public class LoadDbJdbc {
    private static Logger LOG = LoggerFactory.getLogger(LoadDbJdbc.class);

    private static String CREATE_SQL = "src/main/resources/scripts/base/rdbms/ddl-jdbc.sql";

    private static String INSERT_SQL = "src/main/resources/scripts/base/rdbms/insertdata.sql";

    private static String DROP_SQL = "src/main/resources/scripts/base/rdbms/dropjdbc.sql";

    @Autowired(required=false)
    private JdbcTemplate jdbcTemplate;

    @Value("${custom.kafkawize.version:3.5}")
    private String kafkawizeVersion;

    public void createTables(){

        createTables("create");
        LOG.info("Create DB Tables setup done !! ");
        createTables("alter");
        LOG.info("Alter DB Tables setup done !! ");
    }

    private void createTables(String sqlType){

        String ALTER_SQL = "src/main/resources/scripts/base/rdbms/"+kafkawizeVersion+"_updates/alter.sql";
        try{
            FileReader fReader = null;
            if(sqlType.equals("create"))
                 fReader = new FileReader(CREATE_SQL);
            else if(sqlType.equals("alter"))
                fReader = new FileReader(ALTER_SQL);

            BufferedReader in = new BufferedReader(fReader);
            String tmpLine = "";
            String execQuery = "";
            while((tmpLine=in.readLine())!=null){
                if(tmpLine.toLowerCase().startsWith(sqlType) && tmpLine.toLowerCase().endsWith(";")){
                    execQuery = tmpLine;
                    log.info("Executing query : "+ execQuery);
                    jdbcTemplate.execute(execQuery.trim());
                    execQuery = "";
                }
                else if(tmpLine.toLowerCase().startsWith(sqlType))
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
        }
        catch (Exception e){
            LOG.error("Could not create/alter database tables " + e.getMessage());
            if(sqlType.equals("create"))
                System.exit(0);
        }
    }

    public void dropTables(){

        try (BufferedReader in = new BufferedReader(new FileReader(DROP_SQL))) {
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
            System.exit(0);
        }
        LOG.info("Drop DB Tables setup done !! ");
    }

    public void insertData(){

        try (BufferedReader in = new BufferedReader(new FileReader(INSERT_SQL))) {
            String tmpLine = "";
            String execQuery = "";
            while((tmpLine=in.readLine())!=null){
                if((tmpLine.toLowerCase().startsWith("insert")
                        || tmpLine.toLowerCase().startsWith("update")) && tmpLine.toLowerCase().endsWith(";")){
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
                else if(tmpLine.toLowerCase().startsWith("insert") || tmpLine.toLowerCase().startsWith("update"))
                    execQuery = tmpLine;
                else if(tmpLine.toLowerCase().endsWith(";"))
                {
                    execQuery = execQuery + tmpLine;
                    log.info("Executing query : "+ execQuery);
                    try {
                        jdbcTemplate.execute(execQuery.trim());
                    }catch (Exception sqlException){
                        if(sqlException instanceof SQLIntegrityConstraintViolationException){
                            //do nothing
                        }else throw sqlException;
                    }
                    execQuery = "";
                }
                else{
                    execQuery = execQuery + tmpLine;
                }

            }
        }catch (Exception e){
            LOG.error("Exiting .. could not setup create database tables " + e.getMessage());
            System.exit(0);
        }
        LOG.info("Insert DB Tables setup done !! ");
    }


}
