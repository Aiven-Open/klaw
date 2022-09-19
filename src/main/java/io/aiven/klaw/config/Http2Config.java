package io.aiven.klaw.config;

import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class Http2Config implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

  @Override
  public void customize(TomcatServletWebServerFactory factory) {
    // customize the factory here
    factory.addConnectorCustomizers(
        (connector) -> {
          connector.addUpgradeProtocol(new Http2Protocol());
          ProtocolHandler handler = connector.getProtocolHandler();
          if (handler instanceof AbstractHttp11Protocol) {
            AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) handler;
            protocol.setCompression("on");
            protocol.setCompressionMinSize(1024);
            String mimeTypes = "text/html,text/css,application/javascript";
            String mimeTypesWithJson = mimeTypes + "," + MediaType.APPLICATION_JSON_VALUE;
            protocol.setCompressibleMimeType(mimeTypesWithJson);
          }
        });
  }
}
