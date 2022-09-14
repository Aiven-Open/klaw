package com.kafkamgt.uiapi.model.charts;

import lombok.Data;

// https://www.chartjs.org/docs/latest/configuration/title.html

@Data
public class Title {
  boolean display;
  String text;
  String position;
  String fontColor;
  String fontFamily;
  String fontStyle;
}
