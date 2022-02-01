package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class KwClusterID implements Serializable {

    private Integer clusterId;

    private Integer tenantId;

    public KwClusterID(){}

    public KwClusterID(Integer clusterId, Integer tenantId){
        this.clusterId = clusterId;
        this.tenantId = tenantId;
    }
}
