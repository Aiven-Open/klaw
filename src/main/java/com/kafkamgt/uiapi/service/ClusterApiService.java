package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.tomcat.util.codec.binary.Base64;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;
import static com.kafkamgt.uiapi.service.KwConstants.CLUSTER_CONN_URL_KEY;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
@Slf4j
public class ClusterApiService {

    @Autowired
    ManageDatabase manageDatabase;

    @Value("${server.ssl.trust-store:null}")
    private String trustStore;

    @Value("${server.ssl.trust-store-password:null}")
    private String trustStorePwd;

    @Value("${server.ssl.key-store:null}")
    private String keyStore;

    @Value("${server.ssl.key-store-password:null}")
    private String keyStorePwd;

    @Value("${server.ssl.key-store-type:JKS}")
    private String keyStoreType;

    protected static HttpComponentsClientHttpRequestFactory requestFactory;

    static String URI_CREATE_TOPICS = "/topics/createTopics";
    static String URI_UPDATE_TOPICS = "/topics/updateTopics";
    static String URI_DELETE_TOPICS = "/topics/deleteTopics";

    @Value("${kafkawize.jasypt.encryptor.secretkey}")
    private String encryptorSecretKey;

    @Value("${kafkawize.clusterapi.access.username}")
    private String clusterApiUser;

    @Value("${kafkawize.clusterapi.access.password}")
    private String clusterApiPwd;

    private static String clusterConnUrl;

    public String getClusterApiUrl(){
        return clusterConnUrl;
    }

    RestTemplate httpRestTemplate, httpsRestTemplate;

    private RestTemplate getRestTemplate(){
        if(clusterConnUrl.toLowerCase().startsWith("https")) {
            if(this.httpsRestTemplate == null){
                this.httpsRestTemplate = new RestTemplate(requestFactory);
            }
            return this.httpsRestTemplate;
        }
        else {
            if(this.httpRestTemplate == null) {
                this.httpRestTemplate = new RestTemplate();
            }
            return this.httpRestTemplate;
        }
    }

    private void getClusterApiProperties(int tenantId){
        clusterConnUrl = manageDatabase.getKwPropertyValue(CLUSTER_CONN_URL_KEY, tenantId);
    }

