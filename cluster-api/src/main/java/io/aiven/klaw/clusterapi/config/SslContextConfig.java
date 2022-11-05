package io.aiven.klaw.clusterapi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.ResourceUtils;

@Configuration
@Slf4j
public class SslContextConfig {

    public static HttpComponentsClientHttpRequestFactory requestFactory;
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

    @PostConstruct
    public void setKwSSLContext() throws Exception {
        if (keyStore != null && !keyStore.equals("null")) {
            TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
            javax.net.ssl.SSLContext sslContext = null;
            try {
                sslContext =
                        org.apache.http.ssl.SSLContexts.custom()
                                .loadKeyMaterial(getStore(keyStorePwd, keyStore), keyStorePwd.toCharArray())
                                .loadTrustMaterial(null, acceptingTrustStrategy)
                                .build();
            } catch (NoSuchAlgorithmException
                     | KeyManagementException
                     | KeyStoreException
                     | CertificateException
                     | UnrecoverableKeyException
                     | IOException e) {
                log.error("Exception:", e);
                throw new Exception("Unable to load TLS certs");
            }
            SSLConnectionSocketFactory csf;
            if (sslContext != null) {
                csf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
                requestFactory = new HttpComponentsClientHttpRequestFactory();
                requestFactory.setHttpClient(httpClient);
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
}
