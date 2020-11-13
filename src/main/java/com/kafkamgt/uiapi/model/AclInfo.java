package com.kafkamgt.uiapi.model;

import lombok.Data;

import java.util.List;

@Data
public class AclInfo {

    private String sequence;
    private String req_no;
    private String acl_ip;
    private String acl_ssl;
    private String topicname;
    private String topictype;
    private String consumergroup;
    private String environment;
    private String teamname;
    private String operation;
    private String permission;
    private String totalNoPages;
    private List<String> allPageNos;
    private List<String> possibleTeams;
    private boolean showDeleteAcl;
}
