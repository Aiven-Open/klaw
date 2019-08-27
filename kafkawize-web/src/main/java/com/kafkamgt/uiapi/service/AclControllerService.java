package com.kafkamgt.uiapi.service;


import com.google.gson.Gson;
import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.model.AclInfo;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
public class AclControllerService {

    private static Logger LOG = LoggerFactory.getLogger(AclControllerService.class);

    private String uriCreateAcls = "/topics/createAcls";

    private String uriGetAcls = "/topics/getAcls/";

    @Autowired
    ManageTopics createTopicHelper;

    @Value("${clusterapi.url}")
    String clusterConnUrl;

    @Value("${clusterapi.username}")
    String clusterApiUser;

    @Value("${clusterapi.password}")
    String clusterApiPwd;

    private String getUserName(){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }

    private UserDetails getUserDetails(){
        return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public String createAcl(AclRequests aclReq) {

        aclReq.setUsername(getUserName());

        String execRes = createTopicHelper.requestForAcl(aclReq);
        return "{\"result\":\""+execRes+"\"}";
    }

    public String updateSyncAcls(String updateSyncAcls, String envSelected) {

        UserDetails userDetails = getUserDetails();

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        if(authority.equals("ROLE_SUPERUSER")){}
        else{
            return "{ \"result\": \"Not Authorized\" }";
        }

        StringTokenizer strTkr = new StringTokenizer(updateSyncAcls,"\n");
        String topicSel=null,teamSelected=null,consumerGroup=null,aclIp=null,aclSsl=null,aclType=null,tmpToken=null;
        List<Acl> listtopics = new ArrayList<>();
        Acl t;

        StringTokenizer strTkrIn = null;
        while(strTkr.hasMoreTokens()){
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

                listtopics.add(t);
            }

        }

        createTopicHelper.deletePrevAclRecs(listtopics);

        String execRes = createTopicHelper.addToSyncacls(listtopics);

        return "{\"result\":\""+execRes+"\"}";
    }

    public List<AclRequests> getAclRequests() {
        return createTopicHelper.getAllAclRequests(getUserName());
    }

    public List<AclRequests> getCreatedAclRequests() {
        return createTopicHelper.getCreatedAclRequests(getUserName());
    }

    public String deleteAclRequests(String req_no) {
        String deleteTopicReqStatus = createTopicHelper.deleteAclRequest(req_no);
        return "{\"result\":\""+deleteTopicReqStatus+"\"}";
    }

    public String approveAclRequests(String req_no) {

        AclRequests aclReq = createTopicHelper.selectAcl(req_no);
        String env = aclReq.getEnvironment();

        String uri = clusterConnUrl + uriCreateAcls;
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();
        params.add("env",bootstrapHost);
        params.add("topicName",aclReq.getTopicname());
        params.add("consumerGroup",aclReq.getConsumergroup());
        params.add("aclType",aclReq.getTopictype());
        params.add("acl_ip",aclReq.getAcl_ip());
        params.add("acl_ssl",aclReq.getAcl_ssl());

        HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity( uri, request , String.class );

        String updateAclReqStatus = response.getBody();

        if(response.getBody().equals("success"))
            updateAclReqStatus = createTopicHelper.updateAclRequest(aclReq,getUserName());

        return "{\"result\":\""+updateAclReqStatus+"\"}";
    }

    public List<AclInfo> getAcls(String env, String pageNo) {

        UserDetails userDetails = getUserDetails();

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")){}
        else{
            List<AclInfo> topicsList1 = new ArrayList();
            return topicsList1;
        }

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uri = clusterConnUrl + uriGetAcls + bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<HashMap<String,String>>> entity = new HttpEntity<>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uri, HttpMethod.GET, entity, Set.class);
        List<HashMap<String,String>> aclList = new ArrayList(s.getBody());

        // Get Sync acls
        List<Acl> aclsFromSOT = createTopicHelper.getSyncAcls(env);

        topicCounter = 0;

