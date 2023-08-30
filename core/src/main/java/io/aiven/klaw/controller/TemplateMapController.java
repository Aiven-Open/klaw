package io.aiven.klaw.controller;

import static io.aiven.klaw.model.enums.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.enums.AuthenticationType.DATABASE;
import static io.aiven.klaw.model.enums.AuthenticationType.LDAP;

import io.aiven.klaw.constants.UriConstants;
import io.aiven.klaw.service.UiControllerLoginService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@Slf4j
public class TemplateMapController {

  private static final String SAAS = "saas";

  @Value("${klaw.login.authentication.type}")
  private String authenticationType;

  @Value("${klaw.installation.type:onpremise}")
  private String kwInstallationType;

  @Value("${klaw.enable.sso:false}")
  private String ssoEnabled;

  @Autowired UiControllerLoginService uiControllerLoginService;

  private String checkAuth(
      String uri,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return uiControllerLoginService.checkAuth(uri, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String root(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.INDEX_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/index", method = RequestMethod.GET)
  public String index(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.INDEX_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
  public String dashboard(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("dashboard", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/login", method = RequestMethod.GET)
  public String login(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    if (ssoEnabled.equals("true")) {
      return UriConstants.OAUTH_LOGIN;
    }
    if (DATABASE.value.equals(authenticationType) && SAAS.equals(kwInstallationType))
      return checkAuth(
          UriConstants.LOGIN_SAAS_PAGE, request, response, abstractAuthenticationToken);
    return UriConstants.LOGIN_PAGE;
  }

  @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
  public String forgotPassword(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(
        UriConstants.FORGOT_PASSWORD_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/register", method = RequestMethod.GET)
  public String register(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    if (LDAP.value.equals(authenticationType)
        || ACTIVE_DIRECTORY.value.equals(authenticationType)
        || "true".equals(ssoEnabled)) {
      if (SAAS.equals(kwInstallationType)) {
        return checkAuth(
            UriConstants.REGISTER_SAAS_PAGE, request, response, abstractAuthenticationToken);
      } else {
        return checkAuth(
            UriConstants.REGISTER_LDAP_PAGE, request, response, abstractAuthenticationToken);
      }
    } else if (authenticationType.equals(DATABASE.value) && kwInstallationType.equals(SAAS))
      return checkAuth(
          UriConstants.REGISTER_SAAS_PAGE, request, response, abstractAuthenticationToken);
    else
      return checkAuth(UriConstants.REGISTER_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/registrationReview", method = RequestMethod.GET)
  public String registrationReview(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    if (DATABASE.value.equals(authenticationType) && SAAS.equals(kwInstallationType))
      return checkAuth(
          "registrationReviewSaas.html", request, response, abstractAuthenticationToken);
    return checkAuth(
        UriConstants.REGISTRATION_REVIEW_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/feedback", method = RequestMethod.GET)
  public String feedback(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.FEEDBACK_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/newADUser", method = RequestMethod.GET)
  public String newADUser(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return UriConstants.NEW_AD_USER_PAGE;
  }

  @RequestMapping(value = "/home", method = RequestMethod.GET)
  public String home(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.HOME_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/terms", method = RequestMethod.GET)
  public String terms(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.TERMS_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addUser", method = RequestMethod.GET)
  public String addUsers(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    if (LDAP.value.equals(authenticationType) || ACTIVE_DIRECTORY.value.equals(authenticationType))
      return checkAuth("addUserLdap.html", request, response, abstractAuthenticationToken);
    else return checkAuth("addUser.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/envs", method = RequestMethod.GET)
  public String envs(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("envs.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/userActivation", method = RequestMethod.GET)
  public String userActivation(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.USER_ACTIVATION, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/helpwizard", method = RequestMethod.GET)
  public String helpWizard(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("helpwizard.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/clusters", method = RequestMethod.GET)
  public String clusters(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("clusters.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/monitorEnvs", method = RequestMethod.GET)
  public String monitorEnvs(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("monitorEnvs.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/execAcls", method = RequestMethod.GET)
  public String execAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("execAcls.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/execSchemas", method = RequestMethod.GET)
  public String execSchemas(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("execSchemas.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/execUsers", method = RequestMethod.GET)
  public String execUsers(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("execRegisteredUsers.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/execTopics", method = RequestMethod.GET)
  public String execTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("execTopics.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/execConnectors", method = RequestMethod.GET)
  public String execConnectors(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("execConnectors.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/myTopicRequests", method = RequestMethod.GET)
  public String myTopicRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("myTopicRequests.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/myConnectorRequests", method = RequestMethod.GET)
  public String myConnectorRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("myConnectorRequests.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/mySchemaRequests", method = RequestMethod.GET)
  public String mySchemaRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("mySchemaRequests.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/myAclRequests", method = RequestMethod.GET)
  public String myAclRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("myAclRequests.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/requestAcls", method = RequestMethod.GET)
  public String requestAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("requestAcls.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/requestSchema", method = RequestMethod.GET)
  public String requestSchemaUpload(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("requestSchema.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/requestTopics", method = RequestMethod.GET)
  public String requestTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("requestTopics.html", request, response, abstractAuthenticationToken);
  }

//  @RequestMapping(value = "/requestConsumerOffsetReset", method = RequestMethod.GET)
  public String requestConsumerOffsetReset(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(
        "requestConsumerOffsetReset.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/editTopicRequest", method = RequestMethod.GET)
  public String editTopicRequest(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("editTopicRequest.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/requestConnector", method = RequestMethod.GET)
  public String requestConnector(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("requestConnector.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public String showUsers(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("showUsers.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/myProfile", method = RequestMethod.GET)
  public String myProfile(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("myProfile.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/tenantInfo", method = RequestMethod.GET)
  public String tenantInfo(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.TENANT_INFO_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/changePwd", method = RequestMethod.GET)
  public String changePwd(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    if (LDAP.value.equals(authenticationType) || ACTIVE_DIRECTORY.value.equals(authenticationType))
      return checkAuth(UriConstants.INDEX, request, response, abstractAuthenticationToken);
    else return checkAuth("changePwd.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/synchronizeTopics", method = RequestMethod.GET)
  public String synchronizeTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("synchronizeTopics.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/synchronizeSchemas", method = RequestMethod.GET)
  public String synchronizeSchemas(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("synchronizeSchemas.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/syncConnectors", method = RequestMethod.GET)
  public String syncConnectors(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("synchronizeConnectors.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/manageConnectors", method = RequestMethod.GET)
  public String manageConnectors(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("manageConnectors.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/synchronizeAcls", method = RequestMethod.GET)
  public String synchronizeAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("synchronizeAcls.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/teams", method = RequestMethod.GET)
  public String showTeams(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("showTeams.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addTeam", method = RequestMethod.GET)
  public String addTeam(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("addTeam.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addEnv", method = RequestMethod.GET)
  public String addEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("addEnv.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addKafkaConnectEnv", method = RequestMethod.GET)
  public String addKafkaConnectEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("addKafkaConnectEnv.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/modifyEnv", method = RequestMethod.GET)
  public String modifyEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("modifyEnv.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/modifyCluster", method = RequestMethod.GET)
  public String modifyCluster(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("modifyCluster.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/modifyUser", method = RequestMethod.GET)
  public String modifyUser(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("modifyUser.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/modifyTeam", method = RequestMethod.GET)
  public String modifyTeam(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("modifyTeam.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addSchemaEnv", method = RequestMethod.GET)
  public String addSchemaEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("addSchemaEnv.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addCluster", method = RequestMethod.GET)
  public String addCluster(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("addCluster.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/activityLog", method = RequestMethod.GET)
  public String activityLog(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("activityLog.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/browseTopics", method = RequestMethod.GET)
  public String browseTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("browseTopics.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/kafkaConnectors", method = RequestMethod.GET)
  public String manageKafkaConnect(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("kafkaConnectors.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/syncBackTopics", method = RequestMethod.GET)
  public String syncBackTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("syncBackTopics.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/syncBackSchemas", method = RequestMethod.GET)
  public String syncBackSchemas(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("syncBackSchemas.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/syncBackAcls", method = RequestMethod.GET)
  public String syncBackAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("syncBackAcls.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/topicOverview", method = RequestMethod.GET)
  public String browseAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("browseAcls.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/connectorOverview", method = RequestMethod.GET)
  public String connectorOverview(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("connectorOverview.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/serverConfig", method = RequestMethod.GET)
  public String serverConfig(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("serverConfig.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/analytics", method = RequestMethod.GET)
  public String analytics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("analytics.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/metrics", method = RequestMethod.GET)
  public String metrics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("kwmetrics.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/permissions", method = RequestMethod.GET)
  public String permissions(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("permissions.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/roles", method = RequestMethod.GET)
  public String roles(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("roles.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addRole", method = RequestMethod.GET)
  public String addRole(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("addRole.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/tenants", method = RequestMethod.GET)
  public String tenants(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.TENANTS_PAGE, request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/addTenant", method = RequestMethod.GET)
  public String addTenant(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("addTenant.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/docs", method = RequestMethod.GET)
  public String docs(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth("docs.html", request, response, abstractAuthenticationToken);
  }

  @RequestMapping(value = "/notFound", method = RequestMethod.GET)
  public String notFound(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      AbstractAuthenticationToken abstractAuthenticationToken) {
    return checkAuth(UriConstants.INDEX_PAGE, request, response, abstractAuthenticationToken);
  }
}
