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
    ManageTopics manageTopics;

    @Autowired
    UtilService utilService;

    @Value("${custom.org.name}")
    String companyInfo;

    public UtilControllerService(ManageTopics manageTopics, UtilService utilService){
        this.manageTopics = manageTopics;
        this.utilService = utilService;
    }

    public String getAuth() {

        UserDetails userDetails = utilService.getUserDetails();

        if(userDetails!=null) {

            String teamName = manageTopics.getUsersInfo(userDetails.getUsername()).getTeam();
            String authority = utilService.getAuthority(userDetails);

            String statusAuth = null;
            String statusAuthExecTopics = null;

            HashMap<String, String> outstanding = manageTopics.getAllRequestsToBeApproved(userDetails.getUsername());
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

        UserDetails userDetails = utilService.getUserDetails();
        String teamName = manageTopics.getUsersInfo(userDetails.getUsername()).getTeam();

        String authority = utilService.getAuthority(userDetails);

        String statusAuth ;

        if(authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER"))
            statusAuth = "Authorized";
        else
            statusAuth = "NotAuthorized";

        return "{ \"status\": \""+statusAuth+"\" , " +
                " \"companyinfo\": \"" + companyInfo + "\"," +
                " \"teamname\": \"" + teamName + "\"," +
                "\"username\":\""+userDetails.getUsername()+"\" }";
    }

    public void getLogoutPage(HttpServletRequest request, HttpServletResponse response){

        Authentication authentication = utilService.getAuthentication();
        if (authentication != null)
            new SecurityContextLogoutHandler().logout(request, response, authentication);
    }

}
