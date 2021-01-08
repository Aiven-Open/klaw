package com.kafkamgt.uiapi.model.charts;

import lombok.Data;

@Data
public class Options{
    public String title;
    public String isStacked;
    public int fill;
    public boolean displayExactValues;
    public VAxis vAxis;
    public HAxis hAxis;
    public String[] colors;
}
