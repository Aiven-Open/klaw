package com.kafkamgt.uiapi.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UiControllerLogin {

    private static Logger LOG = LoggerFactory.getLogger(UiControllerLogin.class);

    private static final String defaultPage = "login.html";

    private String checkAuth(String uri){
        try {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails != null) {
                LOG.info("Authenticated..." + userDetails.getUsername());
                return uri;
            }
            return defaultPage;
        }catch (Exception e){
            return defaultPage;
        }
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String root(ModelMap model) {
        return checkAuth("index");
    }

    @RequestMapping(value = "/dashboard", method = RequestMethod.GET)
    public String dashboard(ModelMap model) {
        return checkAuth("dashboard");
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(ModelMap model) {
        return checkAuth("index");
    }

    @RequestMapping(value = "/addUser", method = RequestMethod.GET)
    public String addUsers(ModelMap model) {
        return checkAuth("addUser.html");
    }

    @RequestMapping(value = "/envs", method = RequestMethod.GET)
    public String envs(ModelMap model) {
        return checkAuth("envs.html");
    }

    @RequestMapping(value = "/execAcls", method = RequestMethod.GET)
    public String execAcls(ModelMap model) {
        return checkAuth("execAcls.html");
    }

    @RequestMapping(value = "/execSchemas", method = RequestMethod.GET)
    public String execSchemas(ModelMap model) {
        return checkAuth("execSchemas.html");
    }

    @RequestMapping(value = "/execTopics", method = RequestMethod.GET)
    public String execTopics(ModelMap model) {
        return checkAuth("execTopics.html");
    }

    @RequestMapping(value = "/myTopicRequests", method = RequestMethod.GET)
    public String myTopicRequests(ModelMap model) {
        return checkAuth("myTopicRequests.html");
    }

    @RequestMapping(value = "/myAclRequests", method = RequestMethod.GET)
    public String myAclRequests(ModelMap model) {
        return checkAuth("myAclRequests.html");
    }

    @RequestMapping(value = "/requestAcls", method = RequestMethod.GET)
    public String requestAcls(ModelMap model) {
        return checkAuth("requestAcls.html");
    }

    @RequestMapping(value = "/requestSchema", method = RequestMethod.GET)
    public String requestSchemaUpload(ModelMap model) {
        return checkAuth("requestSchema.html");
    }

    @RequestMapping(value = "/requestTopics", method = RequestMethod.GET)
    public String requestTopics(ModelMap model) {
        return checkAuth("requestTopics.html");
    }

    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String showUsers(ModelMap model) {
        return checkAuth("showUsers.html");
    }

    @RequestMapping(value = "/myProfile", method = RequestMethod.GET)
    public String myProfile(ModelMap model) {
        return checkAuth("myProfile.html");
    }

    @RequestMapping(value = "/changePwd", method = RequestMethod.GET)
    public String changePwd(ModelMap model) {
        return checkAuth("changePwd.html");
    }

    @RequestMapping(value = "/synchronizeTopics", method = RequestMethod.GET)
    public String synchronizeTopics(ModelMap model) {
        return checkAuth("synchronizeTopics.html");
    }

    @RequestMapping(value = "/synchronizeAcls", method = RequestMethod.GET)
    public String synchronizeAcls(ModelMap model) {
        return checkAuth("synchronizeAcls.html");
    }

    @RequestMapping(value = "/teams", method = RequestMethod.GET)
    public String showTeams(ModelMap model) {
        return checkAuth("showTeams.html");
    }

    @RequestMapping(value = "/addTeam", method = RequestMethod.GET)
    public String addTeam(ModelMap model) {
        return checkAuth("addTeam.html");
    }

    @RequestMapping(value = "/addEnv", method = RequestMethod.GET)
    public String addEnv(ModelMap model) {
        return checkAuth("addEnv.html");
    }

    @RequestMapping(value = "/activityLog", method = RequestMethod.GET)
    public String activityLog(ModelMap model) {
        return checkAuth("activityLog.html");
    }

    @RequestMapping(value = "/browseTopics", method = RequestMethod.GET)
    public String browseTopics(ModelMap model) {
        return checkAuth("browseTopics.html");
    }

    @RequestMapping(value = "/topicOverview", method = RequestMethod.GET)
    public String browseAcls(ModelMap model) {
        return checkAuth("browseAcls.html");
    }

    @RequestMapping(value = "/serverConfig", method = RequestMethod.GET)
    public String serverConfig(ModelMap model) {
        return checkAuth("serverConfig.html");
    }

    @RequestMapping(value = "/notFound", method = RequestMethod.GET)
    public String notFound(ModelMap model) {
        return checkAuth("index.html");
    }
}
