package com.kafkamgt.uiapi.model.charts;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RowContent{
    public Object v;
    public String f;
}
