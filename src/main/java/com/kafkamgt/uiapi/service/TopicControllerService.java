package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicPK;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.PCStream;
import com.kafkamgt.uiapi.model.TopicInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

@Service
public class TopicControllerService {
    private static Logger LOG = LoggerFactory.getLogger(TopicControllerService.class);

    @Autowired
    ClusterApiService clusterApiService;

    @Value("${custom.clusterapi.url}")
    private String clusterConnUrl;

    @Value("${custom.clusterapi.username}")
    private String clusterApiUser;

    @Value("${custom.clusterapi.password}")
    private String clusterApiPwd;

    String uriGetTopics = "/topics/getTopics/";

    @Autowired
    private ManageTopics manageTopics;

    @Autowired
    private UtilService utilService;

    @Autowired
    private Environment springEnvProps;

    public String createTopics(TopicRequest topicRequestReq) {

        LOG.info(topicRequestReq.getTopicname()+ "---" + topicRequestReq.getTeamname()+"---"+ topicRequestReq.getEnvironment() + "---"+ topicRequestReq.getAppname());
        topicRequestReq.setUsername(utilService.getUserName());

        String topicPartitions = topicRequestReq.getTopicpartitions();
        int topicPartitionsInt;
        String envSelected = topicRequestReq.getEnvironment();

        Env env = manageTopics.selectEnvDetails(envSelected);
        String otherParams = env.getOtherParams();
        String params[] ;
        String defPartns = null, defMaxPartns = null, defaultRf = null;

        try{
            if(otherParams!=null) {
                params = otherParams.split(",");

                if(params!=null && params.length==3) {
                     defPartns = (params[0]).split("=")[1];
                     defMaxPartns = (params[1]).split("=")[1];
                     defaultRf = (params[2]).split("=")[1];
                }
            }
        }catch (Exception e){
            LOG.error("Unable to set topic partitions, setting default from properties.");
        }

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
                else
                    topicRequestReq.setTopicpartitions(topicPartitions);
            } else
                topicRequestReq.setTopicpartitions(defPartns);

