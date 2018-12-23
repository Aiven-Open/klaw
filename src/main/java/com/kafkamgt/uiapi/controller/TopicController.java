package com.kafkamgt.uiapi.controller;


import com.google.gson.Gson;
import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.helpers.ManageTopics;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
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
import java.util.*;


@RestController
@RequestMapping("/")
public class TopicController {

    private static Logger LOG = LoggerFactory.getLogger(TopicController.class);

    @Autowired
    ManageTopics createTopicHelper;

    @Value("${clusterapi.url}")
    String clusterConnUrl;

    @Value("${clusterapi.username}")
    String clusterApiUser;

    @Value("${clusterapi.password}")
    String clusterApiPwd;

    @Autowired
    Environment springEnvProps;

    @PostMapping(value = "/createTopics")
    public ResponseEntity<String> createTopics(@RequestParam ("addTopicRequest") String addTopicReq) {

        LOG.info("*********"+addTopicReq);
        Gson gson = new Gson();

        Topic topicReq = gson.fromJson(addTopicReq, Topic.class);

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info(topicReq.getTopicName()+ "---" + topicReq.getTeamname()+"---"+topicReq.getEnvironment() + "---"+topicReq.getAppname());
        topicReq.setUsername(userDetails.getUsername());

        String execRes = null;

        String topicPartitions = topicReq.getTopicpartitions();
        int topicPartitionsInt = 1;
        String envSelected = topicReq.getEnvironment();
        String defPartns = springEnvProps.getProperty("kafka." + envSelected + ".default.partitions");
        String defMaxPartns = springEnvProps.getProperty("kafka." + envSelected + ".default.maxpartitions");

        String defaultRf = springEnvProps.getProperty("kafka." + envSelected + ".default.replicationfactor");

        try {
            int defMaxPartnsInt = Integer.parseInt(defMaxPartns);

            if (topicPartitions != null && topicPartitions.length() > 0) {
                topicPartitionsInt = Integer.parseInt(topicPartitions);

                if (topicPartitionsInt > defMaxPartnsInt)
                    topicReq.setTopicpartitions(defMaxPartns);
            } else
                topicReq.setTopicpartitions(defPartns);

            topicReq.setReplicationfactor(defaultRf);
        }catch (Exception e){
            LOG.error("Unable to set topic partitions, setting default.");
            topicReq.setTopicpartitions(defPartns);
        }

        execRes = createTopicHelper.requestForTopic(topicReq);

        String topicaddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<String>(topicaddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/updateSyncTopics")
    public ResponseEntity<String> updateSyncTopics(@RequestParam ("updatedSyncTopics") String updatedSyncTopics,
                                                   @RequestParam ("envSelected") String envSelected) {

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("User is "+userDetails.getUsername()+ userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        String json = "";
        if(authority.equals("ROLE_SUPERUSER")){}
        else{
            json = "{ \"result\": \"Not Authorized\" }";
            return new ResponseEntity<String>(json, HttpStatus.OK);
        }

        StringTokenizer strTkr = new StringTokenizer(updatedSyncTopics,"\n");
        String topicSel=null,teamSelected=null,tmpToken=null;
        List<Topic> listtopics = new ArrayList<>();
        Topic t = null;
        while(strTkr.hasMoreTokens()){
            tmpToken = strTkr.nextToken().trim();

            int indexOfSep = tmpToken.indexOf("-----");
            if(indexOfSep>0) {
                topicSel = tmpToken.substring(0, indexOfSep);
                teamSelected = tmpToken.substring(indexOfSep + 5, tmpToken.length());

                t = new Topic();
                t.setTopicName(topicSel);
                t.setTeamname(teamSelected);
                t.setEnvironment(envSelected);
                listtopics.add(t);
            }
        }
        String execRes = createTopicHelper.addToSynctopics(listtopics);

        String topicaddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<String>(topicaddResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicStreams", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<PCStream>> getTopicStreams(@RequestParam ("env") String envSelected) {

        List<PCStream> topicReqs = null;
        LOG.info("Env is :::"+envSelected);

        topicReqs = createTopicHelper.selectTopicStreams(envSelected);
        LOG.info(topicReqs+"");
        topicReqs.stream().forEach(a->a.getConsumerTeams().forEach(b->LOG.info(b)));

        return new ResponseEntity<List<PCStream>>(topicReqs, HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Topic>> getTopicRequests() {

        List<Topic> topicReqs = null;

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        topicReqs = createTopicHelper.getAllTopicRequests(userDetails.getUsername());

        return new ResponseEntity<List<Topic>>(topicReqs, HttpStatus.OK);
    }

    @RequestMapping(value = "/getTopicTeam", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Topic> getTopicTeam(@RequestParam("topicName") String topicName,
                                              @RequestParam("env") String env) {

       Topic topic = createTopicHelper.getTopicTeam(topicName, env);
      // LOG.info(env+"In get topic team"+topic + "---"+topicName);

       return new ResponseEntity<Topic>(topic, HttpStatus.OK);
    }

    @RequestMapping(value = "/getCreatedTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Topic>> getCreatedTopicRequests() {

        List<Topic> topicReqs = null;

            UserDetails userDetails =
                    (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            topicReqs = createTopicHelper.getCreatedTopicRequests(userDetails.getUsername());

        LOG.info("*****getCreatedTopicRequests"+topicReqs);
        return new ResponseEntity<List<Topic>>(topicReqs, HttpStatus.OK);
    }

    @RequestMapping(value = "/deleteTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteTopicRequests(@RequestParam("topicName") String topicName) {

        LOG.info("In delete req"+topicName);
        String deleteTopicReqStatus = createTopicHelper.deleteTopicRequest(topicName);

        deleteTopicReqStatus = "{\"result\":\""+deleteTopicReqStatus+"\"}";
        return new ResponseEntity<String>(deleteTopicReqStatus, HttpStatus.OK);
    }


    @RequestMapping(value = "/execTopicRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> approveTopicRequests(@RequestParam("topicName") String topicName) {

        Topic topic = createTopicHelper.selectTopicRequestsForTopic(topicName);

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String uri = clusterConnUrl+"/topics/createTopics";
        LOG.info("URI is:"+uri);
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();

        Env envSelected= createTopicHelper.selectEnvDetails(topic.getEnvironment());
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();
        params.add("env",bootstrapHost);

        params.add("topicName",topicName);
        params.add("partitions",topic.getTopicpartitions());
        params.add("rf",topic.getReplicationfactor());
        params.add("acl_ip",topic.getAcl_ip());
        params.add("acl_ssl",topic.getAcl_ssl());

        HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity( uri, request , String.class );

        String updateTopicReqStatus = response.getBody();

        if(response.getBody().equals("success"))
         updateTopicReqStatus = createTopicHelper.updateTopicRequest(topicName,userDetails.getUsername());

        updateTopicReqStatus = "{\"result\":\""+updateTopicReqStatus+"\"}";
        return new ResponseEntity<String>(updateTopicReqStatus, HttpStatus.OK);
    }


    @RequestMapping(value = "/getTopics", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<TopicInfo>> getTopics(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo) {

        LOG.info(pageNo+" In get topics " + env);
        String json = "{ \"name\": \"John\" }";

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("User is "+userDetails.getUsername()+ userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")){}
        else{
            json = "{ \"name\": \"Not Authorized\" }";
            List<TopicInfo> topicsList1 = new ArrayList();
            return new ResponseEntity<List<TopicInfo>>(topicsList1, HttpStatus.OK);
        }

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uri = clusterConnUrl+"/topics/getTopics/"+bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<String>> entity = new HttpEntity<Set<String>>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uri, HttpMethod.GET, entity, Set.class);

        // Get Sync topics
        List<Topic> topicsFromSOT = createTopicHelper.getSyncTopics(env);

        topicCounter = 0;
        List<String> topicsList = new ArrayList(s.getBody());
        Collections.sort(topicsList);

        int totalRecs = topicsList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        List<TopicInfo> topicsListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        StringTokenizer strTkr = null;
        for(int i=0;i<totalRecs;i++){
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                TopicInfo mp = new TopicInfo();
                mp.setSequence(counterInc + "");
                strTkr = new StringTokenizer(topicsList.get(i).toString(),":::::");

                while(strTkr.hasMoreTokens()){
                    final String tmpTopicName = strTkr.nextToken();
                    mp.setTopicName(tmpTopicName);

                    String teamUpdated = null;
                    try {
                        teamUpdated = topicsFromSOT.stream().filter(a -> {
                            if (a.getTopicName().equals(tmpTopicName))
                                return true;
                            else
                                return false;
                        }).findFirst().get().getTeamname();
                    }catch (Exception e){}

                    if(teamUpdated!=null && !teamUpdated.equals("undefined")){
                        mp.setTeamname(teamUpdated);
                    }
                    mp.setNoOfReplcias(strTkr.nextToken());
                    mp.setNoOfPartitions(strTkr.nextToken());
                }
                mp.setTotalNoPages(totalPages + "");
                List<String> numList = new ArrayList<>();
                for (int k = 1; k <= totalPages; k++) {
                    numList.add("" + k);
                }
                mp.setAllPageNos(numList);
                topicsListMap.add(mp);
            }

        }
        return new ResponseEntity<List<TopicInfo>>(topicsListMap, HttpStatus.OK);
    }

    @RequestMapping(value = "/getSyncTopics", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<Topic>> getSyncTopics(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo) {

        LOG.info(pageNo+" In get sync topics " + env);
        String json = "{ \"name\": \"\" }";

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("User is "+userDetails.getUsername()+ userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_SUPERUSER")){}
        else{
            json = "{ \"name\": \"Not Authorized\" }";
            List<Topic> topicsList1 = new ArrayList();
            return new ResponseEntity<List<Topic>>(topicsList1, HttpStatus.OK);
        }

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uri = clusterConnUrl+"/topics/getTopics/"+bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<String>> entity = new HttpEntity<Set<String>>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uri, HttpMethod.GET, entity, Set.class);

        topicCounter = 0;
        List<String> topicsList = new ArrayList(s.getBody());
        Collections.sort(topicsList);

        int totalRecs = topicsList.size();
        int recsPerPage = 100;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        // Get Sync topics
        List<Topic> topicsFromSOT = createTopicHelper.getSyncTopics(env);

        List<Topic> topicsListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        StringTokenizer strTkr = null;

        List<String> teamList = new ArrayList<>();

        createTopicHelper.selectAllTeamsOfUsers(userDetails.getUsername())
                .forEach(teamS->teamList.add(teamS.getTeamname()));
        //String tmpTopicName = null;
        for(int i=0;i<totalRecs;i++){
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                Topic mp = new Topic();
                mp.setSequence(counterInc + "");
                strTkr = new StringTokenizer(topicsList.get(i).toString(),":::::");

                while(strTkr.hasMoreTokens()){
                    final String tmpTopicName = strTkr.nextToken();

                    mp.setTopicName(tmpTopicName);
                    String teamUpdated = null;
                    try {
                        teamUpdated = topicsFromSOT.stream().filter(a -> {
                            if (a.getTopicName().equals(tmpTopicName))
                                return true;
                            else
                                return false;
                        }).findFirst().get().getTeamname();
                    }catch (Exception e){}

                    if(teamUpdated!=null && !teamUpdated.equals("undefined")){
                        List<String> tmpList = new ArrayList<String>();
                        tmpList.add(teamUpdated);
                        mp.setPossibleTeams(teamList);
                        mp.setTeamname(teamUpdated);
                    }
                    else{
                        mp.setPossibleTeams(teamList);
                        mp.setTeamname("");
                    }

                    strTkr.nextToken(); // ignore
                    strTkr.nextToken(); // ignore
                }

                List<String> numList = new ArrayList<>();
                for (int k = 1; k <= totalPages; k++) {
                    numList.add("" + k);
                }
                mp.setTotalNoPages(totalPages + "");
                mp.setAllPageNos(numList);

                topicsListMap.add(mp);
            }

        }

        return new ResponseEntity<List<Topic>>(topicsListMap, HttpStatus.OK);
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


}
