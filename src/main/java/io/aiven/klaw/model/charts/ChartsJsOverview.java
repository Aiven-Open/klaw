package io.aiven.klaw.model.charts;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChartsJsOverview implements Serializable {
  List<Integer> data;
  List<String> labels;
  List<String> colors;
  Options options;
  String xAxisLabel;
  String yAxisLabel;
  List<String> series;
  String titleForReport;
}
