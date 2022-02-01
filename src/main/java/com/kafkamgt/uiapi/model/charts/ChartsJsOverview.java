package com.kafkamgt.uiapi.model.charts;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class ChartsJsOverview  implements Serializable {
    List<Integer> data;
    List<String> labels;
    List<String> colors;
    Options options;
    String xAxisLabel;
    String yAxisLabel;
    List<String> series;
    String titleForReport;
}
