package com.kafkamgt.uiapi.helpers.db.rdbms;

import com.datastax.driver.core.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class LoadDbJdbcTest {

    @Mock
    Session session;

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    LoadDbJdbc loadDbJdbc;

    @Before
    public void setUp() throws Exception {
        loadDbJdbc = new LoadDbJdbc(session);
    }

    @Test
    public void insertDataSucess() {
        ReflectionTestUtils.setField(loadDbJdbc, "INSERT_SQL", "src/main/resources/scripts/base/rdbms/insertdata.sql");
        loadDbJdbc.insertData();
    }

    @Test
    public void insertDataFailure() {
        ReflectionTestUtils.setField(loadDbJdbc, "INSERT_SQL", "testfile_notexists");
        exit.expectSystemExitWithStatus(0);
        loadDbJdbc.insertData();
    }
}