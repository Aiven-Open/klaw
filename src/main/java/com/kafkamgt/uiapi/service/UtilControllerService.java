package com.kafkamgt.uiapi.service;


import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.charts.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
@DependsOn(value={"utilService"})
public class UtilControllerService {

    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    UtilService utilService;

    @Value("${kafkawize.org.name}")
    private
    String companyInfo;

    @Value("${kafkawize.version:5.0}")
    private
    String kafkawizeVersion;

    UtilControllerService(UtilService utilService){
        this.utilService = utilService;
    }

    public HashMap<String, String> getAuth() {
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
        if(userDetails!=null) {

            String teamName = reqsHandle.getUsersInfo(userDetails.getUsername()).getTeam();
            String authority = utilService.getAuthority(userDetails);

            String statusAuth;
            String statusAuthExecTopics, statusAuthExecTopicsSU;

            HashMap<String, String> outstanding = reqsHandle
                    .getAllRequestsToBeApproved(userDetails.getUsername(), authority);

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

            HashMap<String, String> dashboardData = reqsHandle.getDashboardInfo(teamName);

            dashboardData.put("status",statusAuth);
            dashboardData.put("username",userDetails.getUsername());
            dashboardData.put("teamname",teamName);
            dashboardData.put("userrole",authority.substring(5));
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

    public HashMap<String, String> getDashboardStats(){
        log.info("getDashboardInfo");
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();
        if(userDetails!=null) {
            String teamName = reqsHandle.getUsersInfo(userDetails.getUsername()).getTeam();
            return reqsHandle.getDashboardStats(teamName);
        }

        return new HashMap<>();
    }

    public String getExecAuth() {

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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

    public TeamOverview getActivityLogForTeamOverview(String forTeam) {
        TeamOverview teamOverview = new TeamOverview();

        teamOverview.setActivityLogOverview(getActivityLogOverview(forTeam));
        teamOverview.setTopicsPerTeamPerEnvOverview(getTopicsPerTeamEnvOverview());
        return  teamOverview;
    }

    public ChartOverview getTopicsPerTeamEnvOverview(){
        String teamName = null;
        List<HashMap<String, String>> teamCountList = null;
        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(userDetails!=null) {
            teamName = manageDatabase.getHandleDbRequests()
                    .getUsersInfo(userDetails.getUsername()).getTeam();
            teamCountList = manageDatabase.getHandleDbRequests()
                    .selectAllTopicsForTeamGroupByEnv(teamName);
        }

        ChartOverview topicsPerEnvOverview = new ChartOverview();
        DataContent dataContent = new DataContent();
        List<Col> cols = new ArrayList<>();
        Col col1 = new Col();
        col1.setId("env");
        col1.setLabel("Cluster");
        col1.setType("string");
//        col1.setP(new P());

        cols.add(col1);

        Col col2 = new Col();
        col2.setId("topics");
        col2.setLabel("topics");
        col2.setType("number");
//        col2.setP(new P());
        cols.add(col2);

        List<Row> rows = new ArrayList<>();
        List<RowContent> rowContentList;

        if(teamCountList != null)
            for (HashMap<String, String> hashMap : teamCountList) {
                Row row = new Row();
                RowContent rowContent1 = new RowContent();
                rowContent1.setV(hashMap.get("cluster"));

                RowContent rowContent2 = new RowContent();
                if(hashMap.containsKey("topicscount")) {
                    rowContent2.setV(Integer.parseInt(hashMap.get("topicscount")));
                    rowContent2.setF(Integer.parseInt(hashMap.get("topicscount")) +" Topics");
                }
                rowContentList = new ArrayList<>();
                rowContentList.add(rowContent1);
                rowContentList.add(rowContent2);
                row.setC(rowContentList);
                rows.add(row);
            }

        HAxis hAxis = new HAxis();
        hAxis.setTitle("Clusters");

        VAxis vAxis = new VAxis();
        vAxis.setTitle("Topics");
        Gridlines gridlines = new Gridlines();
        gridlines.setCount(6);
        vAxis.setGridlines(gridlines);

        Options options = new Options();
        options.setTitle("Total Topics per cluster (" + teamName + ")");
        options.setIsStacked("true");
        options.setFill(10);
        options.setDisplayExactValues(true);
        options.setHAxis(hAxis);
        options.setVAxis(vAxis);
        String[] colors = {"#2BB58A","#D8D72E","orange","red","brown", "black", "green"};
        options.setColors(colors);

        dataContent.setCols(cols);
        dataContent.setRows(rows);
        topicsPerEnvOverview.setData(dataContent);
        topicsPerEnvOverview.setType("ColumnChart"); // PieChart
        topicsPerEnvOverview.setDisplayed(true);
        topicsPerEnvOverview.setOptions(options);

        return topicsPerEnvOverview;
    }

    public ChartOverview getActivityLogOverview(String forTeam){
        int numberOfDays = 30;
        List<HashMap<String, String>> activityCountList = null;
        String teamName;
        String title  = "Total requests per day";
        if(forTeam != null && forTeam.equals("true")) {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(userDetails!=null) {
                teamName = manageDatabase.getHandleDbRequests()
                        .getUsersInfo(userDetails.getUsername()).getTeam();
                activityCountList = manageDatabase.getHandleDbRequests()
                        .selectActivityLogByTeam(teamName, numberOfDays);
                title = title + " (" + teamName + ")";
            }
        }

        ChartOverview activityLogOverview = new ChartOverview();
        DataContent dataContent = new DataContent();

        List<Row> rows = new ArrayList<>();
        List<RowContent> rowContentList;
        int totalReqs = 0, tmpActivityCount;

        if(activityCountList != null)
            for (HashMap<String, String> hashMap : activityCountList) {
                Row row = new Row();
                RowContent rowContent1 = new RowContent();
                rowContent1.setV(hashMap.get("dateofactivity"));

                RowContent rowContent2 = new RowContent();
                if(hashMap.containsKey("activitycount")) {
                    tmpActivityCount = Integer.parseInt(hashMap.get("activitycount"));
                    totalReqs += tmpActivityCount;
                    rowContent2.setV(tmpActivityCount);
                    rowContent2.setF(tmpActivityCount +" requests");
                }
                rowContentList = new ArrayList<>();
                rowContentList.add(rowContent1);
                rowContentList.add(rowContent2);
                row.setC(rowContentList);
                rows.add(row);
            }

        List<Col> cols = new ArrayList<>();
        Col col1 = new Col();
        col1.setId("date");
        col1.setLabel("Date");
        col1.setType("string");
//        col1.setP(new P());

        cols.add(col1);

        Col col2 = new Col();
        col2.setId("requests");
        col2.setLabel("requests");
        col2.setType("number");
//        col2.setP(new P());
        cols.add(col2);

        HAxis hAxis = new HAxis();
        hAxis.setTitle("Date"+ " [Last " + numberOfDays +  " days ] : " + totalReqs + " requests");

        VAxis vAxis = new VAxis();
        vAxis.setTitle("Requests");
        Gridlines gridlines = new Gridlines();
        gridlines.setCount(6);
        vAxis.setGridlines(gridlines);

        Options options = new Options();
        options.setTitle(title);
        options.setIsStacked("true");
        options.setFill(10);
        options.setDisplayExactValues(true);
        options.setHAxis(hAxis);
        options.setVAxis(vAxis);
        String[] colors = {"#2BB58A","#D8D72E","orange","red","brown", "black", "green"};
        options.setColors(colors);

        dataContent.setCols(cols);
        dataContent.setRows(rows);
        activityLogOverview.setData(dataContent);
        activityLogOverview.setType("LineChart");
        activityLogOverview.setDisplayed(true);
        activityLogOverview.setOptions(options);

        return activityLogOverview;
    }
}
