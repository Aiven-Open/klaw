package com.kafkamgt.uiapi.helpers;

import com.kafkamgt.uiapi.controller.UiConfigController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TestUtil {

    private static Logger LOG = LoggerFactory.getLogger(UiConfigController.class);


    public TestUtil(){
        callSchema();
    }

    public void callSchema(){

//        String uri = "http://bl00034:9098/topics/postSchema";
//        RestTemplate restTemplate = new RestTemplate();
//
//        Map<String,String> params = new HashMap<>();
//        params.put("schema","kjfashfkjafhkjahfkjah");

//        HttpHeaders httpHeaders = createHeaders("user1", "pwd");
//        httpHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//        //httpHeaders.setContentType(MediaType.APPLICATION_JSON);
//        //httpHeaders.set("Content-Type","application/json");
//
//        restTemplate.exchange
//                (uri, HttpMethod.POST, new HttpEntity<String>(httpHeaders), String.class,params);
//        //LOG.info(s.toString());

//        HttpHeaders headers = createHeaders("user1", "pwd");
//        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
//        map.add("schema", "fkjdsanjfahkj");
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
//
//        ResponseEntity<String> response = restTemplate.postForEntity( uri, request , String.class );
//
//        uri = "http://bl00034:9098/topics/createTopics";
//         restTemplate = new RestTemplate();
//
//         headers = createHeaders("user1", "pwd1");
//        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        map= new LinkedMultiValueMap<String, String>();
//        map.add("topicName", "my sweet topic");
//
//        request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
//
//        response = restTemplate.postForEntity( uri, request , String.class );
//
//        System.out.println(response);

        String uri = "http://kafkaserver:8081/subjects/testtopic174-value/versions";
        LOG.info("URI is:"+uri);
        RestTemplate restTemplate = new RestTemplate();


        Map<String, String> params= new HashMap<String, String>();

        params.put("schema","{\"type\": \"string\"}");

        HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
        headers.set("Content-Type","application/vnd.schemaregistry.v1+json");

        HttpEntity<Map<String, String>> request = new HttpEntity<Map<String, String>>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity( uri, request , String.class );

        String updateTopicReqStatus = response.getBody();
    }

//    HttpHeaders createHeaders(String username, String password) {
//        return new HttpHeaders() {{
//            String auth = username + ":" + password;
//            byte[] encodedAuth = Base64.encodeBase64(
//                    auth.getBytes(Charset.forName("US-ASCII")));
//            String authHeader = "Basic " + new String(encodedAuth);
//            set("Authorization", authHeader);
//        }};
//    }

//    public static void main(String[] args){
//        new TestUtil();
//    }
}
