package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.dao.Topic;
import com.kafkamgt.uiapi.model.PermissionType;
import com.kafkamgt.uiapi.model.charts.ChartsJsOverview;
import com.kafkamgt.uiapi.model.charts.TeamOverview;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import static com.kafkamgt.uiapi.service.KwConstants.KW_REPORTS_TMP_LOCATION_KEY;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Slf4j
public class AnalyticsControllerService {

    //ColumnChart, PieChart, LineChart, Bar, ComboChart
    /*
    tc-chartjs
    tc-chartjs-line
    tc-chartjs-bar
    tc-chartjs-horizontalbar
    tc-chartjs-radar
    tc-chartjs-polararea
    tc-chartjs-pie
    tc-chartjs-doughnut
    tc-chartjs-bubble

    Options
    chart-data: series data
    chart-labels: series labels
    chart-options (default: {}): Chart.js options
    chart-click (optional): onclick event handler
    chart-hover (optional): onmousemove event handler
    chart-colors (default to global colors): colors for the chart
    chart-dataset-override (optional): override datasets individually
    */



    @Autowired
    ManageDatabase manageDatabase;

    @Autowired
    private CommonUtilsService commonUtilsService;

    private String getUserName(){
        return commonUtilsService.getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    private Integer getMyTeamId(String userName){
        return manageDatabase.getHandleDbRequests().getUsersInfo(userName).getTeamId();
    }

    private List<String> getEnvsFromUserId() {
        Integer userTeamId = getMyTeamId(getUserName());
        return manageDatabase.getTeamsAndAllowedEnvs(userTeamId, commonUtilsService.getTenantId(getUserName()));
    }

    public String getEnvName(String envId){
        Optional<Env> envFound = manageDatabase.getKafkaEnvList(commonUtilsService.getTenantId(getUserName()))
                .stream().filter(env->env.getId().equals(envId)).findFirst();
        return envFound.map(Env::getName).orElse(null);
    }

    // For Sync Back Acls
    public HashMap<String, String> getAclsCountPerEnv(String sourceEnvSelected) {
        int tenantId = commonUtilsService.getTenantId(getUserName());

        List<HashMap<String, String>> aclsPerEnvList = manageDatabase
                .getHandleDbRequests().selectAclsCountByEnv(null, tenantId);
        HashMap<String, String> resultMap = new HashMap<>();

        // tenant filtering
        List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);
        if(aclsPerEnvList != null) {
            try {
                aclsPerEnvList = aclsPerEnvList.stream()
                        .filter(mapObj -> allowedEnvIdList.contains(mapObj.get("cluster")) &&
                                mapObj.get("cluster").equals(sourceEnvSelected))
                        .collect(Collectors.toList());

                if(aclsPerEnvList.size() == 1) {
                    resultMap.put("status","success");
                    resultMap.put("aclsCount", aclsPerEnvList.get(0).get("aclscount"));
                }
            } catch (Exception e) {
                log.error("No environments/clusters found.", e);
            }
        }
        return resultMap;
    }

    // For Sync Back Topics
    public HashMap<String, String> getTopicsCountPerEnv(String sourceEnvSelected) {
        List<HashMap<String, String>> topicsCountList = manageDatabase
                .getHandleDbRequests().selectTopicsCountByEnv(commonUtilsService.getTenantId(getUserName()));

        HashMap<String, String> resultMap = new HashMap<>();
        // tenant filtering
        List<String> allowedEnvIdList = getEnvsFromUserId();
        try {
            if(topicsCountList != null) {
                topicsCountList = topicsCountList.stream()
                        .filter(mapObj -> allowedEnvIdList.contains(mapObj.get("cluster")) &&
                                mapObj.get("cluster").equals(sourceEnvSelected)
                                )
                        .collect(Collectors.toList());

                if(topicsCountList.size() == 1) {
                    resultMap.put("status","success");
                    resultMap.put("topicsCount", topicsCountList.get(0).get("topicscount"));
                }
            }
        } catch (Exception e) {
            log.error("No environments/clusters found.", e);
        }
        return resultMap;
    }

