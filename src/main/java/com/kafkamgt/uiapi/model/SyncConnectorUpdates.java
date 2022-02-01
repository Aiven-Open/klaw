package com.kafkamgt.uiapi.model;

import lombok.Data;

@Data
public class SyncConnectorUpdates {
    private String sequence;
    private String req_no;
    private String connectorName;
    private String teamSelected;
    private String envSelected;
}
