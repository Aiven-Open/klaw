package io.aiven.klaw.model.charts;

import java.util.List;
import lombok.Data;

@Data
public class Scales {
  List<YAx> yAxes;
  List<YAx> xAxes;
}
