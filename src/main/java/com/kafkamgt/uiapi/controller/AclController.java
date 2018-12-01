package com.kafkamgt.uiapi.controller;


import com.google.gson.Gson;
import com.kafkamgt.uiapi.dao.*;
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
import java.util.*;


@RestController
@RequestMapping("/")
public class AclController {

    private static Logger LOG = LoggerFactory.getLogger(AclController.class);

    @Autowired
    ManageTopics createTopicHelper;

    @Value("${clusterapi.url}")
    String clusterConnUrl;

    @Value("${clusterapi.username}")
    String clusterApiUser;

    @Value("${clusterapi.password}")
    String clusterApiPwd;

    @PostMapping(value = "/createAcl")
    public ResponseEntity<String> createAcl(@RequestParam ("addAclRequest") String addAclRequest) {

        LOG.info("*********"+addAclRequest);
        Gson gson = new Gson();

        AclReq aclReq = gson.fromJson(addAclRequest, AclReq.class);

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        aclReq.setUsername(userDetails.getUsername());

        String execRes = null;

        execRes = createTopicHelper.requestForAcl(aclReq);
        String topicaddResult = "{\"result\":\""+execRes+"\"}";
        LOG.info(topicaddResult);
        return new ResponseEntity<String>(topicaddResult, HttpStatus.OK);
    }

    @PostMapping(value = "/updateSyncAcls")
    public ResponseEntity<String> updateSyncAcls(@RequestParam ("updatedSyncAcls") String updateSyncAcls,
                                                   @RequestParam ("envSelected") String envSelected) {

        //LOG.info("in updateSyncAcls" + updateSyncAcls );
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

        StringTokenizer strTkr = new StringTokenizer(updateSyncAcls,"\n");
        String topicSel=null,teamSelected=null,consumerGroup=null,aclIp=null,aclSsl=null,aclType=null,tmpToken=null;
        List<AclReq> listtopics = new ArrayList<>();
        AclReq t = null;

        StringTokenizer strTkrIn = null;
        while(strTkr.hasMoreTokens()){
            tmpToken = strTkr.nextToken().trim();
            strTkrIn = new StringTokenizer(tmpToken,"-----");
            while(strTkrIn.hasMoreTokens()){
                t = new AclReq();

                topicSel = strTkrIn.nextToken();
                teamSelected = strTkrIn.nextToken();
                consumerGroup = strTkrIn.nextToken();
                aclIp =strTkrIn.nextToken();
                aclSsl = strTkrIn.nextToken();
                aclType = strTkrIn.nextToken();

                t.setTopicname(topicSel);
                t.setConsumergroup(consumerGroup);
                t.setAcl_ip(aclIp);
                t.setAcl_ssl(aclSsl);
                t.setTeamname(teamSelected);
                t.setEnvironment(envSelected);
                t.setTopictype(aclType);

                listtopics.add(t);
            }

        }

        String execDelete = createTopicHelper.deletePrevAclRecs(listtopics);

        String execRes = createTopicHelper.addToSyncacls(listtopics);

        String topicaddResult = "{\"result\":\""+execRes+"\"}";
        return new ResponseEntity<String>(topicaddResult, HttpStatus.OK);
    }

    @RequestMapping(value = "/getAclRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AclReq>> getAclRequests() {

        List<AclReq> topicReqs = null;
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            topicReqs = createTopicHelper.getAllAclRequests(userDetails.getUsername());


        return new ResponseEntity<List<AclReq>>(topicReqs, HttpStatus.OK);
    }



    @RequestMapping(value = "/getCreatedAclRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AclReq>> getCreatedAclRequests() {

        List<AclReq> topicReqs = null;
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            topicReqs = createTopicHelper.getCreatedAclRequests(userDetails.getUsername());

        LOG.info("*****getCreatedTopicRequests"+topicReqs);
        return new ResponseEntity<List<AclReq>>(topicReqs, HttpStatus.OK);
    }



