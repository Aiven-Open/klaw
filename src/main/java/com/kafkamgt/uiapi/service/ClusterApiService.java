package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
public class ClusterApiService {

    private HandleDbRequests handleDbRequests = ManageDatabase.handleDbRequests;

    @Autowired
    private UtilService utilService;

    @Value("${custom.clusterapi.url}")
    private String clusterConnUrl;

    @Value("${custom.clusterapi.username}")
    private String clusterApiUser;

    @Value("${custom.clusterapi.password}")
    private String clusterApiPwd;

    private static String URI_CREATE_ACLS = "/topics/createAcls";

    private static String URI_GET_ACLS = "/topics/getAcls/";

    private static String URI_CREATE_TOPICS = "/topics/createTopics";

    private static String URI_GET_TOPICS = "/topics/getTopics/";

    private static String URI_POST_SCHEMA = "/topics/postSchema";

    private static String URI_ENV_STATUS = "/topics/getStatus/";

    private static String URI_CLSTR_API_STAUTS = "/topics/getApiStatus";

    private static String CLSTR_API_STATUS = "OFFLINE";

    public ClusterApiService(UtilService utilService){
        this.utilService = utilService;
    }

    public String getClusterApiUrl(){
        return this.clusterConnUrl;
    }

    public String getClusterApiStatus() {
        String clusterStatus;
        try {
            String uri = clusterConnUrl + URI_CLSTR_API_STAUTS;
            RestTemplate restTemplate = utilService.getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, String.class);
            clusterStatus = resultBody.getBody();
        }catch(Exception e){
            this.CLSTR_API_STATUS = "OFFLINE";
            return "OFFLINE";
        }
        this.CLSTR_API_STATUS = clusterStatus;
        return clusterStatus;
    }

    public String getSchemaClusterStatus(String host) {
        String clusterStatus = null;
        try {
            String uri = host+"/subjects";
            RestTemplate restTemplate = utilService.getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, String.class);
            clusterStatus = resultBody.getBody();
        }catch(Exception e){
            return "OFFLINE";
        }
        return clusterStatus;
    }

    public String getKafkaClusterStatus(String bootstrapHost) {
        String clusterStatus ;

        try {
            String uri = clusterConnUrl + URI_ENV_STATUS + bootstrapHost;
            RestTemplate restTemplate = utilService.getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, String.class);
            clusterStatus = resultBody.getBody();
        }catch(Exception e){
            return "NOT_KNOWN";
        }
        return clusterStatus;
    }

    public List<HashMap<String,String>> getAcls(String bootstrapHost) throws KafkawizeException {
        List<HashMap<String, String>> aclListOriginal = null;
        try {
            String uri = clusterConnUrl + URI_GET_ACLS + bootstrapHost;
            RestTemplate restTemplate = utilService.getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<HashMap<String, String>>> entity = new HttpEntity<>(headers);

            ResponseEntity<Set> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, Set.class);
            aclListOriginal = new ArrayList(resultBody.getBody());
        }catch(Exception e){
            throw new KafkawizeException("Could not load topics/acls. Check Cluster Api connection. "+e.toString());
        }
        return aclListOriginal;
    }

    public List<String> getAllTopics(String bootstrapHost) throws Exception{
        List<String> topicsList ;
        try {
            String uriGetTopicsFull = clusterConnUrl + URI_GET_TOPICS + bootstrapHost;
            RestTemplate restTemplate = utilService.getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<String>> entity = new HttpEntity<>(headers);

            ResponseEntity<Set> s = restTemplate.exchange
                    (uriGetTopicsFull, HttpMethod.GET, entity, Set.class);

            topicsList = new ArrayList(s.getBody());
        }catch(Exception e){
            throw new KafkawizeException("Could not load topics. Check Cluster Api connection. "+e.toString());
        }

        return topicsList;
    }

    public ResponseEntity<String> approveTopicRequests(String topicName, TopicRequest topicRequest) throws KafkawizeException {
        ResponseEntity<String> response;
        try {
            RestTemplate restTemplate = utilService.getRestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            Env envSelected = handleDbRequests.selectEnvDetails(topicRequest.getEnvironment());
            String bootstrapHost = envSelected.getHost() + ":" + envSelected.getPort();
            params.add("env", bootstrapHost);

            params.add("topicName", topicName);
            params.add("partitions", topicRequest.getTopicpartitions());
            params.add("rf", topicRequest.getReplicationfactor());

            HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            response = restTemplate.postForEntity(clusterConnUrl + URI_CREATE_TOPICS, request, String.class);
        }catch(Exception e){
            throw new KafkawizeException("Could not approve topic request. Check Cluster Api connection. "+e.toString());
        }
        return response;
    }

    public ResponseEntity<String> approveAclRequests(AclRequests aclReq) throws KafkawizeException {
        ResponseEntity<String> response;
        try {
            String env = aclReq.getEnvironment();
            String uri = clusterConnUrl + URI_CREATE_ACLS;
            RestTemplate restTemplate = utilService.getRestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

            Env envSelected = handleDbRequests.selectEnvDetails(env);
            String bootstrapHost = envSelected.getHost() + ":" + envSelected.getPort();
            params.add("env", bootstrapHost);
            params.add("topicName", aclReq.getTopicname());
            params.add("consumerGroup", aclReq.getConsumergroup());
            params.add("aclType", aclReq.getTopictype());
            params.add("acl_ip", aclReq.getAcl_ip());
            params.add("acl_ssl", aclReq.getAcl_ssl());

            HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            response = restTemplate.postForEntity(uri, request, String.class);
        }catch(Exception e){
            throw new KafkawizeException("Could not approve acl request. Check Cluster Api connection. "+e.toString());
        }
        return response;
    }

    public ResponseEntity<String> postSchema(SchemaRequest schemaRequest, String env, String topicName) throws KafkawizeException {
        ResponseEntity<String> response;
        try {
            String uri = clusterConnUrl + URI_POST_SCHEMA;

            RestTemplate restTemplate = utilService.getRestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

            Env envSelected = handleDbRequests.selectEnvDetails(env);
            String bootstrapHost = envSelected.getHost() + ":" + envSelected.getPort();
            params.add("env", bootstrapHost);

            params.add("topicName", topicName);
            params.add("fullSchema", schemaRequest.getSchemafull());

            HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            response = restTemplate.postForEntity(uri, request, String.class);
        }catch(Exception e){
            throw new KafkawizeException("Could not post schema. Check Cluster Api connection. "+e.toString());
        }
        return response;
    }

    private HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }
}
