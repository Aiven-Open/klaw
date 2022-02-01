package com.kafkamgt.uiapi.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class TopicHistory  implements Serializable {
    private String environmentName;

    private String teamName;

    private String requestedBy;

    private String requestedTime;

    private String approvedBy;

    private String approvedTime;

    private String remarks;
}
