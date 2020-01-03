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

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(ModelMap model) {
        try {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (userDetails != null) {
                LOG.info("Authenticated..." + userDetails.getUsername());
                return "index";
            }
        }catch (Exception e){
            //LOG.info("Error logging in "+e.getMessage());
            return "newlogin.html";
        }

        return "newlogin.html";
    }

    @RequestMapping(value = "/addUsers", method = RequestMethod.GET)
    public String addUsers(ModelMap model) {
        return "addUser.html";
    }

    @RequestMapping(value = "/envs", method = RequestMethod.GET)
    public String envs(ModelMap model) {
        return "envs.html";
    }

    @RequestMapping(value = "/execAcls", method = RequestMethod.GET)
    public String execAcls(ModelMap model) {
        return "execAcls.html";
    }

    @RequestMapping(value = "/execSchemas", method = RequestMethod.GET)
    public String execSchemas(ModelMap model) {
        return "execSchemas.html";
    }

    @RequestMapping(value = "/execTopics", method = RequestMethod.GET)
    public String execTopics(ModelMap model) {
        return "execTopics.html";
    }

    @RequestMapping(value = "/myTopicRequests", method = RequestMethod.GET)
    public String myTopicRequests(ModelMap model) {
        return "myRequests.html";
    }

    @RequestMapping(value = "/requestAcls", method = RequestMethod.GET)
    public String requestAcls(ModelMap model) {
        return "requestAcls.html";
    }

    @RequestMapping(value = "/requestSchemaUpload", method = RequestMethod.GET)
    public String requestSchemaUpload(ModelMap model) {
        return "requestSchema.html";
    }

    @RequestMapping(value = "/requestTopics", method = RequestMethod.GET)
    public String requestTopics(ModelMap model) {
        return "requestTopics.html";
    }

    @RequestMapping(value = "/showUsers", method = RequestMethod.GET)
    public String showUsers(ModelMap model) {
        return "showUsers.html";
    }

    @RequestMapping(value = "/pcTopics", method = RequestMethod.GET)
    public String pcTopics(ModelMap model) {
        return "pcTopics.html";
    }

    @RequestMapping(value = "/myProfile", method = RequestMethod.GET)
    public String myProfile(ModelMap model) {
        return "myProfile.html";
    }

    @RequestMapping(value = "/changePwd", method = RequestMethod.GET)
    public String changePwd(ModelMap model) {
        return "changePwd.html";
    }

    @RequestMapping(value = "/synchronizeTopics", method = RequestMethod.GET)
    public String synchronizeTopics(ModelMap model) {
        return "synchronizeTopics.html";
    }

    @RequestMapping(value = "/synchronizeAcls", method = RequestMethod.GET)
    public String synchronizeAcls(ModelMap model) {
        return "synchronizeAcls.html";
    }

    @RequestMapping(value = "/showTeams", method = RequestMethod.GET)
    public String showTeams(ModelMap model) {
        return "showTeams.html";
    }

    @RequestMapping(value = "/addTeam", method = RequestMethod.GET)
    public String addTeam(ModelMap model) {
        return "addTeam.html";
    }

    @RequestMapping(value = "/addEnv", method = RequestMethod.GET)
    public String addEnv(ModelMap model) {
        return "addEnv.html";
    }

    @RequestMapping(value = "/activityLog", method = RequestMethod.GET)
    public String activityLog(ModelMap model) {
        return "activityLog.html";
    }

    @RequestMapping(value = "/browseTopics", method = RequestMethod.GET)
    public String tables(ModelMap model) {
        return "browseTopics.html";
    }

    @RequestMapping(value = "/browseAcls", method = RequestMethod.GET)
    public String browseAcls(ModelMap model) {
        return "browseAcls.html";
    }

    @RequestMapping(value = "/serverConfig", method = RequestMethod.GET)
    public String serverConfig(ModelMap model) {
        return "serverConfig.html";
    }

    @RequestMapping(value = "/notFound", method = RequestMethod.GET)
    public String notFound(ModelMap model) {
        return "index.html";
    }
}
