package io.aiven.klaw.service;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.ApiResultStatus;
import io.aiven.klaw.model.enums.PermissionType;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UiConfigControllerService {

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Autowired private MailUtils mailService;

  @Autowired private CommonUtilsService commonUtilsService;

  @Autowired ManageDatabase manageDatabase;

  public Map<String, String> getDbAuth() {
    Map<String, String> dbMap = new HashMap<>();
    if ("db".equals(authenticationType)) {
      dbMap.put("dbauth", "true");
    } else {
      dbMap.put("dbauth", "false");
    }
    return dbMap;
  }

  private String getUserName() {
    return mailService.getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal());
  }

  public List<ActivityLog> showActivityLog(String env, String pageNo, String currentPage) {
    log.debug("showActivityLog {} {}", env, pageNo);
    String userName = getUserName();
    List<ActivityLog> origActivityList;
    int tenantId = commonUtilsService.getTenantId(getUserName());

    if (commonUtilsService.isNotAuthorizedUser(getPrincipal(), PermissionType.ALL_TEAMS_REPORTS)) {
      origActivityList =
          manageDatabase
              .getHandleDbRequests()
              .selectActivityLog(userName, env, false, tenantId); // only your team reqs
    } else {
      origActivityList =
          manageDatabase
              .getHandleDbRequests()
              .selectActivityLog(userName, env, true, tenantId); // all teams reqs
    }

    return getActivityLogsPaginated(pageNo, origActivityList, currentPage, tenantId);
  }

  private List<ActivityLog> getActivityLogsPaginated(
      String pageNo, List<ActivityLog> origActivityList, String currentPage, int tenantId) {
    List<ActivityLog> newList = new ArrayList<>();

    if (origActivityList != null && origActivityList.size() > 0) {
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
          activityLog.setEnvName(
              getEnvName(activityLog.getEnv(), activityLog.getActivityName(), tenantId));
          activityLog.setDetails(activityLog.getDetails().replaceAll("null", ""));
          activityLog.setAllPageNos(numList);
          activityLog.setTotalNoPages("" + totalPages);
          activityLog.setCurrentPage(pageNo);
          activityLog.setTeam(
              manageDatabase.getTeamNameFromTeamId(tenantId, activityLog.getTeamId()));

          newList.add(activityLog);
        }
      }
    }
    newList =
        newList.stream()
            .sorted(Collections.reverseOrder(Comparator.comparing(ActivityLog::getActivityTime)))
            .collect(Collectors.toList());
    return newList;
  }

  public String getEnvName(String envId, String activityName, int tenantId) {
    Optional<Env> envFound;

    if ("SchemaRequest".equals(activityName)) {
      envFound =
          manageDatabase.getSchemaRegEnvList(tenantId).stream()
              .filter(env -> Objects.equals(env.getId(), envId))
              .findFirst();
    } else if ("ConnectorRequest".equals(activityName)) {
      envFound =
          manageDatabase.getKafkaConnectEnvList(tenantId).stream()
              .filter(env -> Objects.equals(env.getId(), envId))
              .findFirst();
    } else {
      envFound =
          manageDatabase.getKafkaEnvList(tenantId).stream()
              .filter(env -> Objects.equals(env.getId(), envId))
              .findFirst();
    }

    return envFound.map(Env::getName).orElse(null);
  }

  public ApiResponse sendMessageToAdmin(String contactFormSubject, String contactFormMessage) {
    String userName = getUserName();

    contactFormMessage = "From " + userName + ":  \n" + contactFormMessage;
    mailService.sendMailToAdmin(
        contactFormSubject,
        contactFormMessage,
        commonUtilsService.getTenantId(getUserName()),
        commonUtilsService.getLoginUrl());
    return ApiResponse.builder().result(ApiResultStatus.SUCCESS.value).build();
  }

  public List<String> getRequestTypeStatuses() {
    return manageDatabase.getRequestStatusList();
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
