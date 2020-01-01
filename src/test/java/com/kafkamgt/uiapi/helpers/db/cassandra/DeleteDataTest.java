package com.kafkamgt.uiapi.helpers.db.cassandra;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.Select;
import com.kafkamgt.uiapi.UtilMethods;
import com.kafkamgt.uiapi.dao.Acl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DeleteDataTest {

    private DeleteData deleteData;

    @Mock
    private Session session;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Row row;

    private UtilMethods utilMethods;

    @Before
    public void setUp(){
        deleteData = new DeleteData(session);
        utilMethods = new UtilMethods();
    }

    @Test
    public void deleteTopicRequest() {
        String topicName = "testtopic";
        String env = "DEV";

        String result = deleteData.deleteTopicRequest(topicName, env);
        assertEquals("success", result);
    }

    @Test
    public void deleteSchemaRequest() {
        String topicName = "testtopic", schemaVersion = "1.0", env="DEV";
        String result = deleteData.deleteSchemaRequest(topicName, schemaVersion, env);
        assertEquals("success", result);
    }

    @Test
    public void deleteAclRequest() {
        String result = deleteData.deleteAclRequest("32FSDt53G");
        assertEquals("success", result);
    }

    @Test
    public void deleteClusterRequest() {
        String result = deleteData.deleteClusterRequest("DEV");
        assertEquals("success", result);
    }

    @Test
    public void deleteUserRequest() {
        String result = deleteData.deleteUserRequest("uiuser1");
        assertEquals("success", result);
    }

    @Test
    public void deleteTeamRequest() {
        String result = deleteData.deleteTeamRequest("Team1");
        assertEquals("success", result);
    }

    @Test
    public void deletePrevAclRecs() {
        List<Acl > aclReqs = utilMethods.getAcls();
        List<Row> rowList = new ArrayList<>();
        rowList.add(row);

        when(session.execute((Select) any())).thenReturn(resultSet);
        when(resultSet.iterator()).thenReturn(rowList.iterator());
        when(row.getString("req_no")).thenReturn("rwe42FSDE");

        String result = deleteData.deletePrevAclRecs(aclReqs);
        assertEquals("success", result);
    }
}