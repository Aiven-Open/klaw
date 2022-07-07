package com.kafkamgt.uiapi.controller;


import com.kafkamgt.uiapi.service.UiControllerLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class TemplateMapController {

    @Value("${kafkawize.login.authentication.type}")
    private String authenticationType;

    @Value("${kafkawize.installation.type:onpremise}")
    private String kwInstallationType;

    @Value("${kafkawize.enable.sso:false}")
    private String ssoEnabled;

    @Autowired
    UiControllerLoginService uiControllerLoginService;

    private String checkAuth(String uri, HttpServletRequest request, HttpServletResponse response){
        return uiControllerLoginService.checkAuth(uri, request, response);
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("index.html", request, response);
    }

    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String index(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("index.html", request, response);
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("dashboard", request, response);
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        if(ssoEnabled.equals("true")) {
            return "oauthLogin";
        }
        if(authenticationType.equals("db") && kwInstallationType.equals("saas"))
            return checkAuth("loginSaas.html", request, response);
        return "login.html";
    }

    @RequestMapping(value = "/forgotPassword", method = RequestMethod.GET)
    public String forgotPassword(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("forgotPassword.html", request, response);
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String register(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        if(authenticationType.equals("ldap") || authenticationType.equals("ad") || ssoEnabled.equals("true"))
        {
            if(kwInstallationType.equals("saas"))
                return checkAuth("registerSaas.html", request, response);
            else
                return checkAuth("registerLdap.html", request, response);
        }
        else if(authenticationType.equals("db") && kwInstallationType.equals("saas"))
            return checkAuth("registerSaas.html", request, response);
        else return checkAuth("register.html", request, response);
    }

    @RequestMapping(value = "/registrationReview", method = RequestMethod.GET)
    public String registrationReview(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        if(authenticationType.equals("db") && kwInstallationType.equals("saas"))
            return checkAuth("registrationReviewSaas.html", request, response);
        return checkAuth("registrationReview.html", request, response);
    }

    @RequestMapping(value = "/feedback", method = RequestMethod.GET)
    public String feedback(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("feedback.html", request, response);
    }

    @RequestMapping(value = "/newADUser", method = RequestMethod.GET)
    public String newADUser(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return "newADUser.html";
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    public String home(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("home.html", request, response);
    }

    @RequestMapping(value = "/terms", method = RequestMethod.GET)
    public String terms(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("terms.html", request, response);
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.GET)
    public String addUsers(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        if(authenticationType.equals("ldap") || authenticationType.equals("ad"))
            return checkAuth("addUserLdap.html", request, response);
        else return checkAuth("addUser.html", request, response);
    }

    @RequestMapping(value = "/envs", method = RequestMethod.GET)
    public String envs(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("envs.html", request, response);
    }

    @RequestMapping(value = "/userActivation", method = RequestMethod.GET)
    public String userActivation(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("userActivation.html", request, response);
    }

    @RequestMapping(value = "/helpwizard", method = RequestMethod.GET)
    public String helpWizard(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("helpwizard.html", request, response);
    }

    @RequestMapping(value = "/clusters", method = RequestMethod.GET)
    public String clusters(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("clusters.html", request, response);
    }

    @RequestMapping(value = "/monitorEnvs", method = RequestMethod.GET)
    public String monitorEnvs(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("monitorEnvs.html", request, response);
    }

    @RequestMapping(value = "/execAcls", method = RequestMethod.GET)
    public String execAcls(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("execAcls.html", request, response);
    }

    @RequestMapping(value = "/execSchemas", method = RequestMethod.GET)
    public String execSchemas(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("execSchemas.html", request, response);
    }

    @RequestMapping(value = "/execUsers", method = RequestMethod.GET)
    public String execUsers(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("execRegisteredUsers.html", request, response);
    }

    @RequestMapping(value = "/execTopics", method = RequestMethod.GET)
    public String execTopics(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("execTopics.html", request, response);
    }

    @RequestMapping(value = "/execConnectors", method = RequestMethod.GET)
    public String execConnectors(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("execConnectors.html", request, response);
    }

    @RequestMapping(value = "/myTopicRequests", method = RequestMethod.GET)
    public String myTopicRequests(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("myTopicRequests.html", request, response);
    }

    @RequestMapping(value = "/myConnectorRequests", method = RequestMethod.GET)
    public String myConnectorRequests(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("myConnectorRequests.html", request, response);
    }

    @RequestMapping(value = "/mySchemaRequests", method = RequestMethod.GET)
    public String mySchemaRequests(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("mySchemaRequests.html", request, response);
    }

    @RequestMapping(value = "/myAclRequests", method = RequestMethod.GET)
    public String myAclRequests(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("myAclRequests.html", request, response);
    }

    @RequestMapping(value = "/requestAcls", method = RequestMethod.GET)
    public String requestAcls(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("requestAcls.html", request, response);
    }

    @RequestMapping(value = "/requestSchema", method = RequestMethod.GET)
    public String requestSchemaUpload(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("requestSchema.html", request, response);
    }

    @RequestMapping(value = "/requestTopics", method = RequestMethod.GET)
    public String requestTopics(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("requestTopics.html", request, response);
    }

    @RequestMapping(value = "/requestConnector", method = RequestMethod.GET)
    public String requestConnector(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("requestConnector.html", request, response);
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String showUsers(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("showUsers.html", request, response);
    }

    @RequestMapping(value = "/myProfile", method = RequestMethod.GET)
    public String myProfile(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("myProfile.html", request, response);
    }

    @RequestMapping(value = "/tenantInfo", method = RequestMethod.GET)
    public String tenantInfo(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("tenantInfo.html", request, response);
    }

    @RequestMapping(value = "/changePwd", method = RequestMethod.GET)
    public String changePwd(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        if(authenticationType.equals("ldap") || authenticationType.equals("ad"))
            return checkAuth("index", request, response);
        else
            return checkAuth("changePwd.html", request, response);
    }

    @RequestMapping(value = "/synchronizeTopics", method = RequestMethod.GET)
    public String synchronizeTopics(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("synchronizeTopics.html", request, response);
    }

    @RequestMapping(value = "/syncConnectors", method = RequestMethod.GET)
    public String syncConnectors(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("synchronizeConnectors.html", request, response);
    }

    @RequestMapping(value = "/synchronizeAcls", method = RequestMethod.GET)
    public String synchronizeAcls(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("synchronizeAcls.html", request, response);
    }

    @RequestMapping(value = "/teams", method = RequestMethod.GET)
    public String showTeams(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("showTeams.html", request, response);
    }

    @RequestMapping(value = "/addTeam", method = RequestMethod.GET)
    public String addTeam(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("addTeam.html", request, response);
    }

    @RequestMapping(value = "/addEnv", method = RequestMethod.GET)
    public String addEnv(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("addEnv.html", request, response);
    }

    @RequestMapping(value = "/addKafkaConnectEnv", method = RequestMethod.GET)
    public String addKafkaConnectEnv(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("addKafkaConnectEnv.html", request, response);
    }

    @RequestMapping(value = "/modifyEnv", method = RequestMethod.GET)
    public String modifyEnv(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("modifyEnv.html", request, response);
    }

    @RequestMapping(value = "/modifyCluster", method = RequestMethod.GET)
    public String modifyCluster(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("modifyCluster.html", request, response);
    }

    @RequestMapping(value = "/modifyUser", method = RequestMethod.GET)
    public String modifyUser(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("modifyUser.html", request, response);
    }

    @RequestMapping(value = "/modifyTeam", method = RequestMethod.GET)
    public String modifyTeam(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("modifyTeam.html", request, response);
    }

    @RequestMapping(value = "/addSchemaEnv", method = RequestMethod.GET)
    public String addSchemaEnv(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("addSchemaEnv.html", request, response);
    }

    @RequestMapping(value = "/addCluster", method = RequestMethod.GET)
    public String addCluster(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("addCluster.html", request, response);
    }

    @RequestMapping(value = "/activityLog", method = RequestMethod.GET)
    public String activityLog(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("activityLog.html", request, response);
    }

    @RequestMapping(value = "/browseTopics", method = RequestMethod.GET)
    public String browseTopics(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("browseTopics.html", request, response);
    }

    @RequestMapping(value = "/kafkaConnectors", method = RequestMethod.GET)
    public String manageKafkaConnect(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("kafkaConnectors.html", request, response);
    }

    @RequestMapping(value = "/syncBackTopics", method = RequestMethod.GET)
    public String syncBackTopics(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("syncBackTopics.html", request, response);
    }

    @RequestMapping(value = "/syncBackAcls", method = RequestMethod.GET)
    public String syncBackAcls(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("syncBackAcls.html", request, response);
    }

    @RequestMapping(value = "/topicOverview", method = RequestMethod.GET)
    public String browseAcls(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("browseAcls.html", request, response);
    }

    @RequestMapping(value = "/connectorOverview", method = RequestMethod.GET)
    public String connectorOverview(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("connectorOverview.html", request, response);
    }

    @RequestMapping(value = "/serverConfig", method = RequestMethod.GET)
    public String serverConfig(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("serverConfig.html", request, response);
    }

    @RequestMapping(value = "/analytics", method = RequestMethod.GET)
    public String analytics(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("analytics.html", request, response);
    }

    @RequestMapping(value = "/metrics", method = RequestMethod.GET)
    public String metrics(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("kwmetrics.html", request, response);
    }

    @RequestMapping(value = "/permissions", method = RequestMethod.GET)
    public String permissions(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("permissions.html", request, response);
    }

    @RequestMapping(value = "/roles", method = RequestMethod.GET)
    public String roles(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("roles.html", request, response);
    }

    @RequestMapping(value = "/addRole", method = RequestMethod.GET)
    public String addRole(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("addRole.html", request, response);
    }

    @RequestMapping(value = "/tenants", method = RequestMethod.GET)
    public String tenants(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
//        if(authenticationType.equals("db"))
//            return checkAuth("index", request, response);
        return checkAuth("tenants.html", request, response);
    }

    @RequestMapping(value = "/addTenant", method = RequestMethod.GET)
    public String addTenant(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("addTenant.html", request, response);
    }

    @RequestMapping(value = "/docs", method = RequestMethod.GET)
    public String docs(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("docs.html", request, response);
    }

    @RequestMapping(value = "/notFound", method = RequestMethod.GET)
    public String notFound(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
        return checkAuth("index.html", request, response);
    }

}
