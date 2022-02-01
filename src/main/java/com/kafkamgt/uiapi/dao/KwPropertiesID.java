package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class KwPropertiesID implements Serializable {

    private String kwKey;

    private Integer tenantId;

    public KwPropertiesID(){}

    public KwPropertiesID(String kwKey, Integer tenantId){
        this.kwKey = kwKey;
        this.tenantId = tenantId;
    }
}
