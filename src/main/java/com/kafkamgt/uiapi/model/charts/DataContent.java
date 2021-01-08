package com.kafkamgt.uiapi.model.charts;

import lombok.Data;

import java.util.List;

@Data
public class DataContent{
    public List<Col> cols;
    public List<Row> rows;
}
