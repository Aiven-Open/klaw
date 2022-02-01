package com.kafkamgt.uiapi.service;

import com.kafkamgt.uiapi.dao.*;
import com.kafkamgt.uiapi.model.PermissionType;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.kafkamgt.uiapi.service.KwConstants.*;

@Service
public class DefaultDataService {

    public UserInfo getUser(int tenantId, String password, String role, Integer teamId, String mailId, String userName, String secretKey){
        UserInfo userInfo = new UserInfo();
        userInfo.setPwd(getJasyptEncryptor(secretKey).encrypt(password));
        userInfo.setRole(role);
        userInfo.setTeamId(teamId);
        userInfo.setMailid(mailId);
        userInfo.setUsername(userName);
        userInfo.setTenantId(tenantId);
        userInfo.setFullname("Super Admin");

        return userInfo;
    }

    private BasicTextEncryptor getJasyptEncryptor(String secretKey){
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPasswordCharArray(secretKey.toCharArray());

        return textEncryptor;
    }

    public Team getTeam(int tenantId, String teamName){
        Team team = new Team();
        team.setTenantId(tenantId);
        team.setTeamname(teamName);
        team.setContactperson("Kw Admin");
        return team;
    }

    public KwTenants getDefaultTenant(int tenantId){
        KwTenants kwTenants = new KwTenants();
        kwTenants.setTenantId(tenantId);
        kwTenants.setTenantName("default");
        kwTenants.setTenantDesc("default");
        kwTenants.setInTrial("false");
        kwTenants.setContactPerson("Kafkawize Administrator");
        kwTenants.setOrgName("Default Organization");
        kwTenants.setLicenseExpiry(new Timestamp(System.currentTimeMillis() + TimeUnit.DAYS.toMillis (DAYS_EXPIRY_DEFAULT_TENANT)));
        kwTenants.setIsActive("true");

        return kwTenants;
    }