    @RequestMapping(value = "/deleteAclRequests", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteAclRequests(@RequestParam("req_no") String req_no) {

        LOG.info("In delete req "+req_no);
        String deleteTopicReqStatus = createTopicHelper.deleteAclRequest(req_no);

        deleteTopicReqStatus = "{\"result\":\""+deleteTopicReqStatus+"\"}";
        return new ResponseEntity<String>(deleteTopicReqStatus, HttpStatus.OK);
    }



    @PostMapping(value = "/execAclRequest")
    public ResponseEntity<String> approveAclRequests(@RequestParam("req_no") String req_no) {

        AclReq aclReq = createTopicHelper.selectAcl(req_no);
        String topicName = aclReq.getTopicname();
        String env = aclReq.getEnvironment();
        String acl_ip = aclReq.getAcl_ip();
        String acl_ssl = aclReq.getAcl_ssl();
        String consumerGroup = aclReq.getConsumergroup();
        String aclType = aclReq.getTopictype();

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info(env+"--In approve req--"+topicName+"---"+ acl_ip + acl_ssl);

        String uri = clusterConnUrl+"/topics/createAcls";
        LOG.info("URI is:"+uri);
        RestTemplate restTemplate = new RestTemplate();

        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();
        params.add("env",bootstrapHost);
        params.add("topicName",topicName);
        params.add("consumerGroup",consumerGroup);
        params.add("aclType",aclType);
        params.add("acl_ip",acl_ip);
        params.add("acl_ssl",acl_ssl);

        HttpHeaders headers = new HttpHeaders();//createHeaders("user1", "pwd");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params, headers);

        ResponseEntity<String> response = restTemplate.postForEntity( uri, request , String.class );

        String updateAclReqStatus = response.getBody();

        if(response.getBody().equals("success"))
            updateAclReqStatus = createTopicHelper.updateAclRequest(req_no,userDetails.getUsername());

        updateAclReqStatus = "{\"result\":\""+updateAclReqStatus+"\"}";
        return new ResponseEntity<String>(updateAclReqStatus, HttpStatus.OK);
    }

    @RequestMapping(value = "/getAcls", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AclInfo>> getAcls(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo) {

        LOG.info(pageNo+" In get acls " + env);
        String json = "{ \"name\": \"John\" }";

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("User is "+userDetails.getUsername()+ userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")){}
        else{
            json = "{ \"name\": \"Not Authorized\" }";
            List<AclInfo> topicsList1 = new ArrayList();
            return new ResponseEntity<List<AclInfo>>(topicsList1, HttpStatus.OK);
        }

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uri = clusterConnUrl+"/topics/getAcls/"+bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<HashMap<String,String>>> entity = new HttpEntity<>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uri, HttpMethod.GET, entity, Set.class);
        List<HashMap<String,String>> aclList = new ArrayList(s.getBody());

        // Get Sync acls
        List<AclReq> aclsFromSOT = createTopicHelper.getSyncAcls(env);

        topicCounter = 0;

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

                for(AclReq aclSotItem : aclsFromSOT){
                    String acl_ssl = aclSotItem.getAcl_ssl();
                    if(acl_ssl==null)
                        acl_ssl="User:*";

                    String acl_host = aclSotItem.getAcl_ip();
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

                //LOG.info(mp.getTopicname()+"-----------------"+mp.getTeamname());
                aclListMap.add(mp);
            }

        }

        // LOG.info("--startVar:"+startVar+"---lastVar:"+lastVar+"---"+topicsListMap.size());

        return new ResponseEntity<List<AclInfo>>(aclListMap, HttpStatus.OK);
    }

    @RequestMapping(value = "/getSyncAcls", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<List<AclInfo>> getSyncAcls(@RequestParam("env") String env, @RequestParam("pageNo") String pageNo) {

        LOG.info(pageNo+" In get getSyncAcls acls " + env);
        String json = "{ \"name\": \"John\" }";

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("User is "+userDetails.getUsername()+ userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        if(authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")){}
        else{
            json = "{ \"name\": \"Not Authorized\" }";
            List<AclInfo> topicsList1 = new ArrayList();
            return new ResponseEntity<List<AclInfo>>(topicsList1, HttpStatus.OK);
        }

        Env envSelected= createTopicHelper.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        String uri = clusterConnUrl+"/topics/getAcls/"+bootstrapHost;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = createHeaders(clusterApiUser, clusterApiPwd);
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<Set<HashMap<String,String>>> entity = new HttpEntity<>(headers);

        ResponseEntity<Set> s = restTemplate.exchange
                (uri, HttpMethod.GET, entity, Set.class);
        List<HashMap<String,String>> aclList = new ArrayList(s.getBody());

        // Get Sync acls
        List<AclReq> aclsFromSOT = createTopicHelper.getSyncAcls(env);

        topicCounter = 0;

        int totalRecs = aclList.size();
        int recsPerPage = 20;

        int totalPages = totalRecs/recsPerPage + (totalRecs%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);

        List<AclInfo> aclListMap = new ArrayList<>();
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);

        List<String> teamList = new ArrayList<>();

        createTopicHelper.selectAllTeamsOfUsers(userDetails.getUsername())
                .forEach(teamS->teamList.add(teamS.getTeamname()));

        LOG.info("------- aclList : "+aclList.size() + "  aclsFromSOT" +aclsFromSOT.size());

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

                    for(AclReq aclSotItem : aclsFromSOT){
                        String acl_ssl = aclSotItem.getAcl_ssl();
                        if(acl_ssl==null)
                            acl_ssl="User:*";

                        String acl_host = aclSotItem.getAcl_ip();
                        if(acl_host==null)
                            acl_host="*";

                      //  LOG.info("------- aclListItem"+aclListItem);
                        LOG.info("******* aclSotItem"+aclSotItem.getAcl_ssl()+"--"+aclSotItem.getAcl_ip()+"--"+aclSotItem.getTopicname()+"--"+aclSotItem.getConsumergroup());
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

        // LOG.info("--startVar:"+startVar+"---lastVar:"+lastVar+"---"+topicsListMap.size());

        return new ResponseEntity<List<AclInfo>>(aclListMap, HttpStatus.OK);
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
