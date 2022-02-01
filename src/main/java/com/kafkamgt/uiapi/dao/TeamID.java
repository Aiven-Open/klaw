package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class TeamID implements Serializable {

    private Integer teamId;

    private Integer tenantId;

    public TeamID(){}

    public TeamID(Integer teamId, Integer tenantId){
        this.teamId = teamId;
        this.tenantId = tenantId;
    }

}
