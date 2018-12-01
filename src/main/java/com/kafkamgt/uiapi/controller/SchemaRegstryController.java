package com.kafkamgt.uiapi.controller;


import com.google.gson.Gson;
import com.kafkamgt.uiapi.dao.AclReq;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.helpers.ManageTopics;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.Charset;
import java.util.List;
import java.util.StringTokenizer;


@RestController
@RequestMapping("/")
public class SchemaRegstryController {

    private static Logger LOG = LoggerFactory.getLogger(SchemaRegstryController.class);

    @Autowired
    ManageTopics createTopicHelper;

    @Value("${clusterapi.url}")
    String clusterConnUrl;

    @Value("${clusterapi.username}")
    String clusterApiUser;

    @Value("${clusterapi.password}")
    String clusterApiPwd;

    @RequestMapping(value = "/getSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SchemaRequest>> getSchemaRequests() {

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<SchemaRequest> topicReqs = null;
            topicReqs = createTopicHelper.getAllSchemaRequests(userDetails.getUsername());

        return new ResponseEntity<List<SchemaRequest>>(topicReqs, HttpStatus.OK);
    }

    @RequestMapping(value = "/getCreatedSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<SchemaRequest>> getCreatedSchemaRequests() {

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<SchemaRequest> topicReqs = null;
        topicReqs = createTopicHelper.getCreatedSchemaRequests(userDetails.getUsername());

        return new ResponseEntity<List<SchemaRequest>>(topicReqs, HttpStatus.OK);
    }



    @RequestMapping(value = "/deleteSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteSchemaRequests(@RequestParam("topicName") String topicName) {

        LOG.info("In delete req"+topicName);

        StringTokenizer strTkr = new StringTokenizer(topicName,"-----");
        topicName = strTkr.nextToken();
        String schemaVersion = strTkr.nextToken();
        String env = strTkr.nextToken();

        String deleteTopicReqStatus = createTopicHelper.deleteSchemaRequest(topicName,schemaVersion, env);

        deleteTopicReqStatus = "{\"result\":\""+deleteTopicReqStatus+"\"}";
        return new ResponseEntity<String>(deleteTopicReqStatus, HttpStatus.OK);
    }



    @RequestMapping(value = "/execSchemaRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> execSchemaRequests(@RequestParam("topicName") String topicName) {

        StringTokenizer strTkr = new StringTokenizer(topicName,"-----");
        topicName = strTkr.nextToken();
        String schemaversion = strTkr.nextToken();
        String env = strTkr.nextToken();

        SchemaRequest schemaRequest = createTopicHelper.selectSchemaRequest(topicName,schemaversion,env);

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("--In approve req--"+topicName+"---"+ topicName+schemaversion);

        String uri = clusterConnUrl+"/topics/postSchema";

        LOG.info("URI is:"+uri);
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();
        params.add("env",bootstrapHost);

        params.add("topicName",topicName);
        params.add("fullSchema",schemaRequest.getSchemafull());

        HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity( uri, request , String.class );

        if(response.getBody().contains("id\":")) {
            String updateTopicReqStatus = createTopicHelper.updateSchemaRequest(topicName, schemaversion, env, userDetails.getUsername());

            updateTopicReqStatus = "{\"result\":\"" + updateTopicReqStatus + "\"}";
            return new ResponseEntity<String>(updateTopicReqStatus, HttpStatus.OK);
        }
        else {
            String updateTopicReqStatus1 = "{\"result\":\"" + "Failure in uploading schema" + "\"}";
            return new ResponseEntity<String>(updateTopicReqStatus1, HttpStatus.OK);
        }
    }

    int topicCounter=0;
    public int counterIncrement()
    {
        topicCounter++;
        return topicCounter;
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

    @PostMapping(value = "/uploadSchema")
    public ResponseEntity<String> uploadSchema(@RequestParam ("addSchemaRequest") String addSchemaRequest){

        LOG.info("*********"+addSchemaRequest);
        Gson gson = new Gson();

        SchemaRequest schemaRequest = gson.fromJson(addSchemaRequest, SchemaRequest.class);

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info(schemaRequest.getTopicname()+ "---" + schemaRequest.getTeamname()+"---"+schemaRequest.getEnvironment() +
                "---"+schemaRequest.getAppname()+"---"+
                schemaRequest.getTeamname());
        schemaRequest.setUsername(userDetails.getUsername());

        String execRes = createTopicHelper.requestForSchema(schemaRequest);

        String schemaaddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<String>(schemaaddResult, HttpStatus.OK);
    }
}
