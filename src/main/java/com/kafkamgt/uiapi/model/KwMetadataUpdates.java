package com.kafkamgt.uiapi.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import java.sql.Timestamp;

@ToString
@Getter
@Setter
@Builder
public class KwMetadataUpdates implements Serializable {

    private Integer tenantId;

    private String entityType;

    private String operationType;

    private Timestamp createdTime;
}
