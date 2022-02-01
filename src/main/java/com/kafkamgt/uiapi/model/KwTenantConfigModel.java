package com.kafkamgt.uiapi.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@ToString
public class KwTenantConfigModel implements Serializable {

    private String tenantName;

    private String baseSyncEnvironment;

    private List<String> orderOfTopicPromotionEnvsList;

    private List<String> requestTopicsEnvironmentsList;

    private String baseSyncKafkaConnectCluster;

    private List<String> orderOfConnectorsPromotionEnvsList;

    private List<String> requestConnectorsEnvironmentsList;
}
