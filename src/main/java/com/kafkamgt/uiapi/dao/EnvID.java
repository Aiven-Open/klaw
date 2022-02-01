package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class EnvID implements Serializable {

    private String id;

    private Integer tenantId;

    public EnvID(){}

    public EnvID(String id, Integer tenantId){
        this.id = id;
        this.tenantId = tenantId;
    }
}
