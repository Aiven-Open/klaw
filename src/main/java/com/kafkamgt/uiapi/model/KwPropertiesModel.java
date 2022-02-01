package com.kafkamgt.uiapi.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class KwPropertiesModel implements Serializable {

    private String kwKey;

    private String kwValue;

    private String kwDesc;

    @Override
    public String toString() {
        return "KwPropertiesModel{" +
                "kwKey='" + kwKey + '\'' +
                ", kwValue='" + kwValue + '\'' +
                ", kwDesc='" + kwDesc + '\'' +
                '}';
    }
}
