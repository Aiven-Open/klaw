package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class AclID implements Serializable {

    private Integer req_no;

    private Integer tenantId;

    public AclID(){}

    public AclID(Integer req_no, Integer tenantId){
        this.req_no = req_no;
        this.tenantId = tenantId;
    }
}