    public ChartsJsOverview getProducerAclsTeamsOverview(Integer teamId, Integer tenantId){
        List<HashMap<String, String>> producerAclsPerTeamList = manageDatabase.getHandleDbRequests()
                .selectAclsCountByTeams("Producer", teamId, tenantId);

        String title = "Producer Acls";
        if(teamId != null)
            title += " ("+manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
        else
            title += " (all teams)";

        return commonUtilsService.getChartsJsOverview(producerAclsPerTeamList, title, "aclscount", "teamid",
                "Teams","Producer Acls", tenantId);
    }

    public ChartsJsOverview getConsumerAclsTeamsOverview(Integer teamId, Integer tenantId){
        List<HashMap<String, String>> consumerAclsPerTeamList = manageDatabase.getHandleDbRequests()
                .selectAclsCountByTeams("Consumer", teamId, tenantId);

        String title = "Consumer Acls";
        if(teamId != null)
            title += " ("+manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
        else
            title += " (all teams)";

        return commonUtilsService.getChartsJsOverview(consumerAclsPerTeamList, title, "aclscount", "teamid",
                "Teams","Consumer Acls", tenantId);
    }

    public ChartsJsOverview getTopicsTeamsOverview(Integer teamId, Integer tenantId){

        List<HashMap<String, String>> teamCountList = manageDatabase.getHandleDbRequests().selectTopicsCountByTeams(teamId, tenantId);
        String title = "Topics in all clusters";
        if(teamId != null)
            title += " ("+manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
        else
            title += " (all teams)";

        return commonUtilsService.getChartsJsOverview(teamCountList, title, "topicscount", "teamid",
                "Teams","Topics", tenantId);
    }

    public ChartsJsOverview getTopicsEnvOverview(Integer tenantId, PermissionType permissionType){
        List<HashMap<String, String>> teamCountList = manageDatabase.getHandleDbRequests().selectTopicsCountByEnv(tenantId);

        // tenant filtering
        try {
            List<String> allowedEnvIdList = getEnvsFromUserId();
            if(teamCountList != null) {
                //if(!permissionType.equals(PermissionType.ALL_TENANTS_REPORTS))
                    teamCountList = teamCountList.stream()
                            .filter(mapObj -> allowedEnvIdList.contains(mapObj.get("cluster")))
                            .collect(Collectors.toList());
                teamCountList.forEach(hashMap -> hashMap.put("cluster", getEnvName(hashMap.get("cluster"))));
            }
        } catch (Exception e) {
            log.error("No environments/clusters found.", e);
            teamCountList = new ArrayList<>();
        }

        return commonUtilsService.getChartsJsOverview(teamCountList, "Topics per cluster", "topicscount", "cluster",
                "Clusters","Topics", tenantId);
    }

    public ChartsJsOverview getTopicsPerTeamEnvOverview(int tenantId){
        Integer userTeamId = getMyTeamId(getUserName());
        List<HashMap<String, String>> teamCountList = null;
        String userDetails = getUserName();
        if(userDetails!=null) {
            teamCountList = manageDatabase.getHandleDbRequests()
                    .selectAllTopicsForTeamGroupByEnv(userTeamId, tenantId);
        }

        String title = "Topics per cluster (" + manageDatabase.getTeamNameFromTeamId(tenantId, userTeamId) + ")";

        return commonUtilsService.getChartsJsOverview(teamCountList, title, "topicscount", "cluster",
                "Clusters","Topics", tenantId);
    }

