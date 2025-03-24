package io.aiven.klaw.model.enums;

public enum KafkaSupportedProtocol {
  SSL("SSL"),
  SASL_PLAIN("SASL_PLAIN"),
  SASL_SSL_PLAIN_MECHANISM("SASL_SSL/PLAIN"),
  SASL_SSL_GSSAPI_MECHANISM("SASL_SSL/GSSAPI"),
  SASL_SSL_SCRAM_MECHANISM_256("SASL_SSL/SCRAM-SHA-256"),
  SASL_SSL_SCRAM_MECHANISM_512("SASL_SSL/SCRAM-SHA-512"),
  PLAINTEXT("PLAINTEXT");

  private final String value;

  public String getName() {
    return name();
  }

  public String getValue() {
    return value;
  }

  KafkaSupportedProtocol(String value) {
    this.value = value;
  }
}
