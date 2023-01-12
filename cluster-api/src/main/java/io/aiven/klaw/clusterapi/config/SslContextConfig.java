package io.aiven.klaw.clusterapi.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;

@Configuration
@Slf4j
public class SslContextConfig implements InitializingBean {

  private HttpComponentsClientHttpRequestFactory requestFactory;

  @Value("${server.ssl.trust-store:null}")
  private String trustStore;

  @Value("${server.ssl.trust-store-password:null}")
  private String trustStorePwd;

  @Value("${server.ssl.key-store:null}")
  private String keyStore;

  @Value("${server.ssl.key-store-password:null}")
  private String keyStorePwd;

  @Value("${server.ssl.key-store-type:JKS}")
  private String keyStoreType;

  public HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory() {
    return requestFactory;
  }

  private void setKwSSLContext() throws Exception {
    if (keyStore != null && !keyStore.equals("null")) {
      TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
      SSLContextBuilder builder = new SSLContextBuilder();
      try {
        builder
            .loadKeyMaterial(getStore(keyStorePwd, keyStore), keyStorePwd.toCharArray())
            .loadTrustMaterial(acceptingTrustStrategy);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        Registry<ConnectionSocketFactory> registry =
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();
        HttpClientConnectionManager poolingConnManager =
            new PoolingHttpClientConnectionManager(registry);
        CloseableHttpClient httpClient =
            HttpClients.custom().setConnectionManager(poolingConnManager).build();
        requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
      } catch (NoSuchAlgorithmException
          | KeyStoreException
          | CertificateException
          | UnrecoverableKeyException
          | IOException
          | KeyManagementException e) {
        log.error("Exception: ", e);
        throw new RuntimeException(e);
      }
    }
  }

  protected KeyStore getStore(String secret, String storeLoc)
      throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {

    File key = ResourceUtils.getFile(storeLoc);

    final KeyStore store = KeyStore.getInstance(keyStoreType);
    try (InputStream inputStream = Files.newInputStream(key.toPath())) {
      store.load(inputStream, secret.toCharArray());
    }
    return store;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    setKwSSLContext();
  }
}
