package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.AclInfo;
import com.kafkamgt.uiapi.model.SyncAclUpdates;
import com.kafkamgt.uiapi.model.TopicInfo;
import com.kafkamgt.uiapi.model.TopicOverview;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.kafkamgt.uiapi.dao.MailType.*;
import static org.springframework.beans.BeanUtils.copyProperties;


@Service
public class AclControllerService {
    @Autowired
    ManageDatabase manageDatabase;
    
    @Autowired
    private final UtilService utilService;

    @Autowired
    private final
    ClusterApiService clusterApiService;

    AclControllerService(ClusterApiService clusterApiService, UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.utilService = utilService;
    }

    public String createAcl(AclRequests aclReq) {

        UserDetails userDetails = getUserDetails();
        aclReq.setAclType("Create");
        aclReq.setUsername(userDetails.getUsername());

        String execRes = manageDatabase.getHandleDbRequests().requestForAcl(aclReq);

        return "{\"result\":\""+execRes+"\"}";
    }

    public HashMap<String, String> updateSyncAcls(List<SyncAclUpdates> syncAclUpdates) {
        UserDetails userDetails = getUserDetails();
        HashMap<String, String> response = new HashMap<>();

        if(!utilService.checkAuthorizedSU(userDetails)){
            response.put("result", "Not Authorized.");
            return response;
        }

        List<Acl> listTopics = new ArrayList<>();
        Acl t;

        if(syncAclUpdates != null && syncAclUpdates.size() > 0){
            Set<String> sequences = new HashSet<>();
            syncAclUpdates.forEach(updateItem ->sequences.add(updateItem.getSequence()));
            HashMap<String,SyncAclUpdates> stringSyncAclUpdatesHashMap = new HashMap<>();

            // remove duplicates
            for(SyncAclUpdates syncAclUpdateItem: syncAclUpdates){
                if(stringSyncAclUpdatesHashMap.containsKey(syncAclUpdateItem.getSequence())){
                    stringSyncAclUpdatesHashMap.remove(syncAclUpdateItem.getSequence());
                    stringSyncAclUpdatesHashMap.put(syncAclUpdateItem.getSequence(), syncAclUpdateItem);
                }
                else
                    stringSyncAclUpdatesHashMap.put(syncAclUpdateItem.getSequence(), syncAclUpdateItem);
            }

            for (Map.Entry<String, SyncAclUpdates> stringSyncAclUpdatesEntry : stringSyncAclUpdatesHashMap.entrySet()) {
                SyncAclUpdates syncAclUpdateItem = stringSyncAclUpdatesEntry.getValue();

                t = new Acl();

                t.setReq_no(syncAclUpdateItem.getReq_no());
                t.setTopicname(syncAclUpdateItem.getTopicName());
                t.setConsumergroup(syncAclUpdateItem.getConsumerGroup());
                t.setAclip(syncAclUpdateItem.getAclIp());
                t.setAclssl(syncAclUpdateItem.getAclSsl());
                t.setTeamname(syncAclUpdateItem.getTeamSelected());
                t.setEnvironment(syncAclUpdateItem.getEnvSelected());
                t.setTopictype(syncAclUpdateItem.getAclType());

                listTopics.add(t);
            }
        }
        else
        {
            response.put("result", "No record updated.");
            return response;
        }

        try{
            if(listTopics.size()>0){
                response.put("result", manageDatabase.getHandleDbRequests().addToSyncacls(listTopics));
            }
        }catch(Exception e){
            response.put("result", "Failure." + e.toString());
        }
        return response;
    }

