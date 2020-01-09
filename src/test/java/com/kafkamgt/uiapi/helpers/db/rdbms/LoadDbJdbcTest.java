package com.kafkamgt.uiapi.helpers.db.rdbms;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadDbJdbcTest {

    @Mock
    JdbcTemplate jdbcTemplate;

    private static String CREATE_SQL = "src/main/resources/scripts/base/rdbms/ddl-jdbc.sql";

    private static String INSERT_SQL = "src/main/resources/scripts/base/rdbms/insertdata.sql";

    private static String DROP_SQL = "src/main/resources/scripts/base/rdbms/dropjdbc.sql";

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    LoadDbJdbc loadDb;

    @Before
    public void setUp() throws Exception {
        loadDb = new LoadDbJdbc();
    }

    @Test
    public void createTables() {
        ReflectionTestUtils.setField(loadDb, "CREATE_SQL", CREATE_SQL);
        ReflectionTestUtils.setField(loadDb, "jdbcTemplate", jdbcTemplate);
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