package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadDbTest {

    @Mock
    Session session;

    @Mock
    ResultSet resultSet;

    @Mock
    ResourceLoader resourceLoader;

    @Mock
    Resource resource;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    LoadDb loadDb;

    private static String CREATE_SQL = "scripts/base/cassandra/createcassandra.sql";

    private static String INSERT_SQL = "scripts/base/cassandra/insertdata.sql";

    private static String DROP_SQL = "scripts/base/cassandra/dropcassandra.sql";

    @Before
    public void setUp() throws Exception {
        loadDb = new LoadDb(session);
        ReflectionTestUtils.setField(loadDb, "resourceLoader", resourceLoader);
    }

    @Test
    public void createTablesSuccess() {
        ReflectionTestUtils.setField(loadDb, "CREATE_SQL", CREATE_SQL);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(CREATE_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadDb.createTables();
    }



    @Test
    public void insertDataSuccess() {
        ReflectionTestUtils.setField(loadDb, "INSERT_SQL", INSERT_SQL);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(INSERT_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadDb.insertData();
    }



    @Test
    public void dropTablesSuccess() {
        ReflectionTestUtils.setField(loadDb, "DROP_SQL", DROP_SQL);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(DROP_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadDb.dropTables();
    }


}