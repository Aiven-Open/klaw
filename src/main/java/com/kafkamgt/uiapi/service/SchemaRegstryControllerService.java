package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SchemaRegstryControllerService {

    @Autowired
    ManageDatabase manageDatabase;

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
        UserDetails userDetails = getUserDetails();
        return manageDatabase.getHandleDbRequests().getAllSchemaRequests(userDetails.getUsername());
    }

    public List<SchemaRequest> getCreatedSchemaRequests() {
        UserDetails userDetails = getUserDetails();
        List<SchemaRequest> schemaReqs = manageDatabase.getHandleDbRequests().getCreatedSchemaRequests(userDetails.getUsername());

        schemaReqs = schemaReqs.stream().sorted(Comparator.comparing(SchemaRequest::getRequesttime)).collect(Collectors.toList());
        return schemaReqs;
    }

     public String deleteSchemaRequests(String topicName) {
        try{
        StringTokenizer strTkr = new StringTokenizer(topicName,"-----");
            topicName = strTkr.nextToken();
            String schemaVersion = strTkr.nextToken();
            String env = strTkr.nextToken();

            return manageDatabase.getHandleDbRequests().deleteSchemaRequest(topicName,schemaVersion, env);
        }catch (Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }

    public String execSchemaRequests(String topicName, String env) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        SchemaRequest schemaRequest = manageDatabase.getHandleDbRequests().selectSchemaRequest(topicName,"1.0", env);

        ResponseEntity<String> response = clusterApiService.postSchema(schemaRequest, env, topicName);

        if(response.getBody().contains("id\":")) {
            try {
                return manageDatabase.getHandleDbRequests().updateSchemaRequest(schemaRequest, userDetails.getUsername());
            }catch (Exception e){
                return e.getMessage();
            }
        }
        else {
            return "Failure in uploading schema" ;
        }
    }

    public String execSchemaRequestsDecline(String topicName, String env) {
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedAdmin(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        SchemaRequest schemaRequest = manageDatabase.getHandleDbRequests().selectSchemaRequest(topicName,"1.0", env);

        return  manageDatabase.getHandleDbRequests().updateSchemaRequestDecline(schemaRequest, userDetails.getUsername());

    }

    public String uploadSchema(SchemaRequest schemaRequest){
        UserDetails userDetails = getUserDetails();
        schemaRequest.setUsername(userDetails.getUsername());
        try {
            return manageDatabase.getHandleDbRequests().requestForSchema(schemaRequest);
        }catch (Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }

    private UserDetails getUserDetails(){
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
