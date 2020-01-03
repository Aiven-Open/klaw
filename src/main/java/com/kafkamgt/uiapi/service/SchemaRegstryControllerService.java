package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.StringTokenizer;

@Service
@Slf4j
public class SchemaRegstryControllerService {

    private HandleDbRequests handleDbRequests = ManageDatabase.handleDbRequests;

    @Autowired
    ClusterApiService clusterApiService;

    @Autowired
    private UtilService utilService;

    public SchemaRegstryControllerService(ClusterApiService clusterApiService,
                                          UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.utilService = utilService;
    }

    public List<SchemaRequest> getSchemaRequests() {
        return handleDbRequests.getAllSchemaRequests(utilService.getUserName());
    }

    public List<SchemaRequest> getCreatedSchemaRequests() {

        return handleDbRequests.getCreatedSchemaRequests(utilService.getUserName());
    }

     public String deleteSchemaRequests(String topicName) {

        try{
        StringTokenizer strTkr = new StringTokenizer(topicName,"-----");
            topicName = strTkr.nextToken();
            String schemaVersion = strTkr.nextToken();
            String env = strTkr.nextToken();

            return handleDbRequests.deleteSchemaRequest(topicName,schemaVersion, env);
        }catch (Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }

    public String execSchemaRequests(String topicName) throws KafkawizeException {

        StringTokenizer strTkr = new StringTokenizer(topicName,"-----");
        topicName = strTkr.nextToken();
        String schemaversion = strTkr.nextToken();
        String env = strTkr.nextToken();

        SchemaRequest schemaRequest = handleDbRequests.selectSchemaRequest(topicName,schemaversion,env);

        ResponseEntity<String> response = clusterApiService.postSchema(schemaRequest, env, topicName);

        if(response.getBody().contains("id\":")) {
            try {
                return handleDbRequests.updateSchemaRequest(schemaRequest, utilService.getUserName());
            }catch (Exception e){
                return "{\"result\":\"failure "+e.toString()+"\"}";
            }
        }
        else {
            return "Failure in uploading schema" ;
        }
    }

    public String uploadSchema(SchemaRequest schemaRequest){

        schemaRequest.setUsername(utilService.getUserName());
        try {
            return handleDbRequests.requestForSchema(schemaRequest);
        }catch (Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }
}
