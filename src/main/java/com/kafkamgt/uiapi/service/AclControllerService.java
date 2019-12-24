package com.kafkamgt.uiapi.service;


import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.model.AclInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class AclControllerService {

    private static Logger LOG = LoggerFactory.getLogger(AclControllerService.class);

    @Autowired
    ManageTopics manageTopics;

    @Autowired
    private UtilService utilService;

    @Autowired
    ClusterApiService clusterApiService;

    public AclControllerService(ClusterApiService clusterApiService, ManageTopics manageTopics, UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.manageTopics = manageTopics;
        this.utilService = utilService;
    }

    public String createAcl(AclRequests aclReq) {

        aclReq.setUsername(utilService.getUserName());

        String execRes = manageTopics.requestForAcl(aclReq);
        return "{\"result\":\""+execRes+"\"}";
    }

    public String updateSyncAcls(String updateSyncAcls, String envSelected) {

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        StringTokenizer strTkr = null;
        if(updateSyncAcls != null)
            strTkr = new StringTokenizer(updateSyncAcls,"\n");

        String topicSel, teamSelected, consumerGroup, aclIp, aclSsl, aclType, tmpToken;
        List<Acl> listTopics = new ArrayList<>();
        Acl t;

        StringTokenizer strTkrIn ;
        while(strTkr != null && strTkr.hasMoreTokens()){
            tmpToken = strTkr.nextToken().trim();
            strTkrIn = new StringTokenizer(tmpToken,"-----");
            while(strTkrIn.hasMoreTokens()){
                t = new Acl();

                topicSel = strTkrIn.nextToken();
                teamSelected = strTkrIn.nextToken();
                consumerGroup = strTkrIn.nextToken();
                aclIp =strTkrIn.nextToken();
                aclSsl = strTkrIn.nextToken();
                aclType = strTkrIn.nextToken();

                t.setTopicname(topicSel);
                t.setConsumergroup(consumerGroup);
                t.setAclip(aclIp);
                t.setAclssl(aclSsl);
                t.setTeamname(teamSelected);
                t.setEnvironment(envSelected);
                t.setTopictype(aclType);

                listTopics.add(t);
            }
        }

        try{
            if(listTopics.size()>0){
                String deleteResult = manageTopics.deletePrevAclRecs(listTopics);

                if(deleteResult.equals("success")) {
                    return "{\"result\":\""+manageTopics.addToSyncacls(listTopics)+"\"}";
                }
            }
            else
                return "{\"result\":\"No records to update\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
        return "{\"result\":\"failure\"}";
    }

    public List<AclRequests> getAclRequests() {
        return manageTopics.getAllAclRequests(utilService.getUserName());
    }

    public List<List<AclRequests>> getCreatedAclRequests() {

        List<List<AclRequests>> updatedAclReqs = updateCreatAclReqsList(manageTopics.getCreatedAclRequests(utilService.getUserName()));

        return updatedAclReqs;
    }

    private List<List<AclRequests>> updateCreatAclReqsList(List<AclRequests> topicsList){

        List<List<AclRequests>> newList = new ArrayList<>();
        List<AclRequests> innerList = new ArrayList<>();
        int modulusFactor = 3;
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
            return "{\"result\":\""+manageTopics.deleteAclRequest(req_no)+"\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }

    public String approveAclRequests(String req_no) throws KafkawizeException {

        AclRequests aclReq = manageTopics.selectAcl(req_no);
        if(aclReq.getReq_no() != null){
            ResponseEntity<String> response = clusterApiService.approveAclRequests(aclReq);

            String updateAclReqStatus ;

            try {
                if (response.getBody().equals("success"))
                    updateAclReqStatus = manageTopics.updateAclRequest(aclReq, utilService.getUserName());
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

        AclRequests aclReq = manageTopics.selectAcl(req_no);
        String updateAclReqStatus ;

        if(aclReq.getReq_no() != null){
            try {
                 updateAclReqStatus = manageTopics.declineAclRequest(aclReq, utilService.getUserName());
                 return "{\"result\":\""+updateAclReqStatus+"\"}";
            }catch(Exception e){
                 return "{\"result\":\"failure "+e.toString()+"\"}";
            }
        }else
            return "{\"result\":\"Record not found !\"}";
    }

    private List<HashMap<String, String>> getAclListFromCluster(String bootstrapHost, boolean isSyncAcls) throws KafkawizeException {
        List<HashMap<String, String>> aclList ;
        if(isSyncAcls)
            aclList = clusterApiService.getAcls(bootstrapHost).stream()
                    .filter(aclItem->aclItem.get("operation").equals("READ") && aclItem.get("resourceType").equals("GROUP"))
                    .collect(Collectors.toList());
        else
            aclList = clusterApiService.getAcls(bootstrapHost);

        return aclList;
    }

    private List<Acl> getAclsFromSOT(String env, String topicNameSearch){
        List<Acl> aclsFromSOT = manageTopics.getSyncAcls(env);

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

        return aclsFromSOT;
    }

    public List<AclInfo> getAcls(String env, String pageNo, String topicNameSearch, boolean isSyncAcls) throws KafkawizeException {

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        Env envSelected= manageTopics.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        List<HashMap<String,String>> aclList = getAclListFromCluster(bootstrapHost, isSyncAcls);
        List<Acl> aclsFromSOT = getAclsFromSOT(env, topicNameSearch);

        topicCounter = 0;

        if(!isSyncAcls){
            List<AclInfo> groupedAclsPerTopic  = getAclsList(pageNo,applyFiltersAcls(env, aclList, aclsFromSOT, isSyncAcls))
                .stream()
                .collect(Collectors.groupingBy(w -> w.getTopicname()))
                .get(topicNameSearch);

            return groupedAclsPerTopic;
        }else{
            return getAclsList(pageNo,applyFiltersAcls(env, aclList, aclsFromSOT, isSyncAcls));
        }
    }

    private List<AclInfo> applyFiltersAcls(String env, List<HashMap<String,String>> aclList, List<Acl> aclsFromSOT, boolean isSyncAcls){

        List<AclInfo> aclListMap = new ArrayList<>() ;
        AclInfo mp = new AclInfo();
        boolean addRecord;

        List<String> teamList = new ArrayList<>();

        if(isSyncAcls) {
            manageTopics.selectAllTeamsOfUsers(utilService.getUserName())
                    .forEach(teamS -> teamList.add(teamS.getTeamname()));
        }

        for(HashMap<String,String> aclListItem : aclList)
        {
            addRecord = false;
            for(Acl aclSotItem : aclsFromSOT)
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
                else if(tmpPermType.equals("READ") && aclListItem.get("resourceType").equals("GROUP"))
                    mp.setTopictype("Consumer");

                String acl_ssl = aclSotItem.getAclssl();
                if(acl_ssl==null)
                    acl_ssl="User:*";

                String acl_host = aclSotItem.getAclip();
                if(acl_host==null)
                    acl_host="*";

                if( aclSotItem.getTopicname()!=null && (aclListItem.get("resourceName").equals(aclSotItem.getTopicname()) ||
                        aclListItem.get("resourceName").equals(aclSotItem.getConsumergroup())) &&
                        aclListItem.get("host").equals(acl_host) &&
                        aclListItem.get("principle").equals(acl_ssl) &&
                        aclSotItem.getTopictype().equals(mp.getTopictype()))
                {
                    mp.setTeamname(aclSotItem.getTeamname());
                    mp.setTopicname(aclSotItem.getTopicname());
                    if(isSyncAcls)
                        mp.setReq_no(aclSotItem.getReq_no());

                    addRecord = true;
                    break;
                }
            }
            if(addRecord && !isSyncAcls)
            {
                if (aclListItem.get("resourceType").toLowerCase().equals("group")) {
                    mp.setConsumergroup(aclListItem.get("resourceName"));
                } else if (aclListItem.get("resourceType").toLowerCase().equals("topic"))
                    mp.setTopicname(aclListItem.get("resourceName"));

                mp.setAcl_ip(aclListItem.get("host"));
                mp.setAcl_ssl(aclListItem.get("principle"));
                aclListMap.add(mp);
            }
            else if(isSyncAcls){
                if (aclListItem.get("resourceType").toLowerCase().equals("group")) {
                    mp.setConsumergroup(aclListItem.get("resourceName"));
                } else if (aclListItem.get("resourceType").toLowerCase().equals("topic"))
                    mp.setTopicname(aclListItem.get("resourceName"));

                mp.setAcl_ip(aclListItem.get("host"));
                mp.setAcl_ssl(aclListItem.get("principle"));
                if(mp.getTopictype()!=null)
                    aclListMap.add(mp);
            }
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

    int topicCounter=0;
    private int counterIncrement()
    {
        topicCounter++;
        return topicCounter;
    }
}
