package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class AclRequestID implements Serializable {

    private Integer req_no;

    private Integer tenantId;

    public AclRequestID(){}

    public AclRequestID(Integer req_no, Integer tenantId){
        this.req_no = req_no;
        this.tenantId = tenantId;
    }
}
