package com.kafkamgt.uiapi.controller;


import com.google.gson.Gson;
import com.kafkamgt.uiapi.dao.AclReq;
import com.kafkamgt.uiapi.dao.SchemaRequest;
import com.kafkamgt.uiapi.helpers.ManageTopics;
import com.kafkamgt.uiapi.helpers.Utilities;
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
import java.util.List;
import java.util.StringTokenizer;


@RestController
@RequestMapping("/")
public class UtilController {

    private static Logger LOG = LoggerFactory.getLogger(UtilController.class);

    @Autowired
    ManageTopics createTopicHelper;

    @Autowired
    Utilities utils;

    @Value("${clusterapi.url}")
    String clusterConnUrl;

    @Value("${clusterapi.username}")
    String clusterApiUser;

    @Value("${clusterapi.password}")
    String clusterApiPwd;

    @Value("${license.spec}")
    String companyInfo;


    @RequestMapping(value = "/getAuth", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getAuth() {

        UserDetails userDetails = null;
        try {
             userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }catch (Exception e){}
        String json = null;
        if(userDetails!=null) {
        LOG.info("User is " + userDetails.getUsername() + userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();

        LOG.info("auth is " + authority);
        String statusAuth = null;
        String statusAuthExecTopics = null;
        String licenseValidity=null;

        int outstanding = createTopicHelper.getAllRequestsToBeApproved(userDetails.getUsername());
        String outstandingReqs = "";
        if(outstanding>0)
            outstandingReqs = outstanding+"";

        if(!utils.validateLicense()){
            statusAuth = "NotAuthorized";
            statusAuthExecTopics = "NotAuthorized";
            licenseValidity = "Invalid License !! Pleae provide a valid license.";
            json = "{ \"status\": \"" + statusAuth + "\" , \"username\":\"" + userDetails.getUsername() + "\"," +
                    " \"statusauthexectopics\": \"" + statusAuthExecTopics + "\"," +
                    " \"companyinfo\": \"" + companyInfo + "\"," +
                    " \"alertmessage\": \"" + licenseValidity + "\" }";
            return new ResponseEntity<String>(json, HttpStatus.OK);
        }

        if (authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")) {
            statusAuth = "Authorized";
        } else {
            statusAuth = "NotAuthorized";
        }

        if (authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER"))
            statusAuthExecTopics = "Authorized";
        else
            statusAuthExecTopics = "NotAuthorized";

        json = "{ \"status\": \"" + statusAuth + "\" ," +
                " \"username\":\"" + userDetails.getUsername() + "\"," +
                " \"companyinfo\": \"" + companyInfo + "\"," +
                " \"notifications\": \"" + outstandingReqs + "\"," +
                " \"statusauthexectopics\": \"" + statusAuthExecTopics + "\" }";
    }
        return new ResponseEntity<String>(json, HttpStatus.OK);
    }

    @RequestMapping(value = "/getExecAuth", method = RequestMethod.GET, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> getExecAuth() {

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        LOG.info("User is "+userDetails.getUsername()+ userDetails.getAuthorities());

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        String json = null;
        LOG.info("auth is "+authority);
        String statusAuth = null;
        String licenseValidity=null;


        if(!utils.validateLicense()){
            statusAuth = "NotAuthorized";
            licenseValidity = "Invalid License !! Pleae provide a valid license.";
            json = "{ \"status\": \"" + statusAuth + "\" ," +
                    " \"username\":\"" + userDetails.getUsername() + "\"," +
                    " \"companyinfo\": \"" + companyInfo + "\"," +
                    ", \"alertmessage\": \"" + licenseValidity + "\" }";
            return new ResponseEntity<String>(json, HttpStatus.OK);
        }

        if(authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER"))
            //statusAuth = userDetails.getUsername() +"-"+"Authorized";
            statusAuth = "Authorized";
        else
            //statusAuth = userDetails.getUsername() +"-"+"Not Authorized";
            statusAuth = "NotAuthorized";

        json = "{ \"status\": \""+statusAuth+"\" , " +
                " \"companyinfo\": \"" + companyInfo + "\"," +
                "\"username\":\""+userDetails.getUsername()+"\" }";

        return new ResponseEntity<String>(json, HttpStatus.OK);
    }

    @GetMapping("/logout")
    public void getLogoutPage(HttpServletRequest request, HttpServletResponse response){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null)
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        LOG.info("in logout..");
        //return "redirect:/login";
    }

}