    public ChartsJsOverview getPartitionsEnvOverview(Integer teamId, Integer tenantId){

        List<HashMap<String, String>> partitionsCountList = manageDatabase.getHandleDbRequests()
                .selectPartitionsCountByEnv(teamId, tenantId);
        String title = "Partitions per cluster";
        if(teamId!=null)
            title += " ("+manageDatabase.getTeamNameFromTeamId(tenantId, teamId)+")";

        // tenant filtering
        try {
            List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);
            if(partitionsCountList != null) {
                partitionsCountList = partitionsCountList.stream()
                        .filter(mapObj -> allowedEnvIdList.contains(mapObj.get("cluster")))
                        .collect(Collectors.toList());
                partitionsCountList.forEach(hashMap -> hashMap.put("cluster", getEnvName(hashMap.get("cluster"))));
            }
        } catch (Exception e) {
            log.error("No environments/clusters found.", e);
            partitionsCountList = new ArrayList<>();
        }

        return commonUtilsService.getChartsJsOverview(partitionsCountList, title, "partitionscount", "cluster",
                "Clusters","Partitions", tenantId);
    }

    public ChartsJsOverview getAclsEnvOverview(Integer teamId, Integer tenantId){

        List<HashMap<String, String>> aclsPerEnvList = manageDatabase.getHandleDbRequests().selectAclsCountByEnv(teamId, tenantId);
        String title = "Acls per cluster";
        if(teamId != null)
            title += " ("+manageDatabase.getTeamNameFromTeamId(tenantId, teamId)+")";

        // tenant filtering
        try {
            List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);
            if(aclsPerEnvList != null) {
                aclsPerEnvList = aclsPerEnvList.stream()
                        .filter(mapObj -> allowedEnvIdList.contains(mapObj.get("cluster")))
                        .collect(Collectors.toList());
                aclsPerEnvList.forEach(hashMap -> hashMap.put("cluster", getEnvName(hashMap.get("cluster"))));
            }
        } catch (Exception e) {
            log.error("No environments/clusters found.", e);
            aclsPerEnvList = new ArrayList<>();
        }

        return commonUtilsService.getChartsJsOverview(aclsPerEnvList, title, "aclscount", "cluster",
                "Clusters","Acls", tenantId);
    }

    private ChartsJsOverview getActivityLogOverview(Integer teamId, Integer tenantId) {
        int numberOfDays = 30;
        List<HashMap<String, String>> activityCountList;
        String title  = "Requests per day";

        if(teamId != null) {
            activityCountList = manageDatabase.getHandleDbRequests()
                    .selectActivityLogByTeam(teamId, numberOfDays, tenantId);
            title = title + " (" + manageDatabase.getTeamNameFromTeamId(tenantId, teamId) + ")";
        }
        else {
            // tenant filtering
            List<String> allowedEnvIdList = manageDatabase.getEnvsOfTenantsMap().get(tenantId);

            try {
                activityCountList = manageDatabase.getHandleDbRequests()
                        .selectActivityLogForLastDays(numberOfDays, allowedEnvIdList.toArray(new String[0]), tenantId);
            } catch (Exception e) {
                log.error("No environments/clusters found.", e);
                activityCountList = new ArrayList<>();
            }
        }

        return commonUtilsService.getChartsJsOverview(activityCountList, title, "activitycount", "dateofactivity",
                "Days","Activities", tenantId);
    }

    public List<TeamOverview> getTeamsOverview(String forTeam) {
        List<TeamOverview> listTeamOverview = new ArrayList<>();
        TeamOverview teamOverview = new TeamOverview();

        Integer userTeamId = getMyTeamId(getUserName());

        //All tenant reports
//        if(!commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TENANTS_REPORTS)) {
//            Map<Integer, String> tenantMap = manageDatabase.getTenantMap();
//            for (Integer uniqueTenantId : tenantMap.keySet()) {
//                teamOverview = new TeamOverview();
//                teamOverview.setTenantName(manageDatabase.getTenantMap().get(uniqueTenantId));
//
//                teamOverview.setTopicsPerEnvOverview(getTopicsEnvOverview(uniqueTenantId, PermissionType.ALL_TENANTS_REPORTS));
//                teamOverview.setPartitionsPerEnvOverview(getPartitionsEnvOverview(null, uniqueTenantId));
//                teamOverview.setAclsPerEnvOverview(getAclsEnvOverview(null, uniqueTenantId));
//
//                teamOverview.setTopicsPerTeamsOverview(getTopicsTeamsOverview(null, uniqueTenantId));
//
//                teamOverview.setProducerAclsPerTeamsOverview(getProducerAclsTeamsOverview(null, uniqueTenantId));
//                teamOverview.setConsumerAclsPerTeamsOverview(getConsumerAclsTeamsOverview(null, uniqueTenantId));
//
//                teamOverview.setActivityLogOverview(getActivityLogOverview(null, uniqueTenantId));
//
//                listTeamOverview.add(teamOverview);
//            }
//        }
//        else

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS)) {
            int tenantId = commonUtilsService.getTenantId(getUserName());

            teamOverview.setProducerAclsPerTeamsOverview(getProducerAclsTeamsOverview(userTeamId, tenantId));
            teamOverview.setConsumerAclsPerTeamsOverview(getConsumerAclsTeamsOverview(userTeamId, tenantId));

            teamOverview.setTopicsPerEnvOverview(getTopicsPerTeamEnvOverview(tenantId));
            teamOverview.setPartitionsPerEnvOverview(getPartitionsEnvOverview(userTeamId, tenantId));

            teamOverview.setActivityLogOverview(getActivityLogOverview(userTeamId, tenantId));
            teamOverview.setAclsPerEnvOverview(getAclsEnvOverview(userTeamId, tenantId));

            teamOverview.setTopicsPerTeamsOverview(getTopicsTeamsOverview(userTeamId, tenantId));
            listTeamOverview.add(teamOverview);
        }
        else {
            int tenantId = commonUtilsService.getTenantId(getUserName());

            teamOverview.setTopicsPerEnvOverview(getTopicsEnvOverview(tenantId, PermissionType.ALL_TEAMS_REPORTS));
            teamOverview.setPartitionsPerEnvOverview(getPartitionsEnvOverview(null, tenantId));
            teamOverview.setAclsPerEnvOverview(getAclsEnvOverview(null, tenantId));

            teamOverview.setTopicsPerTeamsOverview(getTopicsTeamsOverview(null, tenantId));

            teamOverview.setProducerAclsPerTeamsOverview(getProducerAclsTeamsOverview(null, tenantId));
            teamOverview.setConsumerAclsPerTeamsOverview(getConsumerAclsTeamsOverview(null, tenantId));

            teamOverview.setActivityLogOverview(getActivityLogOverview(null, tenantId));

            listTeamOverview.add(teamOverview);
        }

        return listTeamOverview;
    }

    public TeamOverview getActivityLogForTeamOverview(String forTeam) {
        TeamOverview teamOverview = new TeamOverview();
        Integer userTeamId = getMyTeamId(getUserName());

        teamOverview.setTopicsPerTeamPerEnvOverview(getTopicsPerTeamEnvOverview(commonUtilsService.getTenantId(getUserName())));
        if(forTeam !=null && forTeam.equals("true"))
            teamOverview.setActivityLogOverview(getActivityLogOverview(userTeamId, 101));

        return  teamOverview;
    }

    public File generateReport() {
        int tenantId = commonUtilsService.getTenantId(getUserName());
        String kwReportsLocation = manageDatabase.getKwPropertyValue(KW_REPORTS_TMP_LOCATION_KEY, tenantId);

        List<TeamOverview> totalOverviewList = getTeamsOverview(null);
        String dataPattern = "yyyy-MM-ddHH-mm-ssSSS";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dataPattern);

        File zipFile = new File(manageDatabase.getKwPropertyValue(KW_REPORTS_TMP_LOCATION_KEY, tenantId) +
                "KwReport" + simpleDateFormat.format(new Date())+ ".zip");
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        } catch (FileNotFoundException e) {
            log.error("Exception:", e);
        }

        String actualFileName;
        for (TeamOverview totalOverview : totalOverviewList) {
            if(totalOverview.getTenantName() != null)
                actualFileName = "Kafkawize-" + totalOverview.getTenantName() + ".xlsx";
            else
                actualFileName = "KafkawizeReport"  + ".xlsx";

            String fileName = kwReportsLocation + actualFileName;
            File reportFile = new File(fileName);

            XSSFWorkbook workbook = new XSSFWorkbook();

            generateReportPerView(totalOverview.getTopicsPerEnvOverview(), workbook, reportFile);
            generateReportPerView(totalOverview.getPartitionsPerEnvOverview(), workbook, reportFile);
            generateReportPerView(totalOverview.getTopicsPerTeamsOverview(), workbook, reportFile);
            generateReportPerView(totalOverview.getAclsPerEnvOverview(), workbook, reportFile);
            generateReportPerView(totalOverview.getProducerAclsPerTeamsOverview(), workbook, reportFile);
            generateReportPerView(totalOverview.getConsumerAclsPerTeamsOverview(), workbook, reportFile);
            generateReportPerView(totalOverview.getActivityLogOverview(), workbook, reportFile);

            addTopicNamesPerEnvToReport(getTopicNames(tenantId), workbook, reportFile, "Topics");
            addTopicNamesPerEnvToReport(getConsumerGroups(tenantId), workbook, reportFile, "ConsumerGroups");

            log.info("Report generated");
            if (zipOutputStream != null) {
                writeToZipFile(zipOutputStream, fileName, actualFileName);
            }
        }

        try {
            if (zipOutputStream != null) {
                zipOutputStream.close();
            }
        } catch (IOException e) {
            log.error("Exception:", e);
        }

        return zipFile;
    }

    private void writeToZipFile(ZipOutputStream zos, String fullFileName, String fileName){
        try {
            ZipEntry e = new ZipEntry(fileName);
            zos.putNextEntry(e);

            FileInputStream fis = new FileInputStream(fullFileName);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

        } catch (IOException e) {
            log.error("Exception:", e);
        }
    }

    private void addTopicNamesPerEnvToReport(HashMap<String, List<String>> topicNames, XSSFWorkbook workbook, File reportFile, String sheetName) {
        XSSFSheet sheet = workbook.createSheet(sheetName);
        Map<Integer, Object[]> data = new TreeMap<>();
        int id = 1;
        List<String> envNames = new ArrayList<>(topicNames.keySet());

        // set header row
        data.put(id, new Object[]{"S.No", envNames});
        id ++;

        int maxSize = 0;

        List<List<String>> allTopicLists = new ArrayList<>();
        for (String envName : envNames) {
            allTopicLists.add(topicNames.get(envName));
            if(topicNames.get(envName).size() > maxSize)
                maxSize = topicNames.get(envName).size();
        }

        // set content
        List<String> rowValList;
        for(int i = 0; i< maxSize; i++){
            rowValList = new ArrayList<>();
            for (List<String> allTopicList : allTopicLists) {
                if(allTopicList.size() > i)
                    rowValList.add(allTopicList.get(i));
                else
                    rowValList.add("");
            }
            data.put(id, new Object[]{id - 1, rowValList});
            id++;
        }

        addData(sheet, data);
        writeToFile(sheetName, workbook, reportFile);
    }

    private void addData(XSSFSheet sheet, Map<Integer, Object[]> data) {
        int rownum = 0;
        org.apache.poi.ss.usermodel.Row rowXl;
        Set<Integer> keyset = data.keySet();
        for (Integer key : keyset)
        {
            rowXl = sheet.createRow(rownum++);
            Object [] objArr = data.get(key);
            int cellnum = 0;
            Cell cell;
            for (Object obj : objArr)
            {
                if(obj instanceof String) {
                    cell = rowXl.createCell(cellnum++);
                    cell.setCellValue((String) obj);
                }
                else if(obj instanceof Integer) {
                    cell = rowXl.createCell(cellnum++);
                    cell.setCellValue((Integer) obj);
                }
                else if(obj instanceof List) {
                    List<String> strList = (List<String>)obj;
                    for (String s : strList) {
                        cell = rowXl.createCell(cellnum++);
                        cell.setCellValue(s);
                    }
                }
            }
        }
    }

    private void generateReportPerView(ChartsJsOverview chartsJsOverview, XSSFWorkbook workbook, File reportFile) {
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet(chartsJsOverview.getTitleForReport());
        //This data needs to be written (Object[])
        Map<Integer, Object[]> data = new HashMap<>();
        List<Integer> data1 = chartsJsOverview.getData();
        List<String> labels = chartsJsOverview.getLabels();

        // header
        data.put(1, new Object[] {"S.No", chartsJsOverview.getXAxisLabel(), chartsJsOverview.getYAxisLabel()});
        int id = 2;

        // content
        for(int i=0;i<data1.size();i++){
            if(!labels.get(i).equals("")) {
                data.put(id, new Object[]{id - 1, labels.get(i), data1.get(i)});
                id++;
            }
        }

        addData(sheet, data);

        writeToFile(chartsJsOverview.getOptions().getTitle().toString(), workbook, reportFile);
    }

    private void writeToFile(String title, XSSFWorkbook workbook, File reportFile) {
        try
        {
            //Write the workbook in file system
            FileOutputStream out = new FileOutputStream(reportFile);
            workbook.write(out);
            out.close();
            log.info("Added Sheet {}", title);
        }
        catch (Exception e)
        {
            log.error("Error : writeToFile ", e);
        }
    }

    private HashMap<String, List<String>> getTopicNames(int tenantId){
        // tenant filtering
        List<String> allowedEnvIdList = getEnvsFromUserId();

        HashMap<String, List<String>> topicsPerEnv = new HashMap<>();
        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS)){
            // normal user
            Integer userTeamId = getMyTeamId(getUserName());
            List<Topic> topics = manageDatabase.getHandleDbRequests().getTopicsforTeam(userTeamId, tenantId);

            for (String env : allowedEnvIdList) {
                topicsPerEnv.put(getEnvName(env), topics.stream()
                        .filter(topic -> topic.getEnvironment().equals(env))
                        .map(Topic::getTopicname)
                        .sorted()
                        .collect(Collectors.toList()));
            }
        }else{
            // admin
            List<Topic> topics = manageDatabase.getHandleDbRequests().getAllTopics(tenantId);
            for (String env : allowedEnvIdList) {
                topicsPerEnv.put(getEnvName(env), topics.stream()
                        .filter(topic -> topic.getEnvironment().equals(env))
                        .map(Topic::getTopicname)
                        .sorted()
                        .collect(Collectors.toList()));
            }
        }

        return topicsPerEnv;
    }

    private HashMap<String, List<String>> getConsumerGroups(int tenantId){
        // tenant filtering
        List<String> allowedEnvIdList = getEnvsFromUserId();

        HashMap<String, List<String>> aclsPerEnv = new HashMap<>();
        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS)){
            // normal user
            Integer userTeamId = getMyTeamId(getUserName());
            List<Acl> acls = manageDatabase.getHandleDbRequests().getConsumerGroupsforTeam(userTeamId, tenantId);

            for (String env : allowedEnvIdList) {
                aclsPerEnv.put(getEnvName(env), acls.stream()
                        .filter(acl -> acl.getEnvironment().equals(env) && acl.getConsumergroup() != null)
                        .map(Acl::getConsumergroup)
                        .sorted()
                        .collect(Collectors.toList()));
            }
        }else{
            // admin
            List<Acl> acls = manageDatabase.getHandleDbRequests().getAllConsumerGroups(tenantId);
            for (String env : allowedEnvIdList) {
                aclsPerEnv.put(getEnvName(env), acls.stream()
                        .filter(acl -> acl.getEnvironment().equals(env) && acl.getConsumergroup() != null)
                        .map(Acl::getConsumergroup)
                        .sorted()
                        .collect(Collectors.toList()));
            }
        }

        return aclsPerEnv;
    }

    private Object getPrincipal(){
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
