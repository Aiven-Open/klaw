package com.kafkamgt.uiapi.model.charts;

import lombok.Data;

@Data
public class TeamOverview {
    ChartsJsOverview producerAclsPerTeamsOverview;
    ChartsJsOverview consumerAclsPerTeamsOverview;
    ChartsJsOverview aclsPerEnvOverview;
    ChartsJsOverview topicsPerTeamsOverview;
    ChartsJsOverview topicsPerTeamPerEnvOverview;
    ChartsJsOverview topicsPerEnvOverview;
    ChartsJsOverview partitionsPerEnvOverview;
    ChartsJsOverview activityLogOverview;
    String tenantName;
}
