package io.aiven.klaw.service;

import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.ActivityLog;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.helpers.Pager;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.enums.PermissionType;
import io.aiven.klaw.model.response.DbAuthInfo;
import java.util.*;
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

  public DbAuthInfo getDbAuth() {
    DbAuthInfo dbAuthInfo = new DbAuthInfo();
    if (DATABASE.value.equals(authenticationType)) {
      dbAuthInfo.setDbauth("true");
    } else {
      dbAuthInfo.setDbauth("false");
    }
    return dbAuthInfo;
  }

  private String getUserName() {
    return mailService.getUserName(getPrincipal());
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
              .getActivityLog(userName, env, false, tenantId); // only your team reqs
    } else {
      origActivityList =
          manageDatabase
              .getHandleDbRequests()
              .getActivityLog(userName, env, true, tenantId); // all teams reqs
    }

    return Pager.getItemsList(
        pageNo,
        currentPage,
        origActivityList,
        (pageContext, activityLog) -> {
          activityLog.setEnvName(
              getEnvName(activityLog.getEnv(), activityLog.getActivityName(), tenantId));
          activityLog.setDetails(activityLog.getDetails().replaceAll("null", ""));
          activityLog.setAllPageNos(pageContext.getAllPageNos());
          activityLog.setTotalNoPages(pageContext.getTotalPages());
          activityLog.setCurrentPage(pageContext.getPageNo());
          activityLog.setTeam(
              manageDatabase.getTeamNameFromTeamId(tenantId, activityLog.getTeamId()));
          return activityLog;
        });
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
    return ApiResponse.SUCCESS;
  }

  public List<String> getRequestTypeStatuses() {
    return manageDatabase.getRequestStatusList();
  }

  private Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }
}