    public List<KwProperties> createDefaultProperties(int tenantId, String mailId){
        List<KwProperties> kwPropertiesList = new ArrayList<>();

        KwProperties kwProperties1 = new KwProperties("kafkawize.mail.topicrequest.content",
                tenantId, MAIL_TOPICREQUEST_CONTENT,"Email notification body for a new Topic request");
        kwPropertiesList.add(kwProperties1);

        KwProperties kwProperties2 = new KwProperties("kafkawize.mail.topicdeleterequest.content",
                tenantId, MAIL_TOPICDELETEREQUEST_CONTENT,"Email notification body for Topic delete request");
        kwPropertiesList.add(kwProperties2);

        KwProperties kwProperties3 = new KwProperties("kafkawize.mail.topicclaimrequest.content",
                tenantId, MAIL_TOPICCLAIMREQUEST_CONTENT,"Email notification body for a new claim request");
        kwPropertiesList.add(kwProperties3);

        KwProperties kwProperties4 = new KwProperties("kafkawize.mail.topicrequestapproval.content",
                tenantId, MAIL_TOPICREQUESTAPPROVAL_CONTENT,"Email notification body for Topic request approval");
        kwPropertiesList.add(kwProperties4);

        KwProperties kwProperties5 = new KwProperties("kafkawize.mail.topicrequestdenial.content",
                tenantId, MAIL_TOPICREQUESTDENIAL_CONTENT,"Email notification body for Topic request decline");
        kwPropertiesList.add(kwProperties5);

        KwProperties kwProperties6 = new KwProperties("kafkawize.mail.aclrequest.content",
                tenantId, MAIL_ACLREQUEST_CONTENT,"Email notification body for new Acl request");
        kwPropertiesList.add(kwProperties6);

        KwProperties kwProperties7 = new KwProperties("kafkawize.mail.aclrequestdelete.content",
                tenantId, MAIL_ACLREQUESTDELETE_CONTENT,"Email notification body for Acl delete request");
        kwPropertiesList.add(kwProperties7);

        KwProperties kwProperties8 = new KwProperties("kafkawize.mail.aclrequestapproval.content",
                tenantId, MAIL_ACLREQUESTAPPROVAL_CONTENT,"Email notification body for Acl request approval");
        kwPropertiesList.add(kwProperties8);

        KwProperties kwProperties9 = new KwProperties("kafkawize.mail.aclrequestdenial.content",
                tenantId, MAIL_ACLREQUESTDENIAL_CONTENT,"Email notification body for Acl request decline");
        kwPropertiesList.add(kwProperties9);

        KwProperties kwProperties10 = new KwProperties("kafkawize.mail.registeruser.content",
                tenantId, MAIL_REGISTERUSER_CONTENT,"Email notification body for new user request to be approved.");
        kwPropertiesList.add(kwProperties10);

        KwProperties kwProperties11 = new KwProperties("kafkawize.mail.registeruser.saas.content",
                tenantId, MAIL_REGISTERUSER_SAAS_CONTENT,"Email notification body for new user request");
        kwPropertiesList.add(kwProperties11);

        KwProperties kwProperties12 = new KwProperties("kafkawize.mail.registerusertouser.content",
                tenantId, MAIL_REGISTERUSERTOUSER_CONTENT,"Email notification body for new user request to actual user.");
        kwPropertiesList.add(kwProperties12);

        KwProperties kwProperties13 = new KwProperties("kafkawize.mail.registerusertouser.saas.content",
                tenantId, MAIL_REGISTERUSERTOUSER_SAAS_CONTENT,"Email notification body for new SaaS user request to actual user.");
        kwPropertiesList.add(kwProperties13);

        KwProperties kwProperties14 = new KwProperties("kafkawize.mail.recontopics.content",
                tenantId, MAIL_RECONTOPICS_CONTENT,"Email notification body for reconciliation of topics.");
        kwPropertiesList.add(kwProperties14);

        KwProperties kwProperties15 = new KwProperties("kafkawize.mail.newuseradded.content",
                tenantId, MAIL_NEWUSERADDED_CONTENT,"Email notification body after a new user is added");
        kwPropertiesList.add(kwProperties15);

        KwProperties kwProperties16 = new KwProperties("kafkawize.mail.passwordreset.content",
                tenantId, MAIL_PASSWORDRESET_CONTENT,"Email notification body for password reset");
        kwPropertiesList.add(kwProperties16);

        KwProperties kwProperties17 = new KwProperties("kafkawize.superuser.mailid",
                tenantId, mailId,"Email id of Super user or Super Admin");
        kwPropertiesList.add(kwProperties17);

        KwProperties kwProperties18 = new KwProperties("kafkawize.reports.location",
                tenantId, REPORTS_LOCATION,"Temporary location of reports");
        kwPropertiesList.add(kwProperties18);

        KwProperties kwProperties20 = new KwProperties("kafkawize.getschemas.enable",
                tenantId, GETSCHEMAS_ENABLE,"Enable View or retrieve schemas");
        kwPropertiesList.add(kwProperties20);

        KwProperties kwProperties21 = new KwProperties("kafkawize.clusterapi.url",
                tenantId, CLUSTERAPI_URL,"Cluster Api URL");
        kwPropertiesList.add(kwProperties21);

        KwProperties kwProperties22 = new KwProperties("kafkawize.tenant.config",
                tenantId, TENANT_CONFIG,"Base sync cluster, order of topic promotion environments, topic request envs");
        kwPropertiesList.add(kwProperties22);

        KwProperties kwProperties23 = new KwProperties("kafkawize.adduser.roles",
                tenantId, ADDUSER_ROLES,"Allowed roles when adding a new user.");
        kwPropertiesList.add(kwProperties23);

        KwProperties kwProperties24 = new KwProperties("kafkawize.envs.standardnames",
                tenantId, ENVS_STANDARDNAMES,"Standard names of environments");
        kwPropertiesList.add(kwProperties24);

//        KwProperties kwProperties25 = new KwProperties("kafkawize.mail.transport.protocol",
//                tenantId, MAIL_PROTOCOL,"Smtp Config Mail transport protocol");
//        kwPropertiesList.add(kwProperties25);
//
//        KwProperties kwProperties26 = new KwProperties("kafkawize.mail.host",
//                tenantId, MAIL_HOST,"Smtp Config Mail host");
//        kwPropertiesList.add(kwProperties26);
//
//        KwProperties kwProperties27 = new KwProperties("kafkawize.mail.port",
//                tenantId, MAIL_PORT,"Smtp Config Mail port");
//        kwPropertiesList.add(kwProperties27);
//
//        KwProperties kwProperties28 = new KwProperties("kafkawize.mail.username",
//                tenantId, MAIL_USERNAME,"Smtp Config Mail username");
//        kwPropertiesList.add(kwProperties28);
//
//        KwProperties kwProperties29 = new KwProperties("kafkawize.mail.password",
//                tenantId, MAIL_PASSWORD,"Smtp Config Mail password");
//        kwPropertiesList.add(kwProperties29);
//
//        KwProperties kwProperties30 = new KwProperties("kafkawize.mail.smtp.auth",
//                tenantId, MAIL_SMTP_AUTH,"Smtp Config Mail Smtp Auth true/false");
//        kwPropertiesList.add(kwProperties30);
//
//        KwProperties kwProperties31 = new KwProperties("kafkawize.mail.smtp.starttls.enable",
//                tenantId, MAIL_SMTP_TLS,"Smtp Config Mail Smtp TLS enable true/false");
//        kwPropertiesList.add(kwProperties31);
//
//        KwProperties kwProperties32 = new KwProperties("kafkawize.mail.debug",
//                tenantId, MAIL_DEBUG,"Smtp Config Mail debug");
//        kwPropertiesList.add(kwProperties32);

        KwProperties kwProperties19 = new KwProperties("kafkawize.mail.notifications.enable",
                tenantId, MAIL_NOTIFICATIONS_ENABLE,"Smtp Config Enable email notifications");
        kwPropertiesList.add(kwProperties19);

        KwProperties kwProperties33 = new KwProperties("kafkawize.broadcast.text",
                tenantId, "","Broadcast text to all your tenant users");
        kwPropertiesList.add(kwProperties33);

        KwProperties kwProperties34 = new KwProperties("kafkawize.mail.registerusertouser.saasadmin.content",
                tenantId, MAIL_REGISTERUSERTOUSER_SAAS_ADMIN_CONTENT,"Email notification body for new SaaS user request to actual user.");
        kwPropertiesList.add(kwProperties34);


        return kwPropertiesList;
    }

