package io.aiven.klaw.controller;

import static io.aiven.klaw.model.AuthenticationType.ACTIVE_DIRECTORY;
import static io.aiven.klaw.model.AuthenticationType.DATABASE;
import static io.aiven.klaw.model.AuthenticationType.LDAP;

import io.aiven.klaw.service.UiControllerLoginService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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
      OAuth2AuthenticationToken authentication) {
    return uiControllerLoginService.checkAuth(uri, request, response, authentication);
  }

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String root(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("index.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/index", method = RequestMethod.GET)
  public String index(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("index.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
  public String dashboard(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("dashboard", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/login", method = RequestMethod.GET)
  public String login(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    if (ssoEnabled.equals("true")) {
      return "oauthLogin";
    }
    if (DATABASE.value.equals(authenticationType) && SAAS.equals(kwInstallationType))
      return checkAuth("loginSaas.html", request, response, oAuth2AuthenticationToken);
    return "login.html";
  }

  @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
  public String forgotPassword(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("forgotPassword.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/register", method = RequestMethod.GET)
  public String register(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    if (LDAP.value.equals(authenticationType)
        || ACTIVE_DIRECTORY.value.equals(authenticationType)
        || "true".equals(ssoEnabled)) {
      if (SAAS.equals(kwInstallationType)) {
        return checkAuth("registerSaas.html", request, response, oAuth2AuthenticationToken);
      } else {
        return checkAuth("registerLdap.html", request, response, oAuth2AuthenticationToken);
      }
    } else if (authenticationType.equals(DATABASE.value) && kwInstallationType.equals(SAAS))
      return checkAuth("registerSaas.html", request, response, oAuth2AuthenticationToken);
    else return checkAuth("register.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/registrationReview", method = RequestMethod.GET)
  public String registrationReview(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    if (DATABASE.value.equals(authenticationType) && SAAS.equals(kwInstallationType))
      return checkAuth("registrationReviewSaas.html", request, response, oAuth2AuthenticationToken);
    return checkAuth("registrationReview.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/feedback", method = RequestMethod.GET)
  public String feedback(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("feedback.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/newADUser", method = RequestMethod.GET)
  public String newADUser(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return "newADUser.html";
  }

  @RequestMapping(value = "/home", method = RequestMethod.GET)
  public String home(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("home.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/terms", method = RequestMethod.GET)
  public String terms(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("terms.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addUser", method = RequestMethod.GET)
  public String addUsers(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    if (LDAP.value.equals(authenticationType) || ACTIVE_DIRECTORY.value.equals(authenticationType))
      return checkAuth("addUserLdap.html", request, response, oAuth2AuthenticationToken);
    else return checkAuth("addUser.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/envs", method = RequestMethod.GET)
  public String envs(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("envs.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/userActivation", method = RequestMethod.GET)
  public String userActivation(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("userActivation.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/helpwizard", method = RequestMethod.GET)
  public String helpWizard(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("helpwizard.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/clusters", method = RequestMethod.GET)
  public String clusters(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("clusters.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/monitorEnvs", method = RequestMethod.GET)
  public String monitorEnvs(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("monitorEnvs.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/execAcls", method = RequestMethod.GET)
  public String execAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("execAcls.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/execSchemas", method = RequestMethod.GET)
  public String execSchemas(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("execSchemas.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/execUsers", method = RequestMethod.GET)
  public String execUsers(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("execRegisteredUsers.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/execTopics", method = RequestMethod.GET)
  public String execTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("execTopics.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/execConnectors", method = RequestMethod.GET)
  public String execConnectors(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("execConnectors.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/myTopicRequests", method = RequestMethod.GET)
  public String myTopicRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("myTopicRequests.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/myConnectorRequests", method = RequestMethod.GET)
  public String myConnectorRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("myConnectorRequests.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/mySchemaRequests", method = RequestMethod.GET)
  public String mySchemaRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("mySchemaRequests.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/myAclRequests", method = RequestMethod.GET)
  public String myAclRequests(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("myAclRequests.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/requestAcls", method = RequestMethod.GET)
  public String requestAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("requestAcls.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/requestSchema", method = RequestMethod.GET)
  public String requestSchemaUpload(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("requestSchema.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/requestTopics", method = RequestMethod.GET)
  public String requestTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("requestTopics.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/requestConnector", method = RequestMethod.GET)
  public String requestConnector(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("requestConnector.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/users", method = RequestMethod.GET)
  public String showUsers(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("showUsers.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/myProfile", method = RequestMethod.GET)
  public String myProfile(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("myProfile.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/tenantInfo", method = RequestMethod.GET)
  public String tenantInfo(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("tenantInfo.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/changePwd", method = RequestMethod.GET)
  public String changePwd(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    if (LDAP.value.equals(authenticationType) || ACTIVE_DIRECTORY.value.equals(authenticationType))
      return checkAuth("index", request, response, oAuth2AuthenticationToken);
    else return checkAuth("changePwd.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/synchronizeTopics", method = RequestMethod.GET)
  public String synchronizeTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("synchronizeTopics.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/syncConnectors", method = RequestMethod.GET)
  public String syncConnectors(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("synchronizeConnectors.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/synchronizeAcls", method = RequestMethod.GET)
  public String synchronizeAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("synchronizeAcls.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/teams", method = RequestMethod.GET)
  public String showTeams(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("showTeams.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addTeam", method = RequestMethod.GET)
  public String addTeam(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("addTeam.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addEnv", method = RequestMethod.GET)
  public String addEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("addEnv.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addKafkaConnectEnv", method = RequestMethod.GET)
  public String addKafkaConnectEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("addKafkaConnectEnv.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/modifyEnv", method = RequestMethod.GET)
  public String modifyEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("modifyEnv.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/modifyCluster", method = RequestMethod.GET)
  public String modifyCluster(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("modifyCluster.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/modifyUser", method = RequestMethod.GET)
  public String modifyUser(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("modifyUser.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/modifyTeam", method = RequestMethod.GET)
  public String modifyTeam(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("modifyTeam.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addSchemaEnv", method = RequestMethod.GET)
  public String addSchemaEnv(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("addSchemaEnv.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addCluster", method = RequestMethod.GET)
  public String addCluster(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("addCluster.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/activityLog", method = RequestMethod.GET)
  public String activityLog(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("activityLog.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/browseTopics", method = RequestMethod.GET)
  public String browseTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("browseTopics.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/kafkaConnectors", method = RequestMethod.GET)
  public String manageKafkaConnect(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("kafkaConnectors.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/syncBackTopics", method = RequestMethod.GET)
  public String syncBackTopics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("syncBackTopics.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/syncBackAcls", method = RequestMethod.GET)
  public String syncBackAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("syncBackAcls.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/topicOverview", method = RequestMethod.GET)
  public String browseAcls(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("browseAcls.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/connectorOverview", method = RequestMethod.GET)
  public String connectorOverview(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("connectorOverview.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/serverConfig", method = RequestMethod.GET)
  public String serverConfig(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("serverConfig.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/analytics", method = RequestMethod.GET)
  public String analytics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("analytics.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/metrics", method = RequestMethod.GET)
  public String metrics(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("kwmetrics.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/permissions", method = RequestMethod.GET)
  public String permissions(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("permissions.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/roles", method = RequestMethod.GET)
  public String roles(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("roles.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addRole", method = RequestMethod.GET)
  public String addRole(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("addRole.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/tenants", method = RequestMethod.GET)
  public String tenants(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("tenants.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/addTenant", method = RequestMethod.GET)
  public String addTenant(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("addTenant.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/docs", method = RequestMethod.GET)
  public String docs(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("docs.html", request, response, oAuth2AuthenticationToken);
  }

  @RequestMapping(value = "/notFound", method = RequestMethod.GET)
  public String notFound(
      ModelMap model,
      HttpServletRequest request,
      HttpServletResponse response,
      OAuth2AuthenticationToken oAuth2AuthenticationToken) {
    return checkAuth("index.html", request, response, oAuth2AuthenticationToken);
  }
}
