package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.dao.SchemaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringTokenizer;

@Service
public class SchemaRegstryControllerService {

    private static Logger LOG = LoggerFactory.getLogger(SchemaRegstryControllerService.class);

    @Autowired
    ManageTopics createTopicHelper;

    @Autowired
    ClusterApiService clusterApiService;

    @Autowired
    private UtilService utilService;

    public List<SchemaRequest> getSchemaRequests() {
        return createTopicHelper.getAllSchemaRequests(utilService.getUserName());
    }

    public List<SchemaRequest> getCreatedSchemaRequests() {

        return createTopicHelper.getCreatedSchemaRequests(utilService.getUserName());
    }

     public String deleteSchemaRequests(String topicName) {

        StringTokenizer strTkr = new StringTokenizer(topicName,"-----");
        topicName = strTkr.nextToken();
        String schemaVersion = strTkr.nextToken();
        String env = strTkr.nextToken();

        return createTopicHelper.deleteSchemaRequest(topicName,schemaVersion, env);
    }

    public String execSchemaRequests(String topicName) {

        StringTokenizer strTkr = new StringTokenizer(topicName,"-----");
        topicName = strTkr.nextToken();
        String schemaversion = strTkr.nextToken();
        String env = strTkr.nextToken();

        SchemaRequest schemaRequest = createTopicHelper.selectSchemaRequest(topicName,schemaversion,env);

        ResponseEntity<String> response = clusterApiService.postSchema(schemaRequest, env, topicName);

        if(response.getBody().contains("id\":")) {
            return createTopicHelper.updateSchemaRequest(schemaRequest, utilService.getUserName());
        }
        else {
            return "Failure in uploading schema" ;
        }
    }

    public String uploadSchema(SchemaRequest schemaRequest){

        LOG.info(schemaRequest.getTopicname()+ "---" + schemaRequest.getTeamname()+"---"+schemaRequest.getEnvironment() +
                "---"+schemaRequest.getAppname()+"---"+
                schemaRequest.getTeamname());
        schemaRequest.setUsername(utilService.getUserName());

        return createTopicHelper.requestForSchema(schemaRequest);
    }
}