        return getAclsList(pageNo,env,aclList,aclsFromSOT);
    }

    public List<AclInfo> getAclsList(String pageNo, String env, List<HashMap<String,String>> aclList, List<Acl> aclsFromSOT){

        int totalRecs = aclList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        List<AclInfo> aclListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        //  LOG.info("------- aclList : "+aclList.size() + "  aclsFromSOT" +aclsFromSOT.size());

        for(int i=0;i<totalRecs;i++){
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                AclInfo mp = new AclInfo();
                mp.setSequence(counterInc + "");
                mp.setEnvironment(env);
                HashMap<String,String> aclListItem = aclList.get(i);

                String tmpPermType=aclListItem.get("operation");

                //  LOG.info(aclListItem+"-------tmpPermType "+tmpPermType);
                if(tmpPermType.equals("WRITE"))
                    mp.setTopictype("Producer");
                else if(tmpPermType.equals("READ"))
                    mp.setTopictype("Consumer");

                for(Acl aclSotItem : aclsFromSOT){
                    String acl_ssl = aclSotItem.getAclssl();
                    if(acl_ssl==null)
                        acl_ssl="User:*";

                    String acl_host = aclSotItem.getAclip();
                    if(acl_host==null)
                        acl_host="*";

                    if( (aclListItem.get("resourceName").equals(aclSotItem.getTopicname()) ||
                            aclListItem.get("resourceName").equals(aclSotItem.getConsumergroup())) &&
                            aclListItem.get("host").equals(acl_host) && aclListItem.get("principle").equals(acl_ssl) &&
                            aclSotItem.getTopictype().equals(mp.getTopictype()))
                    {
                        mp.setTeamname(aclSotItem.getTeamname());
                        break;
                    }
                }

                if(aclListItem.get("resourceType").toLowerCase().equals("group"))
                    mp.setConsumergroup(aclListItem.get("resourceName"));
                else if(aclListItem.get("resourceType").toLowerCase().equals("topic"))
                    mp.setTopicname(aclListItem.get("resourceName"));

                mp.setAcl_ip(aclListItem.get("host"));
                mp.setAcl_ssl(aclListItem.get("principle"));

                mp.setTotalNoPages(totalPages + "");
                List<String> numList = new ArrayList<>();
                for (int k = 1; k <= totalPages; k++) {
                    numList.add("" + k);
                }
                mp.setAllPageNos(numList);
                aclListMap.add(mp);
            }

        }

        return aclListMap;
    }

    public List<AclInfo> getSyncAcls(String env, String pageNo) {

        UserDetails userDetails = getUserDetails();
        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_SUPERUSER")){}
        else{
            List<AclInfo> topicsList1 = new ArrayList();
            return topicsList1;
        }

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uri = clusterConnUrl + uriGetAcls + bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<HashMap<String,String>>> entity = new HttpEntity<>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uri, HttpMethod.GET, entity, Set.class);
        List<HashMap<String,String>> aclListOriginal = new ArrayList(s.getBody());

        List<HashMap<String,String>> aclList = aclListOriginal.stream()
                .filter(aclItem->aclItem.get("operation").equals("READ"))
                .collect(Collectors.toList());

        // Get Sync acls
        List<Acl> aclsFromSOT = createTopicHelper.getSyncAcls(env);

        topicCounter = 0;

        return getSyncAclList(pageNo, env, aclList, aclsFromSOT);
    }

    public List<AclInfo> getSyncAclList(String pageNo, String env, List<HashMap<String,String>> aclList, List<Acl> aclsFromSOT){
        int totalRecs = aclList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        List<AclInfo> aclListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        List<String> teamList = new ArrayList<>();

        createTopicHelper.selectAllTeamsOfUsers(getUserName())
                .forEach(teamS->teamList.add(teamS.getTeamname()));

        //LOG.info("------- aclList : "+aclList.size() + "  aclsFromSOT" +aclsFromSOT.size());

        for(int i=0;i<totalRecs;i++){
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                AclInfo mp = new AclInfo();
                mp.setSequence(counterInc + "");
                HashMap<String,String> aclListItem = aclList.get(i);

                String tmpPermType=aclListItem.get("operation");
                mp.setPossibleTeams(teamList);
                mp.setTeamname("");
                mp.setEnvironment(env);

                if(tmpPermType.equals("WRITE"))
                    mp.setTopictype("Producer");
                else if(tmpPermType.equals("READ"))
                    mp.setTopictype("Consumer");

                for(Acl aclSotItem : aclsFromSOT){
                    String acl_ssl = aclSotItem.getAclssl();
                    if(acl_ssl==null)
                        acl_ssl="User:*";

                    String acl_host = aclSotItem.getAclip();
                    if(acl_host==null)
                        acl_host="*";

                    //  LOG.info("------- aclListItem"+aclListItem);
                    // LOG.info("******* aclSotItem"+aclSotItem.getAclssl()+"--"+aclSotItem.getAclip()+"--"+aclSotItem.getTopicname()+"--"+aclSotItem.getConsumergroup());
                    if( (aclListItem.get("resourceName").equals(aclSotItem.getTopicname()) ||
                            aclListItem.get("resourceName").equals(aclSotItem.getConsumergroup())) &&
                            aclListItem.get("host").equals(acl_host) && aclListItem.get("principle").equals(acl_ssl) &&
                            aclSotItem.getTopictype().equals(mp.getTopictype()))
                    {
                        mp.setTeamname(aclSotItem.getTeamname());
                        mp.setReq_no(aclSotItem.getReq_no());
                        break;
                    }

                }

                if(aclListItem.get("resourceType").toLowerCase().equals("group"))
                    mp.setConsumergroup(aclListItem.get("resourceName"));
                else if(aclListItem.get("resourceType").toLowerCase().equals("topic"))
                    mp.setTopicname(aclListItem.get("resourceName"));

                mp.setAcl_ip(aclListItem.get("host"));
                mp.setAcl_ssl(aclListItem.get("principle"));

                mp.setTotalNoPages(totalPages + "");
                List<String> numList = new ArrayList<>();
                for (int k = 1; k <= totalPages; k++) {
                    numList.add("" + k);
                }
                mp.setAllPageNos(numList);
                aclListMap.add(mp);
            }

        }

        return aclListMap;
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