    public List<KwRolesPermissions> createDefaultRolesPermissions(int tenantId, boolean isSuperAdmin, String kwInstallationType) {
        HashMap<String, String> defaultUserPermissionsList = getDefaultUserPermissionsList();
        HashMap<String, String> defaultAdminPermissionsList = getDefaultAdminPermissionsList();
        HashMap<String, String> superAdminPermissionsList = getDefaultSuperAdminPermissionsList();

        if(!kwInstallationType.equals("saas")) // on premise
        {
            defaultAdminPermissionsList.put("ADD_TENANT", PermissionType.ADD_TENANT.getDescription());
        }

        List<KwRolesPermissions> kwRolesPermissionsList = new ArrayList<>();
        for (Map.Entry<String, String> rolePermEntry : defaultUserPermissionsList.entrySet()) {
            KwRolesPermissions kwRolesPermissions = new KwRolesPermissions(0, tenantId, USER_ROLE, rolePermEntry.getKey(),rolePermEntry.getValue());
            kwRolesPermissionsList.add(kwRolesPermissions);
        }
        for (Map.Entry<String, String> rolePermEntry : defaultAdminPermissionsList.entrySet()) {
            KwRolesPermissions kwRolesPermissions = new KwRolesPermissions(0, tenantId, SUPERADMIN_ROLE, rolePermEntry.getKey(),rolePermEntry.getValue());
            kwRolesPermissionsList.add(kwRolesPermissions);
        }
        if(isSuperAdmin){
            for (Map.Entry<String, String> rolePermEntry : superAdminPermissionsList.entrySet()) {
                KwRolesPermissions kwRolesPermissions = new KwRolesPermissions(0, tenantId, SUPERADMIN_ROLE, rolePermEntry.getKey(),rolePermEntry.getValue());
                kwRolesPermissionsList.add(kwRolesPermissions);
            }
        }


        return kwRolesPermissionsList;
    }

