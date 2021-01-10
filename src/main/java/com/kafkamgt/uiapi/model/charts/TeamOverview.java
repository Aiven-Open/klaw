package com.kafkamgt.uiapi.model.charts;

import lombok.Data;

@Data
public class TeamOverview {
    ChartOverview topicsPerTeamPerEnvOverview;
    ChartOverview activityLogOverview;
}
