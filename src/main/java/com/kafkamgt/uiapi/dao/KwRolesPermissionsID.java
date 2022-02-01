package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class KwRolesPermissionsID implements Serializable {

    private Integer id;

    private Integer tenantId;

    public KwRolesPermissionsID(){}

    public KwRolesPermissionsID(Integer id, Integer tenantId){
        this.id = id;
        this.tenantId = tenantId;
    }
}