    public String getClusterApiStatus(String clusterApiUrl, boolean testConnection, int tenantId) {
        log.info("getClusterApiStatus clusterApiUrl {} testConnection{}",clusterApiUrl, testConnection );
        getClusterApiProperties(tenantId);
        String clusterStatus;
        try {
            String URI_CLSTR_API_STAUTS = "/topics/getApiStatus";
            String uri ;
            if(testConnection)
                uri = clusterApiUrl + URI_CLSTR_API_STAUTS;
            else
                uri = clusterConnUrl + URI_CLSTR_API_STAUTS; // from stored kw props

            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, String.class);
            clusterStatus = resultBody.getBody();
        }catch(Exception e){
            log.error("Error from getClusterApiStatus {}", e.toString());
            return "OFFLINE";
        }
        return clusterStatus;
    }

    String getSchemaClusterStatus(String host, int tenantId) {
        log.info("getSchemaClusterStatus {}", host);
        getClusterApiProperties(tenantId);
        String clusterStatus ;
        try {
            String uri = host+"/subjects";
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, String.class);
            clusterStatus = resultBody.getBody();
        }catch(Exception e){
            log.error("Error from getSchemaClusterStatus {}", e.toString());
            return "OFFLINE";
        }
        return clusterStatus;
    }

    String getKafkaClusterStatus(String bootstrapHost, String protocol, String clusterName, String clusterType, int tenantId) {
        log.debug("getKafkaClusterStatus {} {}", bootstrapHost, protocol);
        String clusterStatus ;
        getClusterApiProperties(tenantId);

        try {
            String URI_ENV_STATUS = "/topics/getStatus/";
            String uri = clusterConnUrl + URI_ENV_STATUS + bootstrapHost + "/" + protocol + "/" + clusterName + "-" + tenantId +
                    "/" + clusterType;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, String.class);
            clusterStatus = resultBody.getBody();
        }catch(Exception e){
            log.error("Error from getKafkaClusterStatus {}", e.toString());
            return "NOT_KNOWN";
        }
        return clusterStatus;
    }

    public List<HashMap<String, String>> getConsumerOffsets(String bootstrapHost, String protocol, String clusterName,
                                                            String topic, String consumerGroupId, int tenantId) throws KafkawizeException {
        log.info("getConsumerOffsets {} {} {} {}", bootstrapHost, protocol, topic, consumerGroupId);
        getClusterApiProperties(tenantId);
        List<HashMap<String, String>> offsetsMap;
        try {
            String url = "/topics/getConsumerOffsets/";
            url = clusterConnUrl + url + bootstrapHost + "/" + protocol + "/" + clusterName + "-" + tenantId +
                    "/" + consumerGroupId + "/" + topic;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<List<HashMap<String, String>>> entity = new HttpEntity<>(headers);
            ResponseEntity<List> resultBody = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

            offsetsMap = new ArrayList<>(Objects.requireNonNull(resultBody.getBody()));
        }catch (Exception e){
            log.error("Error from getConsumerOffsets {}", e.toString());
            throw new KafkawizeException("Could not get consumer offsets");
        }
        return  offsetsMap;
    }

    public Map<String, String> getTopicEvents(String bootstrapHost, String protocol, String clusterName, String topic,
                                            String offsetId, String consumerGroupId, int tenantId) throws KafkawizeException {
        log.info("getTopicEvents {} {} {} {} {}", bootstrapHost, protocol, topic, offsetId, consumerGroupId);
        getClusterApiProperties(tenantId);
        Map<String, String> eventsMap;
        try {
            String url = "/topics/getTopicContents/";
            url = clusterConnUrl + url + bootstrapHost + "/" + protocol + "/" + clusterName + "-" + tenantId +
                    "/" + consumerGroupId + "/" + topic +
             "/" + offsetId;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> resultBody = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            eventsMap = new TreeMap<>(Objects.requireNonNull(resultBody.getBody()));
        }catch (Exception e){
            log.error("Error from getTopicEvents {} {}", e.toString(), topic);
            throw new KafkawizeException("Could not get events for Topic " + topic);
        }
        return eventsMap;
    }

    public List<HashMap<String,String>> getAcls(String bootstrapHost, String protocol, String clusterName, int tenantId) throws KafkawizeException {
        log.info("getAcls {} {} {}", bootstrapHost, protocol, tenantId);
        getClusterApiProperties(tenantId);

        List<HashMap<String, String>> aclListOriginal;
        try {
            String URI_GET_ACLS = "/topics/getAcls/";
            String uri = clusterConnUrl + URI_GET_ACLS + bootstrapHost + "/" + protocol + "/" + clusterName + "-" + tenantId;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<HashMap<String, String>>> entity = new HttpEntity<>(headers);

            ResponseEntity<Set> resultBody = restTemplate.exchange
                    (uri, HttpMethod.GET, entity, Set.class);
            aclListOriginal = new ArrayList(Objects.requireNonNull(resultBody.getBody()));
        }catch(Exception e){
            log.error("Error from getAcls {}", e.toString());
            throw new KafkawizeException("Could not load topics/acls. Please contact Administrator.");
        }
        return aclListOriginal;
    }

    public List<HashMap<String, String>> getAllTopics(String bootstrapHost, String protocol, String clusterName, int tenantId) throws Exception{
        log.info("getAllTopics {} {}", bootstrapHost, protocol);
        getClusterApiProperties(tenantId);
        List<HashMap<String, String>> topicsList ;
        try {
            String URI_GET_TOPICS = "/topics/getTopics/";
            String uriGetTopicsFull = clusterConnUrl + URI_GET_TOPICS + bootstrapHost + "/" + protocol + "/" + clusterName + "-" + tenantId;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<String>> entity = new HttpEntity<>(headers);

            ResponseEntity<Set> s = restTemplate.exchange
                    (uriGetTopicsFull, HttpMethod.GET, entity, Set.class);

            topicsList = new ArrayList(Objects.requireNonNull(s.getBody()));
        }catch(Exception e){
            log.error("Error from getAllTopics {}", e.toString());
            throw new KafkawizeException("Could not load topics. Please contact Administrator.");
        }

        return topicsList;
    }

    public String approveConnectorRequests(String connectorName, String connectorType, String connectorConfig,
                                           String kafkaConnectHost, int tenantId) throws KafkawizeException {
        log.info("approveConnectorRequests {} {}", connectorConfig, kafkaConnectHost);
        getClusterApiProperties(tenantId);
        ResponseEntity<HashMap> response;
        try {
            RestTemplate restTemplate = getRestTemplate();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("env", kafkaConnectHost);
            params.add("connectorName", connectorName);
            params.add("connectorConfig", connectorConfig);

            String uri;
            String URI_GET_TOPICS = "/topics/";

            if(connectorType.equals("Create")){
                uri = clusterConnUrl + URI_GET_TOPICS + "postConnector";
            }
            else if(connectorType.equals("Update")) {
                uri = clusterConnUrl + URI_GET_TOPICS + "updateConnector";
            }
            else
                uri = clusterConnUrl + URI_GET_TOPICS + "deleteConnector";

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            response = restTemplate.postForEntity(uri, request, HashMap.class);

            if(response.getBody().get("result").equals("success")){
                return "success";
            }else{
                return (String)response.getBody().get("errorText");
            }

        }catch(Exception e){
            log.error("approveConnectorRequests {} {}",connectorName, e.getMessage());
            throw new KafkawizeException("Could not approve connector request. Please contact Administrator.");
        }
    }

    public ResponseEntity<String> approveTopicRequests(String topicName, String topicRequestType,
                                                       int topicPartitions, String replicationFactor, String topicEnvId, int tenantId) throws KafkawizeException {
        log.info("approveTopicRequests {} {}", topicName, topicEnvId);
        getClusterApiProperties(tenantId);
        ResponseEntity<String> response;
        try {
            RestTemplate restTemplate = getRestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(topicEnvId, tenantId);
            String bootstrapHost = manageDatabase.getClusters("kafka", tenantId)
                    .get(envSelected.getClusterId()).getBootstrapServers();
            params.add("env", bootstrapHost);
            params.add("protocol", manageDatabase.getClusters("kafka", tenantId)
                    .get(envSelected.getClusterId()).getProtocol());
            params.add("clusterName", manageDatabase.getClusters("kafka", tenantId)
                    .get(envSelected.getClusterId()).getClusterName() + "-" + tenantId);
            params.add("topicName", topicName);

            String uri;
            if(topicRequestType.equals("Create")) {
                uri = clusterConnUrl + URI_CREATE_TOPICS;
                params.add("partitions", "" + topicPartitions);
                params.add("rf", replicationFactor);
            }
            else if(topicRequestType.equals("Update")) {
                uri = clusterConnUrl + URI_UPDATE_TOPICS;
                params.add("partitions", "" + topicPartitions);
                params.add("rf", replicationFactor);
            }
            else
                uri = clusterConnUrl + URI_DELETE_TOPICS;

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            response = restTemplate.postForEntity(uri, request, String.class);
        }catch(Exception e){
            log.error("approveTopicRequests {} {}",topicName, e.getMessage());
            throw new KafkawizeException("Could not approve topic request. Please contact Administrator.");
        }
        return response;
    }

    public ResponseEntity<String> approveAclRequests(AclRequests aclReq, int tenantId) throws KafkawizeException {
        log.info("approveAclRequests {}", aclReq);
        getClusterApiProperties(tenantId);
        ResponseEntity<String> response;
        try {
            String env = aclReq.getEnvironment();
            String uri;

            String URI_CREATE_ACLS = "/topics/createAcls";
            String URI_DELETE_ACLS = "/topics/deleteAcls";

            if(aclReq.getAclType().equals("Create"))
                uri = clusterConnUrl + URI_CREATE_ACLS;
            else
                uri = clusterConnUrl + URI_DELETE_ACLS;

            RestTemplate restTemplate = getRestTemplate();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(env, tenantId);
            String bootstrapHost = manageDatabase.getClusters("kafka", tenantId)
                    .get(envSelected.getClusterId()).getBootstrapServers();
            params.add("env", bootstrapHost);
            params.add("protocol", manageDatabase.getClusters("kafka", tenantId)
                    .get(envSelected.getClusterId()).getProtocol());
            params.add("clusterName", manageDatabase.getClusters("kafka", tenantId)
                    .get(envSelected.getClusterId()).getClusterName() + "-" + tenantId);
            params.add("topicName", aclReq.getTopicname());
            params.add("consumerGroup", aclReq.getConsumergroup());
            params.add("aclType", aclReq.getTopictype());
            params.add("acl_ip", aclReq.getAcl_ip());
            params.add("acl_ssl", aclReq.getAcl_ssl());
            params.add("transactionalId", aclReq.getTransactionalId());
            if (aclReq.getAclIpPrincipleType() != null) {
                params.add("aclIpPrincipleType", aclReq.getAclIpPrincipleType().name());
            }

            if(aclReq.getAclPatternType() != null && aclReq.getAclPatternType().equals("PREFIXED"))
                params.add("isPrefixAcl", "true");
            else params.add("isPrefixAcl", "false");

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);//createHeaders("user1", "pwd");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            response = restTemplate.postForEntity(uri, request, String.class);
        }catch(Exception e){
            log.error("Error from approveAclRequests {}", e.toString());
            throw new KafkawizeException("Could not approve acl request. Please contact Administrator.");
        }
        return response;
    }

    ResponseEntity<String> postSchema(SchemaRequest schemaRequest, String env, String topicName, int tenantId) throws KafkawizeException {
        log.info("postSchema {} {}", topicName, env);
        getClusterApiProperties(tenantId);
        ResponseEntity<String> response;
        try {
            String URI_POST_SCHEMA = "/topics/postSchema";
            String uri = clusterConnUrl + URI_POST_SCHEMA;

            RestTemplate restTemplate = getRestTemplate();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(env, tenantId);
            String bootstrapHost = manageDatabase.getClusters("schemaregistry", tenantId)
                    .get(envSelected.getClusterId()).getBootstrapServers();
            params.add("env", bootstrapHost);
            params.add("topicName", topicName);
            params.add("fullSchema", schemaRequest.getSchemafull());

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);//createHeaders("user1", "pwd");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            response = restTemplate.postForEntity(uri, request, String.class);
        }catch(Exception e){
            log.error("Error from postSchema {}", e.toString());
            throw new KafkawizeException("Could not post schema. Please contact Administrator.");
        }
        return response;
    }

    public TreeMap<Integer, HashMap<String, Object>> getAvroSchema(String schemaRegistryHost, String clusterName,
                                                                   String topicName, int tenantId) throws Exception{
        log.info("getAvroSchema {} {}", schemaRegistryHost, topicName);
        getClusterApiProperties(tenantId);
        TreeMap<Integer, HashMap<String, Object>> allVersionSchemas = new TreeMap<>(Collections.reverseOrder());
        try {
            String URI_GET_TOPICS = "/topics/getSchema/";
            String uriGetTopicsFull = clusterConnUrl + URI_GET_TOPICS + schemaRegistryHost + "/" + clusterName + "/" + topicName;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<String>> entity = new HttpEntity<>(headers);

            ResponseEntity<TreeMap> s = restTemplate.exchange
                    (uriGetTopicsFull, HttpMethod.GET, entity, TreeMap.class);

            TreeMap<String, HashMap<String, Object>> schemaObjects = s.getBody();
            for (String schemaVersion : schemaObjects.keySet()) {
                allVersionSchemas.put(Integer.parseInt(schemaVersion), schemaObjects.get(schemaVersion));
            }

            return allVersionSchemas;
        }catch(Exception e){
            log.error("Error from getAvroSchema {}", e.toString());
            throw new KafkawizeException("Could not get schema.");
        }
    }

    public LinkedHashMap<String, Object> getConnectorDetails(String connectorName, String kafkaConnectHost, int tenantId) throws KafkawizeException {
        log.info("getConnectorDetails {} {}", connectorName , kafkaConnectHost);
        getClusterApiProperties(tenantId);
        try {
            String URI_GET_TOPICS = "/topics/getConnectorDetails/" + connectorName + "/" + kafkaConnectHost;
            String uriGetConnectorsFull = clusterConnUrl + URI_GET_TOPICS ;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<String>> entity = new HttpEntity<>(headers);

            ResponseEntity<LinkedHashMap> s = restTemplate.exchange
                    (uriGetConnectorsFull, HttpMethod.GET, entity, LinkedHashMap.class);

            return s.getBody();
        }catch(Exception e){
            log.error("Error from getConnectorDetails {}", e.toString());
            throw new KafkawizeException("Could not get Connector Details." + connectorName);
        }
    }

    public ArrayList<String> getAllKafkaConnectors(String kafkaConnectHost, int tenantId) throws KafkawizeException {
        log.info("getAllKafkaConnectors {}", kafkaConnectHost);
        getClusterApiProperties(tenantId);
        try {
            String URI_GET_TOPICS = "/topics/getAllConnectors/" + kafkaConnectHost;
            String uriGetConnectorsFull = clusterConnUrl + URI_GET_TOPICS ;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
            headers.setContentType(MediaType.APPLICATION_JSON);

            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Set<String>> entity = new HttpEntity<>(headers);

            ResponseEntity<ArrayList> s = restTemplate.exchange
                    (uriGetConnectorsFull, HttpMethod.GET, entity, ArrayList.class);

            return s.getBody();
        }catch(Exception e){
            log.error("Error from getAllKafkaConnectors {}", e.toString());
            throw new KafkawizeException("Could not get KafkaConnectors.");
        }
    }

    public HashMap<String, String> retrieveMetrics(String jmxUrl, String objectName) throws KafkawizeException {
        log.info("retrieveMetrics {} {}", jmxUrl, objectName);
        getClusterApiProperties(101);
        try {
            String URI_GET_TOPICS = "/metrics/getMetrics";
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("jmxUrl", jmxUrl);
            params.add("objectName", objectName);

            String uriGetTopicsFull = clusterConnUrl + URI_GET_TOPICS ;
            RestTemplate restTemplate = getRestTemplate();

            HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);//createHeaders("user1", "pwd");
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<HashMap> s = restTemplate.postForEntity(uriGetTopicsFull, entity, HashMap.class);

            return Objects.requireNonNull(s.getBody());

        }catch (Exception e){
            log.error("Error from  retrieveMetrics {} {}", jmxUrl, e.toString());
            throw new KafkawizeException("Could not get metrics.");
        }
    }

    private HttpHeaders createHeaders(String username, String password) {
        HttpHeaders httpHeaders = new HttpHeaders();
        String auth = username + ":" + decodePwd(password);
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.US_ASCII));
        String authHeader = "Basic " + new String(encodedAuth);
        httpHeaders.set("Authorization", authHeader);

        return httpHeaders;
    }

    // to connect to cluster api if https
    @PostConstruct
    private void setKwSSLContext(){
        if(keyStore != null && !keyStore.equals("null")) {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            javax.net.ssl.SSLContext sslContext = null;
            try {
                sslContext = org.apache.http.ssl.SSLContexts.custom()
                        .loadKeyMaterial(getStore(keyStorePwd, keyStore), keyStorePwd.toCharArray())
                        .loadTrustMaterial(null, acceptingTrustStrategy).build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException
                    | CertificateException | UnrecoverableKeyException | IOException e) {
                log.error(e.getMessage());
            }
            SSLConnectionSocketFactory csf = null;
            if (sslContext != null) {
                csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
            }
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
            requestFactory = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
        }
    }

    protected KeyStore getStore(String secret, String storeLoc) throws
            KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
//        ClassPathResource resource = new ClassPathResource(storeLoc);
//        File key = new File(storeLoc);

        File key = ResourceUtils.getFile(storeLoc);

        final KeyStore store = KeyStore.getInstance(keyStoreType);
        try (InputStream inputStream = new FileInputStream(key)) {
            store.load(inputStream, secret.toCharArray());
        }
        return store;
    }

    private String decodePwd(String pwd){
        if(pwd != null) {
            BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
            textEncryptor.setPasswordCharArray(encryptorSecretKey.toCharArray());

            return textEncryptor.decrypt(pwd);
        }
        return "";
    }


}
