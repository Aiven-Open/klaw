package com.kafkamgt.uiapi.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Service
public class UtilControllerService {

    private static Logger LOG = LoggerFactory.getLogger(UtilControllerService.class);

    @Autowired
    ManageTopics createTopicHelper;

    @Value("${custom.clusterapi.url}")
    String clusterConnUrl;

    @Value("${custom.clusterapi.username}")
    String clusterApiUser;

    @Value("${custom.clusterapi.password}")
    String clusterApiPwd;

    @Value("${custom.app.company.name}")
    String companyInfo;


    public String getAuth() {

        UserDetails userDetails = null;
        try {
             userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }catch (Exception e){}
        String json = null;
        if(userDetails!=null) {
        //LOG.info("User is " + userDetails.getUsername() + userDetails.getAuthorities());

            String teamName = createTopicHelper.getUsersInfo(userDetails.getUsername()).getTeam();
            GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
            String authority = ga.getAuthority();

            //LOG.info("auth is " + authority);
            String statusAuth = null;
            String statusAuthExecTopics = null;
            String licenseValidity=null;

            HashMap<String, String> outstanding = createTopicHelper.getAllRequestsToBeApproved(userDetails.getUsername());
            String outstandingTopicReqs = outstanding.get("topics");
            int outstandingTopicReqsInt = Integer.parseInt(outstandingTopicReqs);
            String outstandingAclReqs = outstanding.get("acls");
            int outstandingAclReqsInt = Integer.parseInt(outstandingAclReqs);

            if(outstandingTopicReqsInt<=0)
                outstandingTopicReqs = "";

            if(outstandingAclReqsInt<=0)
                outstandingAclReqs = "";

            if (authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")) {
                statusAuth = "Authorized";
            } else {
                statusAuth = "NotAuthorized";
            }

            if (authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER"))
                statusAuthExecTopics = "Authorized";
            else
                statusAuthExecTopics = "NotAuthorized";

            return "{ \"status\": \"" + statusAuth + "\" ," +
                    " \"username\":\"" + userDetails.getUsername() + "\"," +
                    " \"teamname\": \"" + teamName + "\"," +
                    " \"companyinfo\": \"" + companyInfo + "\"," +
                    " \"notifications\": \"" + outstandingTopicReqs + "\"," +
                    " \"notificationsAcls\": \"" + outstandingAclReqs + "\"," +
                    " \"statusauthexectopics\": \"" + statusAuthExecTopics + "\" }";
        }
        else return null;
    }

    public String getExecAuth() {

        UserDetails userDetails =
                (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String teamName = createTopicHelper.getUsersInfo(userDetails.getUsername()).getTeam();

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        String json = null;
        String statusAuth = null;

        if(authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER"))
            //statusAuth = userDetails.getUsername() +"-"+"Authorized";
            statusAuth = "Authorized";
        else
            //statusAuth = userDetails.getUsername() +"-"+"Not Authorized";
            statusAuth = "NotAuthorized";

        return "{ \"status\": \""+statusAuth+"\" , " +
                " \"companyinfo\": \"" + companyInfo + "\"," +
                " \"teamname\": \"" + teamName + "\"," +
                "\"username\":\""+userDetails.getUsername()+"\" }";

    }

    public void getLogoutPage(HttpServletRequest request, HttpServletResponse response){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null)
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        LOG.info("in logout..");
    }

}
