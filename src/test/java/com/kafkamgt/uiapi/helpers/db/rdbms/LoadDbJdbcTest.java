package com.kafkamgt.uiapi.helpers.db.rdbms;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadDbJdbcTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    private ConfigurableApplicationContext contextApp;

    @Mock
    Resource resource;

    private static String CREATE_SQL = "scripts/base/rdbms/ddl-jdbc.sql";

    private static String INSERT_SQL = "scripts/base/rdbms/insertdata.sql";

    private static String DROP_SQL = "scripts/base/rdbms/dropjdbc.sql";

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    LoadDbJdbc loadDb;

    @Before
    public void setUp() throws Exception {
        loadDb = new LoadDbJdbc();
        ReflectionTestUtils.setField(loadDb, "resourceLoader", resourceLoader);
        ReflectionTestUtils.setField(loadDb, "contextApp", contextApp);
    }

    @Test
    public void createTables() {
        ReflectionTestUtils.setField(loadDb, "jdbcTemplate", jdbcTemplate);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(INSERT_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        doNothing().when(jdbcTemplate).execute(anyString());
        loadDb.createTables();
    }

    @Test
    public void createTablesSuccess1() {
        loadDb.createTables();
    }

    @Test
    public void insertData() {
        ReflectionTestUtils.setField(loadDb, "jdbcTemplate", jdbcTemplate);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(DROP_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        doNothing().when(jdbcTemplate).execute(anyString());
        loadDb.insertData();
    }

    @Test
    public void insertData2() {
        loadDb.insertData();
    }
}