    public List<AclRequests> getAclRequests(String pageNo) {

        UserDetails userDetails = getUserDetails();

        List<AclRequests> aclReqs = manageDatabase.getHandleDbRequests().getAllAclRequests(userDetails.getUsername());
        aclReqs = aclReqs.stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(AclRequests::getRequesttime)))
                .collect(Collectors.toList());

        aclReqs = getAclRequestsPaged(aclReqs, pageNo);
        return aclReqs;
    }

    public List<AclRequests> getAclRequestsPaged(List<AclRequests> origActivityList, String pageNo){

        List<AclRequests> newList = new ArrayList<>();

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
                AclRequests activityLog = origActivityList.get(i);
                if (i >= startVar && i < lastVar) {
                    activityLog.setAllPageNos(numList);
                    activityLog.setTotalNoPages("" + totalPages);

                    newList.add(activityLog);
                }
            }
        }

        return newList;
    }

    public List<List<AclRequests>> getCreatedAclRequests() {
        UserDetails userDetails = getUserDetails();

        List<List<AclRequests>> updatedAclReqs = updateCreatAclReqsList(manageDatabase.getHandleDbRequests()
                .getCreatedAclRequests(userDetails.getUsername()));

        return updatedAclReqs;
    }

    private List<List<AclRequests>> updateCreatAclReqsList(List<AclRequests> topicsList){

        topicsList = topicsList.stream().sorted(Comparator.comparing(AclRequests::getRequesttime)).collect(Collectors.toList());
        List<List<AclRequests>> newList = new ArrayList<>();
        List<AclRequests> innerList = new ArrayList<>();
        int modulusFactor = 1;
        int i=0;
        for(AclRequests topicInfo : topicsList){

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

    public String deleteAclRequests(String req_no) {
        try {
            return "{\"result\":\""+manageDatabase.getHandleDbRequests().deleteAclRequest(req_no)+"\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }

    public String approveAclRequests(String req_no) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedAdmin_SU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        AclRequests aclReq = dbHandle.selectAcl(req_no);
        if(aclReq.getReq_no() != null){
            ResponseEntity<String> response = clusterApiService.approveAclRequests(aclReq);

            String updateAclReqStatus ;

            try {
                if (response!=null && Objects.equals(response.getBody(), "success"))
                    updateAclReqStatus = dbHandle.updateAclRequest(aclReq, userDetails.getUsername());
                else
                    return "{\"result\":\"failure\"}";
            }catch(Exception e){
                return "{\"result\":\"failure "+e.toString()+"\"}";
            }

            return "{\"result\":\""+updateAclReqStatus+"\"}";
        }
        else
            return "{\"result\":\"Record not found !\"}";
    }

    public String declineAclRequests(String req_no) {
        UserDetails userDetails = getUserDetails();
        if(!utilService.checkAuthorizedAdmin_SU(userDetails))
            return "{\"result\":\"Not Authorized\"}";

        HandleDbRequests dbHandle = manageDatabase.getHandleDbRequests();
        AclRequests aclReq = dbHandle.selectAcl(req_no);
        String updateAclReqStatus ;

        if(aclReq.getReq_no() != null){
            try {
                 updateAclReqStatus = dbHandle.declineAclRequest(aclReq, userDetails.getUsername());

                 return "{\"result\":\""+updateAclReqStatus+"\"}";
            }catch(Exception e){
                 return "{\"result\":\"failure "+e.toString()+"\"}";
            }
        }else
            return "{\"result\":\"Record not found !\"}";
    }

    private List<HashMap<String, String>> getAclListFromCluster(String bootstrapHost, boolean isSyncAcls, String topicNameSearch) throws KafkawizeException {
        List<HashMap<String, String>> aclList ;
        aclList = clusterApiService.getAcls(bootstrapHost);
        if(isSyncAcls)
            return updateConsumerGroups(groupAcls(aclList, topicNameSearch, true), aclList);
        else
            return updateConsumerGroups(groupAcls(aclList, topicNameSearch, false), aclList);
    }

    private List<HashMap<String, String>> updateConsumerGroups(List<HashMap<String, String>> groupedList, List<HashMap<String, String>> clusterAclList){
        List<HashMap<String, String>> updateList = new ArrayList<>(groupedList);

        for(HashMap<String, String> hMapGroupItem : groupedList){
            for(HashMap<String, String> hMapItem : clusterAclList){
                if(hMapGroupItem.get("operation").equals("READ") && hMapItem.get("operation").equals("READ") &&
                        hMapItem.get("resourceType").equals("GROUP")){
                    if(hMapItem.get("host").equals(hMapGroupItem.get("host")) && hMapItem.get("principle").equals(hMapGroupItem.get("principle"))){
                        HashMap<String, String> hashMap = new HashMap<>(hMapGroupItem);
                        hashMap.put("consumerGroup",hMapItem.get("resourceName"));
                        updateList.add(hashMap);
                        break;
                    }
                }
            }
        }
        return updateList;
    }

    private List<HashMap<String, String>> groupAcls(List<HashMap<String, String>> aclList, String topicNameSearch, boolean isSync){
        List<HashMap<String, String>> filteredList = aclList.stream()
                .filter(hItem->{
                    if(isSync){
                        if(topicNameSearch!=null){
                            return hItem.get("resourceType").equals("TOPIC") && hItem.get("resourceName").contains(topicNameSearch);
                        }
                        else return hItem.get("resourceType").equals("TOPIC");
                    }
                    else {
                        return hItem.get("resourceName").equals(topicNameSearch);
                    }
                }).collect(Collectors.toList());

        return filteredList;
    }

    private List<Acl> getAclsFromSOT(String env, String topicNameSearch, boolean regex){
        List<Acl> aclsFromSOT;
        if(!regex)
            aclsFromSOT = manageDatabase.getHandleDbRequests().getSyncAcls(env, topicNameSearch);
        else{
            aclsFromSOT = manageDatabase.getHandleDbRequests().getSyncAcls(env);
            List<Acl> topicFilteredList = aclsFromSOT;
            // Filter topics on topic name for search
            if(topicNameSearch!=null && topicNameSearch.length()>0) {
                final String topicSearchFilter = topicNameSearch;
                topicFilteredList = aclsFromSOT.stream()
                        .filter(acl -> acl.getTopicname().contains(topicSearchFilter)
                        )
                        .collect(Collectors.toList());
            }
            aclsFromSOT = topicFilteredList;
        }

        return aclsFromSOT;
    }

    public TopicOverview getAcls(String topicNameSearch) throws KafkawizeException {
        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();
        else
            return null;

        List<Topic> topics = manageDatabase.getHandleDbRequests().getTopics(topicNameSearch);

        List<TopicInfo> topicInfoList = new ArrayList<>();
        for (Topic topic : topics) {
            TopicInfo topicInfo = new TopicInfo();
            topicInfo.setCluster(topic.getTopicPK().getEnvironment());
            topicInfo.setNoOfPartitions(topic.getNoOfPartitions());
            topicInfo.setNoOfReplcias(topic.getNoOfReplcias());
            topicInfoList.add(topicInfo);
        }
        List<Acl> aclsFromSOT = new ArrayList<>();
        List<AclInfo> aclInfo = new ArrayList<>();
        List<AclInfo> tmpAcl;
        TopicOverview topicOverview = new TopicOverview();

        for (TopicInfo topicInfo : topicInfoList) {
            aclsFromSOT.addAll(getAclsFromSOT(topicInfo.getCluster(), topicNameSearch, false));

            tmpAcl = applyFiltersAclsForSOT(topicInfo.getCluster(), aclsFromSOT)
                    .stream()
                    .collect(Collectors.groupingBy(AclInfo::getTopicname))
                    .get(topicNameSearch);
            
            if(tmpAcl != null)
                aclInfo.addAll(tmpAcl);
        }
        aclInfo = aclInfo.stream().distinct().collect(Collectors.toList());
        topicOverview.setAclInfoList(aclInfo);
        topicOverview.setTopicInfoList(topicInfoList);

        return topicOverview;
    }

    public List<AclInfo> getSyncAcls(String env, String pageNo, String topicNameSearch) throws KafkawizeException {
        UserDetails userDetails = getUserDetails();
        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        if(!utilService.checkAuthorizedSU(userDetails))
            return null;

        List<HashMap<String,String>> aclList;

        Env envSelected= manageDatabase.getHandleDbRequests().selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();
        aclList = getAclListFromCluster(bootstrapHost, true, topicNameSearch);

        List<Acl> aclsFromSOT = getAclsFromSOT(env, topicNameSearch, true);

        topicCounter = 0;
        return getAclsList(pageNo,applyFiltersAcls(env, aclList, aclsFromSOT, true));
    }

    private List<AclInfo> applyFiltersAclsForSOT(String env, List<Acl> aclsFromSOT){

        List<AclInfo> aclListMap = new ArrayList<>() ;
        AclInfo mp ;

        for(Acl aclSotItem : aclsFromSOT)
            {
                mp = new AclInfo();
                mp.setEnvironment(aclSotItem.getEnvironment());
                mp.setTopicname(aclSotItem.getTopicname());
                mp.setAcl_ip(aclSotItem.getAclip());
                mp.setAcl_ssl(aclSotItem.getAclssl());
                mp.setTeamname(aclSotItem.getTeamname());
                mp.setConsumergroup(aclSotItem.getConsumergroup());
                mp.setTopictype(aclSotItem.getTopictype());
                mp.setReq_no(aclSotItem.getReq_no());

                if(aclSotItem.getAclip()!=null || aclSotItem.getAclssl()!=null)
                    aclListMap.add(mp);
            }
        return aclListMap;
    }

    private List<AclInfo> applyFiltersAcls(String env, List<HashMap<String,String>> aclList, List<Acl> aclsFromSOT, boolean isSyncAcls){

        UserDetails userDetails = getUserDetails();
        List<AclInfo> aclListMap = new ArrayList<>() ;
        AclInfo mp ;
        List<String> teamList = new ArrayList<>();

        if(isSyncAcls) {
            manageDatabase.getHandleDbRequests().selectAllTeamsOfUsers(userDetails.getUsername())
                    .forEach(teamS -> teamList.add(teamS.getTeamname()));
        }

        for(HashMap<String,String> aclListItem : aclList)
        {
            mp = new AclInfo();
            mp.setEnvironment(env);

            if(isSyncAcls) {
                mp.setPossibleTeams(teamList);
                mp.setTeamname("");
            }

            String tmpPermType=aclListItem.get("operation");

            if(tmpPermType.equals("WRITE"))
                mp.setTopictype("Producer");
            else if(tmpPermType.equals("READ")){
                mp.setTopictype("Consumer");
                if(aclListItem.get("consumerGroup")!=null)
                    mp.setConsumergroup(aclListItem.get("consumerGroup"));
                else
                    continue;
            }

            if (aclListItem.get("resourceType").toLowerCase().equals("topic"))
                mp.setTopicname(aclListItem.get("resourceName"));

            mp.setAcl_ip(aclListItem.get("host"));
            mp.setAcl_ssl(aclListItem.get("principle"));

            for(Acl aclSotItem : aclsFromSOT)
            {
                String acl_ssl = aclSotItem.getAclssl();
                String acl_host = aclSotItem.getAclip();

                if(acl_ssl==null || acl_ssl.equals(""))
                    acl_ssl="User:*";
                else {
                    if(!acl_ssl.equals("User:*")){
                        acl_ssl = "User:" + acl_ssl;
                    }
                }

                if(acl_host==null || acl_host.equals(""))
                    acl_host="*";

                if( aclSotItem.getTopicname()!=null &&
                        aclListItem.get("resourceName").equals(aclSotItem.getTopicname()) &&
                        aclListItem.get("host").equals(acl_host) &&
                        aclListItem.get("principle").equals(acl_ssl) &&
                        aclSotItem.getTopictype().equals(mp.getTopictype()))
                {
                    mp.setTeamname(aclSotItem.getTeamname());
                    mp.setReq_no(aclSotItem.getReq_no());
                    break;
                }
            }
            if(mp.getTeamname()==null)
                mp.setTeamname("Unknown");
            aclListMap.add(mp);

        }
        return aclListMap;
    }

    private List<AclInfo> getAclsList(String pageNo, List<AclInfo> aclListMap){
        List<AclInfo> aclListMapUpdated = new ArrayList<>();

        int totalRecs = aclListMap.size();
        int recsPerPage = 20;

        int totalPages = aclListMap.size()/recsPerPage + (aclListMap.size()%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);
        for(int i=0;i<totalRecs;i++) {
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                AclInfo mp = aclListMap.get(i);
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

    private int topicCounter=0;
    private int counterIncrement()
    {
        topicCounter++;
        return topicCounter;
    }

    private UserDetails getUserDetails(){
        return (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }


}
