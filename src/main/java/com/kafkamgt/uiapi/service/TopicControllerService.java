package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicPK;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.TopicInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicControllerService {

    @Autowired
    ClusterApiService clusterApiService;


    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    private UtilService utilService;

    public TopicControllerService(ClusterApiService clusterApiService, UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.utilService = utilService;
    }

    public String createTopics(TopicRequest topicRequestReq) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        log.info(topicRequestReq.getTopicname()+ "---" +
                topicRequestReq.getTeamname()+"---"+ topicRequestReq.getEnvironment() +
                "---"+ topicRequestReq.getAppname());
        topicRequestReq.setUsername(userDetails.getUsername());

        String topicPartitions = topicRequestReq.getTopicpartitions();

        String envSelected = topicRequestReq.getEnvironment();

        Env env = manageDatabase.getHandleDbRequests().selectEnvDetails(envSelected);

        if(validateParameters(topicRequestReq, env, topicPartitions)){
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().requestForTopic(topicRequestReq)+"\"}";
        }

        return "{\"result\":\"failure\"}";
    }

    private boolean validateParameters(TopicRequest topicRequestReq, Env env, String topicPartitions) throws KafkawizeException {
        String otherParams = env.getOtherParams();
        String params[] ;
        String defPartns = null, defMaxPartns = null, defaultRf = null;
        int topicPartitionsInt;

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
            log.error("Unable to set topic partitions, setting default from properties.");
        }

        try {
            int defMaxPartnsInt = Integer.parseInt(defMaxPartns);

            if (topicPartitions != null && topicPartitions.length() > 0) {
                topicPartitionsInt = Integer.parseInt(topicPartitions);

                if (topicPartitionsInt > defMaxPartnsInt)
                    topicRequestReq.setTopicpartitions(defMaxPartns);
                else if(topicPartitionsInt > 0)
                    topicRequestReq.setTopicpartitions(topicPartitions);
                else
                    topicRequestReq.setTopicpartitions(defPartns);
            } else
                topicRequestReq.setTopicpartitions(defPartns);

            topicRequestReq.setReplicationfactor(defaultRf);
        }catch (Exception e){
            log.error("Unable to set topic partitions, setting default from properties.");
            try{
                Integer.parseInt(defPartns);
                topicRequestReq.setTopicpartitions(defPartns);
            }catch(Exception e1){
                throw new KafkawizeException("Cluster default parameters config missing/incorrect.");
            }
        }
        return true;
    }

    public String updateSyncTopics(String updatedSyncTopics, String envSelected) {
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedSU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        StringTokenizer strTkr = new StringTokenizer(updatedSyncTopics,"\n");
        String topicSel, teamSelected, tmpToken;
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
        if(listTopics.size()>0){
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().addToSynctopics(listTopics)+"\"}";
        }
        else
            return "{\"result\":\"No record updated.\"}";
    }

    public List<TopicRequest> getTopicRequests(String pageNo) {
        UserDetails userDetails = getUserDetails();
        List<TopicRequest> topicReqs = manageDatabase.getHandleDbRequests().getAllTopicRequests(userDetails.getUsername());
        topicReqs = topicReqs.stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(TopicRequest::getRequesttime)))
                .collect(Collectors.toList());
        topicReqs = getTopicRequestsPaged(topicReqs, pageNo);

        return topicReqs;
    }

    public List<TopicRequest> getTopicRequestsPaged(List<TopicRequest> origActivityList, String pageNo){

        List<TopicRequest> newList = new ArrayList<>();

        if(origActivityList!=null && origActivityList.size() > 0) {
            int totalRecs = origActivityList.size();
            int recsPerPage = 10;

            int requestPageNo = Integer.parseInt(pageNo);
            int startVar = (requestPageNo - 1) * recsPerPage;
            int lastVar = (requestPageNo) * (recsPerPage);

            int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);

            List<String> numList = new ArrayList<>();
            for (int k = 1; k <= totalPages; k++) {
                numList.add("" + k);
            }
            for (int i = 0; i < totalRecs; i++) {
                TopicRequest activityLog = origActivityList.get(i);
                if (i >= startVar && i < lastVar) {
                    activityLog.setAllPageNos(numList);
                    activityLog.setTotalNoPages("" + totalPages);

                    newList.add(activityLog);
                }
            }
        }

        return newList;
    }

    public Topic getTopicTeam(String topicName, String env) {
        return manageDatabase.getHandleDbRequests().getTopicTeam(topicName, env);
    }

    public List<List<TopicRequest>> getCreatedTopicRequests() {
        UserDetails userDetails = getUserDetails();
        return updateCreateTopicReqsList(manageDatabase.getHandleDbRequests().getCreatedTopicRequests(userDetails.getUsername()));
    }

    private List<List<TopicRequest>> updateCreateTopicReqsList(List<TopicRequest> topicsList){

        topicsList = topicsList.stream().sorted(Comparator.comparing(TopicRequest::getRequesttime)).collect(Collectors.toList());

        List<List<TopicRequest>> newList = new ArrayList<>();
        List<TopicRequest> innerList = new ArrayList<>();
        int modulusFactor = 2;
        int i=0;
        for(TopicRequest topicInfo : topicsList){

            innerList.add(topicInfo);

            if(i%modulusFactor == (modulusFactor-1)) {
                newList.add(innerList);
                innerList = new ArrayList<>();
            }
            i++;
        }

        if(innerList.size()>0)
            newList.add(innerList);

        return newList;
    }

    public String deleteTopicRequests(String topicName) {

        StringTokenizer strTkr = new StringTokenizer(topicName,",");
        topicName = strTkr.nextToken();
        String env = strTkr.nextToken();

        String deleteTopicReqStatus = manageDatabase.getHandleDbRequests().deleteTopicRequest(topicName,env);

        return "{\"result\":\""+deleteTopicReqStatus+"\"}";
    }

    public String approveTopicRequests(String topicName, String env) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedAdmin_SU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        TopicRequest topicRequest = manageDatabase.getHandleDbRequests().selectTopicRequestsForTopic(topicName, env);

        ResponseEntity<String> response = clusterApiService.approveTopicRequests(topicName,topicRequest);

        String updateTopicReqStatus = response.getBody();

        if(response.getBody().equals("success"))
            updateTopicReqStatus = manageDatabase.getHandleDbRequests().updateTopicRequest(topicRequest, userDetails.getUsername());

        return "{\"result\":\""+updateTopicReqStatus+"\"}";
    }

    public String declineTopicRequests(String topicName, String env) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedAdmin_SU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        TopicRequest topicRequest = manageDatabase.getHandleDbRequests().selectTopicRequestsForTopic(topicName, env);

        String result = manageDatabase.getHandleDbRequests().declineTopicRequest(topicRequest, userDetails.getUsername());

        return "{\"result\":\""+ "Request declined. " + result + "\"}";
    }

    public List<String> getAllTopics(String env) throws Exception {

        Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(env);
        String bootstrapHost = envSelected.getHost() + ":" + envSelected.getPort();

        List<String> topicsList = clusterApiService.getAllTopics(bootstrapHost);

        List<String> topicsListNew = new ArrayList<>();

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

        Env envSelected = manageDatabase.getHandleDbRequests().selectEnvDetails(env);
        String bootstrapHost = envSelected.getHost() + ":" + envSelected.getPort();

        List<String> topicsList = clusterApiService.getAllTopics(bootstrapHost);

        // Get Sync topics
        List<Topic> topicsFromSOT = manageDatabase.getHandleDbRequests().getSyncTopics(env);

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

        if(topicListUpdated!=null && topicListUpdated.size() > 0)
            return getNewList(topicListUpdated);

        return null;
    }

    private List<List<TopicInfo>> getNewList(List<TopicInfo> topicsList){

        List<List<TopicInfo>> newList = new ArrayList<>();
        List<TopicInfo> innerList = new ArrayList<>();
        int modulusFactor = 3;
        int i=0;
        for(TopicInfo topicInfo : topicsList){

            innerList.add(topicInfo);

            if(i%modulusFactor == (modulusFactor-1)) {
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
        UserDetails userDetails = getUserDetails();

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        Env envSelected= manageDatabase.getHandleDbRequests().selectEnvDetails(env);
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

    private int topicCounter=0;
    private int counterIncrement()
    {
        topicCounter++;
        return topicCounter;
    }

    private List<TopicInfo> getTopicList(List<String> topicsList, List<Topic> topicsFromSOT, String pageNo){
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
                            return a.getTopicPK().getTopicname().equals(tmpTopicName);
                        }).findFirst().get().getTeamname();
                    }catch (Exception ignored){}

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

    private List<TopicRequest> getSyncTopicList(List<String> topicsList, UserDetails userDetails, String pageNo, String env){
        int totalRecs = topicsList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        // Get Sync topics
        List<Topic> topicsFromSOT = manageDatabase.getHandleDbRequests().getSyncTopics(env);

        List<TopicRequest> topicsListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        StringTokenizer strTkr = null;

        List<String> teamList = new ArrayList<>();

        manageDatabase.getHandleDbRequests().selectAllTeamsOfUsers(userDetails.getUsername())
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
                            return a.getTopicPK().getTopicname().equals(tmpTopicName);
                        }).findFirst().get().getTeamname();
                    }catch (Exception ignored){}

                    if(teamUpdated!=null && !teamUpdated.equals("undefined")){
//                        List<String> tmpList = new ArrayList<String>();
//                        tmpList.add(teamUpdated);
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

    private UserDetails getUserDetails(){
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
