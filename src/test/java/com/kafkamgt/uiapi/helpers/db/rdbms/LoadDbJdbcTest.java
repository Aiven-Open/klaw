package com.kafkamgt.uiapi.helpers.db.rdbms;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(SpringExtension.class)
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

//    @Rule
//    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    LoadDbJdbc loadDb;

    @BeforeEach
    public void setUp() throws Exception {
        loadDb = new LoadDbJdbc();
        ReflectionTestUtils.setField(loadDb, "resourceLoader", resourceLoader);
    }

    @Test
    public void createTables() {
        ReflectionTestUtils.setField(loadDb, "jdbcTemplate", jdbcTemplate);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(INSERT_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        doNothing().when(jdbcTemplate).execute(anyString());
    }

    @Test
    public void dropTables() {
        ReflectionTestUtils.setField(loadDb, "jdbcTemplate", jdbcTemplate);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(INSERT_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        doNothing().when(jdbcTemplate).execute(anyString());
    }

    @Test
    public void insertData() {
        ReflectionTestUtils.setField(loadDb, "jdbcTemplate", jdbcTemplate);
        when(resourceLoader.getResource(anyString())).thenReturn(resource, resource);
        try {
            InputStream inputStream = new ClassPathResource(DROP_SQL).getInputStream();
            when(resource.getInputStream()).thenReturn(inputStream, inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        doNothing().when(jdbcTemplate).execute(anyString());
        loadDb.insertData();
    }

}