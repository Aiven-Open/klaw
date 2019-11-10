package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
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

    @Autowired
    ManageTopics createTopicHelper;

    @Value("${clusterapi.url}")
    String clusterConnUrl;

    @Value("${clusterapi.username}")
    String clusterApiUser;

    @Value("${clusterapi.password}")
    String clusterApiPwd;

    private String uriCreateAcls = "/topics/createAcls";

    private String uriGetAcls = "/topics/getAcls/";

    String uriCreateTopics = "/topics/createTopics";

    String uriGetTopics = "/topics/getTopics/";

    String uriPostSchema = "/topics/postSchema";

    public List<HashMap<String,String>> getAcls(String bootstrapHost) throws KafkawizeException {
        List<HashMap<String, String>> aclListOriginal = null;
        try {
            String uri = clusterConnUrl + uriGetAcls + bootstrapHost;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<HashMap<String, String>>> entity = new HttpEntity<>(headers);

            ResponseEntity<Set> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, Set.class);
            aclListOriginal = new ArrayList(resultBody.getBody());
        }catch(Exception e){
            throw new KafkawizeException("Could not load acls. Check Cluster Api connection. "+e.toString());
        }
        return aclListOriginal;
    }

    public List<String> getAllTopics(String bootstrapHost) throws Exception{
        List<String> topicsList = null;
        try {
            String uriGetTopicsFull = clusterConnUrl + uriGetTopics + bootstrapHost;
            RestTemplate restTemplate = new RestTemplate();

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
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            Env envSelected = createTopicHelper.selectEnvDetails(topicRequest.getEnvironment());
            String bootstrapHost = envSelected.getHost() + ":" + envSelected.getPort();
            params.add("env", bootstrapHost);

            params.add("topicName", topicName);
            params.add("partitions", topicRequest.getTopicpartitions());
            params.add("rf", topicRequest.getReplicationfactor());
//            params.add("acl_ip", topicRequest.getAcl_ip());
//            params.add("acl_ssl", topicRequest.getAcl_ssl());

            HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            response = restTemplate.postForEntity(clusterConnUrl + uriCreateTopics, request, String.class);
        }catch(Exception e){
            throw new KafkawizeException("Could not approve topic request. Check Cluster Api connection. "+e.toString());
        }
        return response;
    }

    public ResponseEntity<String> approveAclRequests(AclRequests aclReq) throws KafkawizeException {
        ResponseEntity<String> response;
        try {
            String env = aclReq.getEnvironment();
            String uri = clusterConnUrl + uriCreateAcls;
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

            Env envSelected = createTopicHelper.selectEnvDetails(env);
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
            String uri = clusterConnUrl + uriPostSchema;

            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();

            Env envSelected = createTopicHelper.selectEnvDetails(env);
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

    HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {{
            String auth = username + ":" + password;
            byte[] encodedAuth = Base64.encodeBase64(
                    auth.getBytes(Charset.forName("US-ASCII")));
            String authHeader = "Basic " + new String(encodedAuth);
            set("Authorization", authHeader);
        }};
    }
}
