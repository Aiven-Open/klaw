package com.kafkamgt.uiapi.model.charts;

import lombok.Data;

import java.util.List;

@Data
public class Scales {
    List<YAx> yAxes;
    List<YAx> xAxes;
}