            topicRequestReq.setReplicationfactor(defaultRf);
        }catch (Exception e){
            LOG.error("Unable to set topic partitions, setting default from properties.");
            topicRequestReq.setTopicpartitions(defPartns);
        }

        String execRes = manageTopics.requestForTopic(topicRequestReq);

        String topicaddResult = "{\"result\":\""+execRes+"\"}";
        return topicaddResult;
    }

    public String updateSyncTopics(String updatedSyncTopics, String envSelected) {

        if(!utilService.checkAuthorizedSU())
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
        String execRes = manageTopics.addToSynctopics(listTopics);

        return "{\"result\":\""+execRes+"\"}";
    }

    public List<PCStream> getTopicStreams(String envSelected, String pageNo, String topicNameSearch) {
        List<PCStream> pcList = manageTopics.selectTopicStreams(envSelected);

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        List<PCStream> pcListUpdated = pcList;
        if(topicNameSearch!=null && topicNameSearch.length()>0) {
            final String topicSearchFilter = topicNameSearch;
            pcListUpdated = pcList.stream()
                    .filter(pcStream -> pcStream.getTopicName().contains(topicSearchFilter))
                    .collect(Collectors.toList());
        }
        return getPCStreamsPaginated(pageNo,pcListUpdated);
    }

    public List<PCStream> getPCStreamsPaginated(String pageNo, List<PCStream> aclListMap){
        List<PCStream> aclListMapUpdated = new ArrayList<>();

        int totalRecs = aclListMap.size();
        int recsPerPage = 20;

        int totalPages = aclListMap.size()/recsPerPage + (aclListMap.size()%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);
        topicCounter = 0;
        for(int i=0;i<totalRecs;i++) {
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                PCStream mp = aclListMap.get(i);
                mp.setSequence(counterInc + "");

                mp.setTotalNoPages(totalPages + "");
                List<String> numList = new ArrayList<>();
                for (int k = 1; k <= totalPages; k++) {
                    numList.add("" + k);
                }
                mp.setAllPageNos(numList);
                aclListMapUpdated.add(mp);
            }
        }
        return aclListMapUpdated;
    }

    public List<TopicRequest> getTopicRequests() {

        return manageTopics.getAllTopicRequests(utilService.getUserName());
    }

    public Topic getTopicTeam(String topicName, String env) {

        return manageTopics.getTopicTeam(topicName, env);
    }

    public List<TopicRequest> getCreatedTopicRequests() {
       return manageTopics.getCreatedTopicRequests(utilService.getUserName());
    }

    public String deleteTopicRequests(String topicName) {

        StringTokenizer strTkr = new StringTokenizer(topicName,",");
        topicName = strTkr.nextToken();
        String env = strTkr.nextToken();

        String deleteTopicReqStatus = manageTopics.deleteTopicRequest(topicName,env);

        return "{\"result\":\""+deleteTopicReqStatus+"\"}";
    }

    public String approveTopicRequests(String topicName) throws KafkawizeException {

        StringTokenizer strTkr = new StringTokenizer(topicName,",");
        topicName = strTkr.nextToken();
        String env = strTkr.nextToken();

        TopicRequest topicRequest = manageTopics.selectTopicRequestsForTopic(topicName, env);

        ResponseEntity<String> response = clusterApiService.approveTopicRequests(topicName,topicRequest);

        String updateTopicReqStatus = response.getBody();

        if(response.getBody().equals("success"))
            updateTopicReqStatus = manageTopics.updateTopicRequest(topicRequest,utilService.getUserName());

        return "{\"result\":\""+updateTopicReqStatus+"\"}";
    }

    public String declineTopicRequests(String topicName) throws KafkawizeException {

        StringTokenizer strTkr = new StringTokenizer(topicName,",");
        topicName = strTkr.nextToken();
        String env = strTkr.nextToken();

        TopicRequest topicRequest = manageTopics.selectTopicRequestsForTopic(topicName, env);

        manageTopics.declineTopicRequest(topicRequest,utilService.getUserName());

        return "{\"result\":\""+ "Request declined. " +"\"}";
    }

    public List<String> getAllTopics(String env) throws Exception {

        Env envSelected = manageTopics.selectEnvDetails(env);
        String bootstrapHost = envSelected.getHost() + ":" + envSelected.getPort();

        List<String> topicsList = clusterApiService.getAllTopics(bootstrapHost);

        List<String> topicsListNew = new ArrayList();

        int indexOfDots = 0;
        for (String s1 : topicsList) {
            indexOfDots = s1.indexOf(":::::");
            if(indexOfDots>0)
                topicsListNew.add(s1.substring(0,indexOfDots));
        }

        List<String> uniqueList = topicsListNew.stream().distinct().sorted().collect(Collectors.toList());

        return uniqueList;
    }

    public List<List<TopicInfo>> getTopics(String env, String pageNo, String topicNameSearch) throws Exception {

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        Env envSelected= manageTopics.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        List<String> topicsList = clusterApiService.getAllTopics(bootstrapHost);

        // Get Sync topics
        List<Topic> topicsFromSOT = manageTopics.getSyncTopics(env);

        topicCounter = 0;

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

        List<TopicInfo> topicListUpdated = getTopicList(topicsList,topicsFromSOT,pageNo);

        List<List<TopicInfo>> newList = getNewList(topicListUpdated);

        return newList;
    }

    private List<List<TopicInfo>> getNewList(List<TopicInfo> topicsList){

        List<List<TopicInfo>> newList = new ArrayList<>();
        List<TopicInfo> innerList = new ArrayList<>();
        int i=0;
        for(TopicInfo topicInfo : topicsList){

            innerList.add(topicInfo);

            if(i%3 == 2) {
                newList.add(innerList);
                innerList = new ArrayList<>();
            }
            i++;
        }

        if(innerList.size()>0)
            newList.add(innerList);

        return newList;
    }

    public List<TopicRequest> getSyncTopics(String env, String pageNo, String topicNameSearch) throws Exception {

        UserDetails userDetails = utilService.getUserDetails();

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        Env envSelected= manageTopics.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        List<String> topicsList = clusterApiService.getAllTopics(bootstrapHost);

        topicCounter = 0;

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

    public List<TopicInfo> getTopicList(List<String> topicsList, List<Topic> topicsFromSOT, String pageNo){
        int totalRecs = topicsList.size();
        int recsPerPage = 21;

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
        List<Topic> topicsFromSOT = manageTopics.getSyncTopics(env);

        List<TopicRequest> topicsListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        StringTokenizer strTkr = null;

        List<String> teamList = new ArrayList<>();

        manageTopics.selectAllTeamsOfUsers(userDetails.getUsername())
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
