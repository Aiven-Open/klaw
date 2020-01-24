package com.kafkamgt.uiapi.helpers.db.rdbms;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
    }

    @Test
    public void createTables() {
        ReflectionTestUtils.setField(loadDb, "CREATE_SQL", CREATE_SQL);
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
    public void createTablesFail() {
        ReflectionTestUtils.setField(loadDb, "CREATE_SQL", CREATE_SQL);
        exit.expectSystemExitWithStatus(0);
        loadDb.createTables();
    }

    @Test
    public void dropTables() {
        ReflectionTestUtils.setField(loadDb, "INSERT_SQL", INSERT_SQL);
        ReflectionTestUtils.setField(loadDb, "jdbcTemplate", jdbcTemplate);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(INSERT_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        doNothing().when(jdbcTemplate).execute(anyString());
        loadDb.dropTables();
    }

    @Test
    public void dropTablesFail() {
        ReflectionTestUtils.setField(loadDb, "INSERT_SQL", INSERT_SQL);
        exit.expectSystemExitWithStatus(0);
        loadDb.dropTables();
    }

    @Test
    public void insertData() {
        ReflectionTestUtils.setField(loadDb, "DROP_SQL", DROP_SQL);
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
    public void insertDataFail() {
        ReflectionTestUtils.setField(loadDb, "DROP_SQL", DROP_SQL);
        exit.expectSystemExitWithStatus(0);
        loadDb.insertData();
    }
}