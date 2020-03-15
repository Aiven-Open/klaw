package com.kafkamgt.uiapi.service;


import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@Service
@DependsOn(value={"utilService"})
public class UtilControllerService {

    //private HandleDbRequests handleDbRequests = ManageDatabase.handleDbRequests;

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    UtilService utilService;

    @Value("${custom.org.name}")
    String companyInfo;

    @Value("${custom.kafkawize.version:3.5}")
    String kafkawizeVersion;

    public UtilControllerService(UtilService utilService){
        this.utilService = utilService;
    }

    public HashMap<String, String> getAuth() {
        UserDetails userDetails = utilService.getUserDetails();

        if(userDetails!=null) {

            String teamName = manageDatabase.getHandleDbRequests().getUsersInfo(userDetails.getUsername()).getTeam();
            String authority = utilService.getAuthority(userDetails);

            String statusAuth = null;
            String statusAuthExecTopics, statusAuthExecTopicsSU;

            HashMap<String, String> outstanding = manageDatabase.getHandleDbRequests().getAllRequestsToBeApproved(userDetails.getUsername());
            String outstandingTopicReqs = outstanding.get("topics");
            int outstandingTopicReqsInt = Integer.parseInt(outstandingTopicReqs);
            String outstandingAclReqs = outstanding.get("acls");
            int outstandingAclReqsInt = Integer.parseInt(outstandingAclReqs);

            String outstandingSchemasReqs = outstanding.get("schemas");
            int outstandingSchemasReqsInt = Integer.parseInt(outstandingSchemasReqs);


            if(outstandingTopicReqsInt<=0)
                outstandingTopicReqs = "0";

            if(outstandingAclReqsInt<=0)
                outstandingAclReqs = "0";

            if(outstandingSchemasReqsInt<=0)
                outstandingSchemasReqs = "0";

            if (authority.equals("ROLE_USER") || authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER")) {
                statusAuth = "Authorized";
            } else {
                statusAuth = "NotAuthorized";
            }

            if (authority.equals("ROLE_ADMIN") || authority.equals("ROLE_SUPERUSER"))
                statusAuthExecTopics = "Authorized";
            else
                statusAuthExecTopics = "NotAuthorized";

            if (authority.equals("ROLE_SUPERUSER"))
                statusAuthExecTopicsSU = "Authorized";
            else
                statusAuthExecTopicsSU = "NotAuthorized";

            HashMap<String, String> dashboardData = manageDatabase.getHandleDbRequests().getDashboardInfo();

            dashboardData.put("status",statusAuth);
            dashboardData.put("username",userDetails.getUsername());
            dashboardData.put("teamname",teamName);
            dashboardData.put("companyinfo",companyInfo);
            dashboardData.put("kafkawizeversion",kafkawizeVersion);
            dashboardData.put("notifications",outstandingTopicReqs);
            dashboardData.put("notificationsAcls",outstandingAclReqs);
            dashboardData.put("notificationsSchemas",outstandingSchemasReqs);
            dashboardData.put("statusauthexectopics_su",statusAuthExecTopicsSU);
            dashboardData.put("statusauthexectopics",statusAuthExecTopics);

            return dashboardData;
        }
        else return null;
    }

    public String getExecAuth() {

        UserDetails userDetails = utilService.getUserDetails();
        String teamName = manageDatabase.getHandleDbRequests().getUsersInfo(userDetails.getUsername()).getTeam();

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
