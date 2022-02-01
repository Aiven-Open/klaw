package com.kafkamgt.uiapi.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class TopicOverview {
    List<TopicInfo> topicInfoList;
    List<AclInfo> aclInfoList;
    List<AclInfo> prefixedAclInfoList;
    List<AclInfo> transactionalAclInfoList;
    private List<TopicHistory> topicHistoryList;
    HashMap<String, String> promotionDetails;
    boolean topicExists;
    List<HashMap<String, String>> schemaDetails;
    boolean schemaExists;
    boolean prefixAclsExists;
    boolean txnAclsExists;

    String topicDocumentation;
    Integer topicIdForDocumentation;
}
