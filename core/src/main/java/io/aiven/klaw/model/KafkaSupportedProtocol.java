package io.aiven.klaw.model;

public enum KafkaSupportedProtocol {
  PLAINTEXT("PLAINTEXT"),
  SSL("SSL"),
  SASL_PLAIN("SASL_PLAIN"),
  SASL_SSL_PLAIN_MECHANISM("SASL_SSL/PLAIN"),
  SASL_SSL_GSSAPI_MECHANISM("SASL_SSL/GSSAPI"),
  SASL_SSL_SCRAM_MECHANISM_256("SASL_SSL/SCRAM-SHA-256"),
  SASL_SSL_SCRAM_MECHANISM_512("SASL_SSL/SCRAM-SHA-512");

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
