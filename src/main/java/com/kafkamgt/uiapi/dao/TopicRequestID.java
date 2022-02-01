package com.kafkamgt.uiapi.dao;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class TopicRequestID implements Serializable {

    private Integer topicid;

    private Integer tenantId;

    public TopicRequestID(){}

    public TopicRequestID(Integer topicid, Integer tenantId){
        this.topicid = topicid;
        this.tenantId = tenantId;
    }
}
