package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class KafkaConnectorRequestID implements Serializable {

    private Integer connectorId;

    private Integer tenantId;

    public KafkaConnectorRequestID(){}

    public KafkaConnectorRequestID(Integer connectorId, Integer tenantId){
        this.connectorId = connectorId;
        this.tenantId = tenantId;
    }
}
