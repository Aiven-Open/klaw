package com.kafkamgt.uiapi.error;

public class KafkawizeException extends Exception {
  public KafkawizeException(String error) {
    super(error);
  }
}