    private static HashMap<String, String> getDefaultUserPermissionsList() {
        HashMap<String, String> defaultUserPermissionsList = new HashMap<>();

        defaultUserPermissionsList.put("REQUEST_CREATE_TOPICS", PermissionType.REQUEST_CREATE_TOPICS.getDescription());
        defaultUserPermissionsList.put("REQUEST_CREATE_SUBSCRIPTIONS", PermissionType.REQUEST_CREATE_SUBSCRIPTIONS.getDescription());
        defaultUserPermissionsList.put("REQUEST_DELETE_TOPICS", PermissionType.REQUEST_DELETE_TOPICS.getDescription());
        defaultUserPermissionsList.put("REQUEST_DELETE_SUBSCRIPTIONS", PermissionType.REQUEST_DELETE_SUBSCRIPTIONS.getDescription());
        defaultUserPermissionsList.put("REQUEST_CREATE_SCHEMAS", PermissionType.REQUEST_CREATE_SCHEMAS.getDescription());
        defaultUserPermissionsList.put("REQUEST_DELETE_SCHEMAS", PermissionType.REQUEST_DELETE_SCHEMAS.getDescription());
        defaultUserPermissionsList.put("REQUEST_CREATE_CONNECTORS", PermissionType.REQUEST_CREATE_CONNECTORS.getDescription());
        defaultUserPermissionsList.put("REQUEST_DELETE_CONNECTORS", PermissionType.REQUEST_DELETE_CONNECTORS.getDescription());
        defaultUserPermissionsList.put("APPROVE_TOPICS", PermissionType.APPROVE_TOPICS.getDescription());
        defaultUserPermissionsList.put("APPROVE_SUBSCRIPTIONS", PermissionType.APPROVE_SUBSCRIPTIONS.getDescription());
        defaultUserPermissionsList.put("APPROVE_SCHEMAS", PermissionType.APPROVE_SCHEMAS.getDescription());
        defaultUserPermissionsList.put("APPROVE_CONNECTORS", PermissionType.APPROVE_CONNECTORS.getDescription());
        defaultUserPermissionsList.put("VIEW_CONNECTORS", PermissionType.VIEW_CONNECTORS.getDescription());
        defaultUserPermissionsList.put("VIEW_TOPICS", PermissionType.VIEW_TOPICS.getDescription());

        return defaultUserPermissionsList;
    }

    private static HashMap<String, String> getDefaultAdminPermissionsList() {
        HashMap<String, String> defaultAdminPermissionsList = new HashMap<>();

        defaultAdminPermissionsList.put("ADD_EDIT_DELETE_TEAMS", PermissionType.ADD_EDIT_DELETE_TEAMS.getDescription());
        defaultAdminPermissionsList.put("ADD_EDIT_DELETE_USERS", PermissionType.ADD_EDIT_DELETE_USERS.getDescription());
        defaultAdminPermissionsList.put("ADD_EDIT_DELETE_CLUSTERS", PermissionType.ADD_EDIT_DELETE_CLUSTERS.getDescription());
        defaultAdminPermissionsList.put("ADD_EDIT_DELETE_ENVS", PermissionType.ADD_EDIT_DELETE_ENVS.getDescription());
        defaultAdminPermissionsList.put("ADD_EDIT_DELETE_ROLES", PermissionType.ADD_EDIT_DELETE_ROLES.getDescription());

        defaultAdminPermissionsList.put("SYNC_TOPICS", PermissionType.SYNC_TOPICS.getDescription());
        defaultAdminPermissionsList.put("SYNC_SUBSCRIPTIONS", PermissionType.SYNC_SUBSCRIPTIONS.getDescription());
        defaultAdminPermissionsList.put("SYNC_BACK_TOPICS", PermissionType.SYNC_BACK_TOPICS.getDescription());
        defaultAdminPermissionsList.put("SYNC_BACK_SUBSCRIPTIONS", PermissionType.SYNC_BACK_SUBSCRIPTIONS.getDescription());

        defaultAdminPermissionsList.put("UPDATE_PERMISSIONS", PermissionType.UPDATE_PERMISSIONS.getDescription());
        defaultAdminPermissionsList.put("UPDATE_SERVERCONFIG", PermissionType.UPDATE_SERVERCONFIG.getDescription());
        defaultAdminPermissionsList.put("ALL_TEAMS_REPORTS", PermissionType.ALL_TEAMS_REPORTS.getDescription());
        defaultAdminPermissionsList.put("APPROVE_ALL_REQUESTS_TEAMS", PermissionType.APPROVE_ALL_REQUESTS_TEAMS.getDescription());
        defaultAdminPermissionsList.put("VIEW_TOPICS", PermissionType.VIEW_TOPICS.getDescription());
        defaultAdminPermissionsList.put("UPDATE_DELETE_MY_TENANT", PermissionType.UPDATE_DELETE_MY_TENANT.getDescription());


        return defaultAdminPermissionsList;
    }

    public HashMap<String, String> getDefaultSuperAdminPermissionsList() {
        HashMap<String, String> permissionsList = new HashMap<>();

        permissionsList.put("SHUTDOWN_KAFKAWIZE", PermissionType.SHUTDOWN_KAFKAWIZE.getDescription());
        return permissionsList;
    }

    public ProductDetails getProductDetails(String name, String version) {
        ProductDetails productDetails = new ProductDetails();
        productDetails.setName(name);
        productDetails.setVersion(version);

        return productDetails;
    }
}
