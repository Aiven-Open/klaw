package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.dao.TopicRequest;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.SyncTopicUpdates;
import com.kafkamgt.uiapi.model.TopicInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TopicControllerService {

    @Value("${kafkawize.syncdata.cluster:DEV}")
    private String syncCluster;

    @Value("${kafkawize.envs.order}")
    private String orderOfEnvs;

    @Autowired
    private final
    ClusterApiService clusterApiService;

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    private final UtilService utilService;

    private int topicCounter=0;

    TopicControllerService(ClusterApiService clusterApiService, UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.utilService = utilService;
    }

    public String createTopicsRequest(TopicRequest topicRequestReq) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        log.info(topicRequestReq.getTopicname() + "---" +
                topicRequestReq.getTeamname() + "---" + topicRequestReq.getEnvironment() +
                "---" + topicRequestReq.getAppname());

        if(utilService.checkAuthorizedSU(userDetails)){
            return "{\"result\":\"Not Authorized\"}";
        }

        topicRequestReq.setRequestor(userDetails.getUsername());
        topicRequestReq.setUsername(userDetails.getUsername());
        topicRequestReq.setTopictype("Create");

        String topicPartitions = topicRequestReq.getTopicpartitions();

        String envSelected = topicRequestReq.getEnvironment();

        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

        List<Topic> topics = getTopicFromName(topicRequestReq.getTopicname());

        if(topics!=null && topics.size()>0
                && !topics.get(0).getTeamname().equals(topicRequestReq.getTeamname()))
            return "{\"result\":\"Failure. This topic is owned by a different team.\"}";

        if(topics!=null && topics.size()>0){
            int devTopicFound = (int) topics.stream().filter(topic -> topic.getEnvironment().equals(syncCluster)).count();
            if(devTopicFound != 1){
                return "{\"result\":\"Failure. This topic does not exist in "+ syncCluster + " cluster.\"}";
            }
        }
        else if(!topicRequestReq.getEnvironment().equals(syncCluster)){
            return "{\"result\":\"Failure. Please request for a topic first in "+ syncCluster + " cluster.\"}";
        }

        boolean topicExists = false;
        if(topics != null) {
            topicExists = topics.stream()
                            .anyMatch(topicEx-> topicEx.getTopicPK().getEnvironment().equals(topicRequestReq.getEnvironment())
                                    );
        }
        if(topicExists)
            return "{\"result\":\"Failure. This topic already exists in the selected cluster.\"}";

        Env env = dbHandle.selectEnvDetails(envSelected);

         if(validateParameters(topicRequestReq, env, topicPartitions)){
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().requestForTopic(topicRequestReq)+"\"}";
        }

        return "{\"result\":\"failure\"}";
    }

    private boolean validateParameters(TopicRequest topicRequestReq, Env env, String topicPartitions) throws KafkawizeException {
        String otherParams = env.getOtherParams();
        String[] params;
        String defPartns = null, defMaxPartns = null, defaultRf = null;
        int topicPartitionsInt;

        try{
            if(otherParams!=null) {
                params = otherParams.split(",");

                if(params.length == 3) {
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
                if(topicPartitions.contains("default"))
                    topicPartitions = topicPartitions.substring(0,topicPartitions.indexOf(" "));

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

    public List<TopicRequest> getTopicRequests(String pageNo) {
        UserDetails userDetails = getUserDetails();
        List<TopicRequest> topicReqs = manageDatabase.getHandleDbRequests().getAllTopicRequests(userDetails.getUsername());
        topicReqs = topicReqs.stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(TopicRequest::getRequesttime)))
                .collect(Collectors.toList());
        topicReqs = getTopicRequestsPaged(topicReqs, pageNo);

        return topicReqs;
    }

    private List<TopicRequest> getTopicRequestsPaged(List<TopicRequest> origActivityList, String pageNo){

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

    public List<Topic> getTopicFromName(String topicName) {
        return manageDatabase.getHandleDbRequests().getTopicTeam(topicName);
    }

    public String getTopicTeamOnly(String topicName) {
        List<Topic> topics = manageDatabase.getHandleDbRequests().getTopicTeam(topicName);
        if(topics.size()>0)
            return "{\"team\":\""+topics.get(0).getTeamname()+"\"}";
        else
            return null;
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

        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();

        if(Objects.equals(response.getBody(), "success"))
            updateTopicReqStatus = dbHandle.updateTopicRequest(topicRequest, userDetails.getUsername());

        return "{\"result\":\""+updateTopicReqStatus+"\"}";
    }

    public String declineTopicRequests(String topicName, String env) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedAdmin_SU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        TopicRequest topicRequest = dbHandle.selectTopicRequestsForTopic(topicName, env);

        String result = dbHandle.declineTopicRequest(topicRequest, userDetails.getUsername());

        return "{\"result\":\""+ "Request declined. " + result + "\"}";
    }

    public List<String> getAllTopics() throws Exception {

        List<Topic> topicsFromSOT = manageDatabase.getHandleDbRequests().getSyncTopics(null,null);

        List<String> topicsList = new ArrayList<>();
        topicsFromSOT.forEach(topic -> topicsList.add(topic.getTopicname()));

        return topicsList.stream().distinct().collect(Collectors.toList());
    }

    class TopicNameComparator implements Comparator<Topic> {
        @Override
        public int compare(Topic topic1, Topic topic2) {
            return topic1.getTopicname().compareTo(topic2.getTopicname());
        }
    }

    class TopicNameSyncComparator implements Comparator<HashMap<String, String>> {
        @Override
        public int compare(HashMap<String, String> topic1, HashMap<String, String> topic2) {
            return topic1.get("topicName").compareTo(topic2.get("topicName"));
        }
    }

    public List<List<TopicInfo>> getTopics(String env, String pageNo, String topicNameSearch, String teamName) throws Exception {

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        // Get Sync topics
        List<Topic> topicsFromSOT = manageDatabase.getHandleDbRequests().getSyncTopics(env, teamName);

        topicsFromSOT = groupTopicsByEnv(topicsFromSOT);

        List<Topic> topicFilteredList = topicsFromSOT;
        // Filter topics on topic name for search
        if(topicNameSearch!=null && topicNameSearch.length()>0){
            final String topicSearchFilter = topicNameSearch;
            topicFilteredList = topicsFromSOT.stream().filter(topic-> {
                return topic.getTopicname().contains(topicSearchFilter);
                    }
            ).collect(Collectors.toList());
        }

        topicsFromSOT = topicFilteredList.stream().sorted(new TopicNameComparator()).collect(Collectors.toList());

        List<TopicInfo> topicListUpdated = getTopicInfoList(topicsFromSOT, pageNo);

        if(topicListUpdated!=null && topicListUpdated.size() > 0)
            return getPagedList(topicListUpdated);

        return null;
    }

    private List<Topic> groupTopicsByEnv(List<Topic> topicsFromSOT) {
        List<Topic> tmpTopicList = new ArrayList<>();

        Map<String, List<Topic>> groupedList = topicsFromSOT.stream().collect(Collectors.groupingBy(Topic::getTopicname));
        groupedList.forEach((k,v)->{
            Topic t = v.get(0);
            List<String> tmpEnvList = new ArrayList<>();
            for (Topic topic : v) {
                tmpEnvList.add(topic.getTopicPK().getEnvironment());
            }
            t.setEnvironmentsList(tmpEnvList);
            tmpTopicList.add(t);
        });
        return tmpTopicList;
    }

    private List<List<TopicInfo>> getPagedList(List<TopicInfo> topicsList){

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

    private int counterIncrement()
    {
        topicCounter++;
        return topicCounter;
    }

    class TopicEnvComparator implements Comparator<String> {
        List<String> orderedEnv = Arrays.asList(orderOfEnvs.split(","));

        @Override
        public int compare(String topicEnv1, String topicEnv2) {
            if(orderedEnv.indexOf(topicEnv1) > orderedEnv.indexOf(topicEnv2))
                return 1;
            else
                return -1;
        }
    }

    private List<TopicInfo> getTopicInfoList(List<Topic> topicsFromSOT, String pageNo){
        int totalRecs = topicsFromSOT.size();
        int recsPerPage = 21;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);
        int requestPageNo = Integer.parseInt(pageNo);

        List<TopicInfo> topicsListMap = null;
        if(totalRecs>0)
            topicsListMap = new ArrayList<>();

        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);
        //HashMap<String, String> topicMap ;

        for(int i=0; i<topicsFromSOT.size(); i++){
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                TopicInfo mp = new TopicInfo();
                mp.setSequence(counterInc + "");
                Topic topicSOT = topicsFromSOT.get(i);

                List<String> envList = topicSOT.getEnvironmentsList();
                envList = envList.stream().sorted(new TopicEnvComparator()).collect(Collectors.toList());

                mp.setCluster(topicSOT.getTopicPK().getEnvironment());
                mp.setEnvironmentsList(envList);
                mp.setTopicName(topicSOT.getTopicname());
                mp.setTeamname(topicSOT.getTeamname());

                mp.setNoOfReplcias(topicSOT.getNoOfReplcias());
                mp.setNoOfPartitions(topicSOT.getNoOfPartitions());

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

    private List<TopicRequest> getSyncTopicList(List<HashMap<String, String>> topicsList, UserDetails userDetails, String pageNo, String env){
        int totalRecs = topicsList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        // Get Sync topics
        List<Topic> topicsFromSOT = manageDatabase.getHandleDbRequests().getSyncTopics(env, null);

        List<TopicRequest> topicsListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        List<String> teamList = new ArrayList<>();

        manageDatabase.getHandleDbRequests().selectAllTeamsOfUsers(userDetails.getUsername())
                .forEach(teamS->teamList.add(teamS.getTeamname()));

        HashMap<String, String> topicMap;

        for(int i=0; i < topicsList.size(); i++){
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                TopicRequest mp = new TopicRequest();
                mp.setSequence(counterInc + "");

                topicMap = topicsList.get(i);
                final String tmpTopicName = topicMap.get("topicName");

                mp.setTopicname(tmpTopicName);
                mp.setTopicpartitions(topicMap.get("partitions"));
                mp.setReplicationfactor(topicMap.get("replicationFactor"));
                String teamUpdated = null;

                try {
                    teamUpdated = topicsFromSOT.stream().filter(a -> {
                        return a.getTopicPK().getTopicname().equals(tmpTopicName);
                    }).findFirst().get().getTeamname();
                }catch (Exception ignored){}

                if(teamUpdated!=null && !teamUpdated.equals("undefined")){
                    mp.setPossibleTeams(teamList);
                    mp.setTeamname(teamUpdated);
                }
                else{
                    mp.setPossibleTeams(teamList);
                    mp.setTeamname("");
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
