package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicPK;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.model.TopicInfo;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicControllerService {
    private static Logger LOG = LoggerFactory.getLogger(TopicControllerService.class);

    @Value("${clusterapi.url}")
    private String clusterConnUrl;

    @Value("${clusterapi.username}")
    private String clusterApiUser;

    @Value("${clusterapi.password}")
    private String clusterApiPwd;

    String uriCreateTopics = "/topics/createTopics";

    String uriGetTopics = "/topics/getTopics/";

    @Autowired
    private ManageTopics createTopicHelper;

    @Autowired
    private Environment springEnvProps;

    private String getUserName(){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }

    private UserDetails getUserDetails(){
        return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String createTopics(TopicRequest topicRequestReq) {

        LOG.info(topicRequestReq.getTopicname()+ "---" + topicRequestReq.getTeamname()+"---"+ topicRequestReq.getEnvironment() + "---"+ topicRequestReq.getAppname());
        topicRequestReq.setUsername(getUserName());

        String topicPartitions = topicRequestReq.getTopicpartitions();
        int topicPartitionsInt;
        String envSelected = topicRequestReq.getEnvironment();
        String defPartns = springEnvProps.getProperty("kafka." + envSelected + ".default.partitions");
        String defMaxPartns = springEnvProps.getProperty("kafka." + envSelected + ".default.maxpartitions");
        String defaultRf = springEnvProps.getProperty("kafka." + envSelected + ".default.replicationfactor");

        if(defPartns==null)
            defPartns="1";

        if(defMaxPartns==null)
            defMaxPartns="1";

        if(defaultRf==null)
            defaultRf="1";

        try {
            int defMaxPartnsInt = Integer.parseInt(defMaxPartns);

            if (topicPartitions != null && topicPartitions.length() > 0) {
                topicPartitionsInt = Integer.parseInt(topicPartitions);

                if (topicPartitionsInt > defMaxPartnsInt)
                    topicRequestReq.setTopicpartitions(defMaxPartns);
            } else
                topicRequestReq.setTopicpartitions(defPartns);

            topicRequestReq.setReplicationfactor(defaultRf);
        }catch (Exception e){
            LOG.error("Unable to set topic partitions, setting default from properties.");
            topicRequestReq.setTopicpartitions(defPartns);
        }

        String execRes = createTopicHelper.requestForTopic(topicRequestReq);

        String topicaddResult = "{\"result\":\""+execRes+"\"}";
        return topicaddResult;
    }

    public String updateSyncTopics(String updatedSyncTopics, String envSelected) {

        UserDetails userDetails = getUserDetails();

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        if(authority.equals("ROLE_SUPERUSER")){}
        else
            return "{ \"result\": \"Not Authorized\" }";

        StringTokenizer strTkr = new StringTokenizer(updatedSyncTopics,"\n");
        String topicSel=null,teamSelected=null,tmpToken=null;
        List<Topic> listTopics = new ArrayList<>();
        Topic t;
        while(strTkr.hasMoreTokens()){
            tmpToken = strTkr.nextToken().trim();

            int indexOfSep = tmpToken.indexOf("-----");
            if(indexOfSep>0) {
                topicSel = tmpToken.substring(0, indexOfSep);
                teamSelected = tmpToken.substring(indexOfSep + 5, tmpToken.length());

                t = new Topic();

                TopicPK topicPK = new TopicPK();
                topicPK.setTopicname(topicSel);
                topicPK.setEnvironment(envSelected);

                t.setTopicname(topicSel);
                t.setEnvironment(envSelected);
                t.setTeamname(teamSelected);
                t.setTopicPK(topicPK);

                listTopics.add(t);
            }
        }
        String execRes = createTopicHelper.addToSynctopics(listTopics);

        return "{\"result\":\""+execRes+"\"}";
    }

    public List<PCStream> getTopicStreams(String envSelected) {
        return createTopicHelper.selectTopicStreams(envSelected);
    }

    public List<TopicRequest> getTopicRequests() {

        return createTopicHelper.getAllTopicRequests(getUserName());
    }

    public Topic getTopicTeam(String topicName, String env) {

        return createTopicHelper.getTopicTeam(topicName, env);
    }

    public List<TopicRequest> getCreatedTopicRequests() {
       return createTopicHelper.getCreatedTopicRequests(getUserName());
    }

    public String deleteTopicRequests(String topicName) {

        StringTokenizer strTkr = new StringTokenizer(topicName,",");
        topicName = strTkr.nextToken();
        String env = strTkr.nextToken();

        String deleteTopicReqStatus = createTopicHelper.deleteTopicRequest(topicName,env);

        return "{\"result\":\""+deleteTopicReqStatus+"\"}";
    }

    public String approveTopicRequests(String topicName) {

        StringTokenizer strTkr = new StringTokenizer(topicName,",");
        topicName = strTkr.nextToken();
        String env = strTkr.nextToken();

        TopicRequest topicRequest = createTopicHelper.selectTopicRequestsForTopic(topicName, env);

        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params= new LinkedMultiValueMap<>();

        Env envSelected= createTopicHelper.selectEnvDetails(topicRequest.getEnvironment());
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();
        params.add("env",bootstrapHost);

        params.add("topicName",topicName);
        params.add("partitions", topicRequest.getTopicpartitions());
        params.add("rf", topicRequest.getReplicationfactor());
        params.add("acl_ip", topicRequest.getAcl_ip());
        params.add("acl_ssl", topicRequest.getAcl_ssl());

        HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity( clusterConnUrl+uriCreateTopics, request , String.class );

        String updateTopicReqStatus = response.getBody();

        if(response.getBody().equals("success"))
            updateTopicReqStatus = createTopicHelper.updateTopicRequest(topicRequest,getUserName());

        return "{\"result\":\""+updateTopicReqStatus+"\"}";
    }

    public List<TopicInfo> getTopics(String env, String pageNo, String topicNameSearch) {

        UserDetails userDetails = getUserDetails();
        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")){}
        else {
            List<TopicInfo> topicsList1 = new ArrayList();
            return topicsList1;
        }

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uriGetTopicsFull = clusterConnUrl + uriGetTopics + bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<String>> entity = new HttpEntity<>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uriGetTopicsFull, HttpMethod.GET, entity, Set.class);

        // Get Sync topics
        List<Topic> topicsFromSOT = createTopicHelper.getSyncTopics(env);

        topicCounter = 0;
        List<String> topicsList = new ArrayList(s.getBody());

        List<String> topicFilteredList = topicsList;
        // Filter topics on topic name for search
        if(topicNameSearch!=null && topicNameSearch.length()>0){
            final String topicSearchFilter = topicNameSearch;
            topicFilteredList = topicsList.stream().filter(topic-> {
                        if(topic.contains(topicSearchFilter))
                            return true;
                        else
                            return false;
                    }
            ).collect(Collectors.toList());
        }

        topicsList = topicFilteredList;
        Collections.sort(topicsList);

        return getTopicList(topicsList,topicsFromSOT,pageNo);
    }

    public List<TopicRequest> getSyncTopics(String env, String pageNo, String topicNameSearch) {

        UserDetails userDetails = getUserDetails();
        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_SUPERUSER")){}
        else{
            List<TopicRequest> topicsList1 = new ArrayList();
            return topicsList1;
        }

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uri = clusterConnUrl + uriGetTopics + bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<String>> entity = new HttpEntity<>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uri, HttpMethod.GET, entity, Set.class);

        topicCounter = 0;
        List<String> topicsList = new ArrayList(s.getBody());

        List<String> topicFilteredList = topicsList;
        // Filter topics on topic name for search

        if(topicNameSearch!=null && topicNameSearch.length()>0){
            final String topicSearchFilter = topicNameSearch;
            topicFilteredList = topicsList.stream().filter(topic-> {
                        if(topic.contains(topicSearchFilter))
                            return true;
                        else
                            return false;
                    }
            ).collect(Collectors.toList());
        }

        topicsList = topicFilteredList;
        Collections.sort(topicsList);

        return getSyncTopicList(topicsList,userDetails,pageNo,env);
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

    public List<TopicInfo> getTopicList(List<String> topicsList, List<Topic> topicsFromSOT, String pageNo){
        int totalRecs = topicsList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);
        int requestPageNo = Integer.parseInt(pageNo);

        List<TopicInfo> topicsListMap = null;//new ArrayList<>();
        if(totalRecs>0)
            topicsListMap = new ArrayList<>();

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
                            if (a.getTopicPK().getTopicname().equals(tmpTopicName))
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

        return topicsListMap;
    }

    public List<TopicRequest> getSyncTopicList(List<String> topicsList, UserDetails userDetails, String pageNo, String env){
        int totalRecs = topicsList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        // Get Sync topics
        List<Topic> topicsFromSOT = createTopicHelper.getSyncTopics(env);

        List<TopicRequest> topicsListMap = new ArrayList<>();
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
                TopicRequest mp = new TopicRequest();
                mp.setSequence(counterInc + "");
                strTkr = new StringTokenizer(topicsList.get(i).toString(),":::::");

                while(strTkr.hasMoreTokens()){
                    final String tmpTopicName = strTkr.nextToken();

                    mp.setTopicname(tmpTopicName);
                    String teamUpdated = null;
                    try {
                        teamUpdated = topicsFromSOT.stream().filter(a -> {
                            if (a.getTopicPK().getTopicname().equals(tmpTopicName))
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
        return topicsListMap;
    }


}
