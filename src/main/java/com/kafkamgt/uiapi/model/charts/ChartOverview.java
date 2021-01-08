package com.kafkamgt.uiapi.model.charts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChartOverview implements Serializable{
    public String type;
    public DataContent data;
    public Options options;
    public Formatters formatters;
    public boolean displayed;
}

class Formatters{
}


