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
import org.springframework.test.util.ReflectionTestUtils;

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

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    LoadDb loadDb;

    @Before
    public void setUp() throws Exception {
        loadDb = new LoadDb(session);
    }

    @Test
    public void createTablesSuccess() {
        ReflectionTestUtils.setField(loadDb, "CREATE_SQL",
                "src/main/resources/scripts/base/cassandra/createcassandra.sql");

        loadDb.createTables();
    }

    @Test
    public void createTablesFailure() {
        ReflectionTestUtils.setField(loadDb, "CREATE_SQL", "testfile_notexists");
        exit.expectSystemExitWithStatus(0);
        loadDb.createTables();
    }

    @Test
    public void insertDataSuccess() {
        ReflectionTestUtils.setField(loadDb, "INSERT_SQL",
                "src/main/resources/scripts/base/cassandra/insertdata.sql");
        loadDb.insertData();
    }

    @Test
    public void insertDataFailure() {
        ReflectionTestUtils.setField(loadDb, "INSERT_SQL", "testfile_notexists");
        exit.expectSystemExitWithStatus(0);
        loadDb.insertData();
    }
}