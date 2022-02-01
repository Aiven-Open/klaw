package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.ActivityLog;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.model.PermissionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UiConfigControllerService {

    @Value("${kafkawize.login.authentication.type}")
    private String authenticationType;

    @Autowired
    private MailUtils mailService;

    @Autowired
    private CommonUtilsService commonUtilsService;

    @Autowired
    ManageDatabase manageDatabase;

    public HashMap<String, String> getDbAuth() {
        HashMap<String, String> dbMap = new HashMap<>();
        if(authenticationType.equals("db"))
            dbMap.put("dbauth","true");
        else
            dbMap.put("dbauth","false");
        return dbMap;
    }

    private String getUserName(){
        return mailService.getUserName(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public List<ActivityLog> showActivityLog(String env, String pageNo, String currentPage){
        log.debug("showActivityLog {} {}", env, pageNo);
        String userName = getUserName();
        List<ActivityLog> origActivityList ;
        int tenantId = commonUtilsService.getTenantId(getUserName());

        if(commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS))
            origActivityList = manageDatabase.getHandleDbRequests()
                    .selectActivityLog(userName, env, false, tenantId); // only your team reqs
        else
            origActivityList = manageDatabase.getHandleDbRequests()
                .selectActivityLog(userName, env, true, tenantId); // all teams reqs

        return getActivityLogsPaginated(pageNo, origActivityList, currentPage, tenantId);
    }

    private List<ActivityLog> getActivityLogsPaginated(String pageNo, List<ActivityLog> origActivityList, String currentPage, int tenantId) {
        List<ActivityLog> newList = new ArrayList<>();

        if(origActivityList!=null && origActivityList.size() > 0) {
            int totalRecs = origActivityList.size();
            int recsPerPage = 20;
            int totalPages = totalRecs / recsPerPage + (totalRecs % recsPerPage > 0 ? 1 : 0);
            List<String> numList = new ArrayList<>();

            pageNo = commonUtilsService.deriveCurrentPage(pageNo, currentPage, totalPages);

            int requestPageNo = Integer.parseInt(pageNo);
            int startVar = (requestPageNo - 1) * recsPerPage;
            int lastVar = (requestPageNo) * (recsPerPage);

            commonUtilsService.getAllPagesList(pageNo, currentPage, totalPages, numList);

            for (int i = 0; i < totalRecs; i++) {
                ActivityLog activityLog = origActivityList.get(i);
                if (i >= startVar && i < lastVar) {
                    activityLog.setEnvName(getEnvName(activityLog.getEnv(), activityLog.getActivityName(), tenantId));
                    activityLog.setDetails(activityLog.getDetails().replaceAll("null",""));
                    activityLog.setAllPageNos(numList);
                    activityLog.setTotalNoPages("" + totalPages);
                    activityLog.setCurrentPage(pageNo);
                    activityLog.setTeam(manageDatabase.getTeamNameFromTeamId(tenantId, activityLog.getTeamId()));

                    newList.add(activityLog);
                }
            }
        }
        newList = newList.stream()
                 .sorted(Collections.reverseOrder(Comparator.comparing(ActivityLog::getActivityTime)))
                 .collect(Collectors.toList());
        return newList;
    }

    public String getEnvName(String envId, String activityName, int tenantId){
        Optional<Env> envFound ;

        if(activityName.equals("SchemaRequest"))
            envFound = manageDatabase.getSchemaRegEnvList(tenantId).stream().filter(env->env.getId().equals(envId)).findFirst();
        else if(activityName.equals("ConnectorRequest"))
            envFound = manageDatabase.getKafkaConnectEnvList(tenantId).stream().filter(env->env.getId().equals(envId)).findFirst();
        else
            envFound = manageDatabase.getKafkaEnvList(tenantId).stream().filter(env->env.getId().equals(envId)).findFirst();


        return envFound.map(Env::getName).orElse(null);
    }

    public HashMap<String, String> sendMessageToAdmin(String contactFormSubject, String contactFormMessage) {
        String userName = getUserName();

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("result", "success");

        contactFormMessage = "From " + userName + ":  \n" + contactFormMessage;
        mailService.sendMailToAdmin(contactFormSubject, contactFormMessage, commonUtilsService.getTenantId(getUserName()),
                commonUtilsService.getLoginUrl());
        return hashMap;
    }


    public List<String> getRequestTypeStatuses() {
        return manageDatabase.getRequestStatusList();
//        if(userType.equals("USER"))
//            return manageDatabase.getRequestStatusList();
//        else {
//            ArrayList<String> apprvrList = new ArrayList<>(manageDatabase.getRequestStatusList());
//            apprvrList.remove("deleted");
//            return apprvrList;
//        }
    }

    private Object getPrincipal(){
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
