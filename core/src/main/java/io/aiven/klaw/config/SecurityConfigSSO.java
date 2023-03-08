package io.aiven.klaw.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import io.aiven.klaw.auth.KwAuthenticationFailureHandler;
import io.aiven.klaw.auth.KwAuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static io.aiven.klaw.config.SecurityConfigSSO.OBJECT_MAPPER;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "klaw.enable.sso", havingValue = "true")
@EnableWebSecurity
public class SecurityConfigSSO {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Value("${klaw.coral.enabled:false}")
  private boolean coralEnabled;

  @Autowired KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler;

  @Autowired KwAuthenticationFailureHandler kwAuthenticationFailureHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.info("SSO enabled");
    ConfigUtils.applyHttpSecurityConfig(
        http, coralEnabled, kwAuthenticationSuccessHandler, kwAuthenticationFailureHandler);
    return http.build();
  }

  @Bean
  public JwtDecoderFactory<ClientRegistration> customJwtDecoderFactory() {
    return new CustomJwtDecoderFactory();
  }

  static class CustomJwtDecoderFactory implements JwtDecoderFactory<ClientRegistration> {
    public JwtDecoder createDecoder(ClientRegistration reg) {
      return new CustomJwtDecoder();
    }
  }

  @Bean
  WebClient webClient(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientRepository authorizedClientRepository) {
    ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
        new ServletOAuth2AuthorizedClientExchangeFilterFunction(
            clientRegistrationRepository, authorizedClientRepository);
    oauth2.setDefaultOAuth2AuthorizedClient(true);

    return WebClient.builder().apply(oauth2.oauth2Configuration()).build();
  }

  @Bean
  public AuthorizationRequestRepository<OAuth2AuthorizationRequest>
      authorizationRequestRepository() {
    return new HttpSessionOAuth2AuthorizationRequestRepository();
  }

  @Bean
  public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
      accessTokenResponseClient() {
    DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
        new DefaultAuthorizationCodeTokenResponseClient();
    return accessTokenResponseClient;
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
    final Properties globalUsers = new Properties();
    return new InMemoryUserDetailsManager(globalUsers);
  }
}

@Slf4j
class CustomJwtDecoder implements JwtDecoder {
  @Override
  public Jwt decode(String token) throws JwtException {
    JWT jwt;
    try {
      log.info("Custom decoder, token : {}", token);
      jwt = JWTParser.parse(token);
      return createJwt(token, jwt);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return null;
  }

  private Jwt createJwt(String token, JWT parsedJwt) {
    try {
      Map<String, Object> headers = new LinkedHashMap<>(parsedJwt.getHeader().toJSONObject());
      Map<String, Object> claimsMap = new HashMap<>();
      Map<String, Object> claims;
      if (parsedJwt instanceof SignedJWT) {
        claims = getJWTClaimsSet(parsedJwt).getClaims();
      } else claims = parsedJwt.getJWTClaimsSet().getClaims();

      for (String key : claims.keySet()) {
        Object value = claims.get(key);
        if (key.equals("exp") || key.equals("iat")) {
          value = ((Date) value).toInstant();
        }
        claimsMap.put(key, value);
      }
      return Jwt.withTokenValue(token)
          .headers(h -> h.putAll(headers))
          .claims(c -> c.putAll(claimsMap))
          .build();
    } catch (Exception ex) {
      if (ex.getCause() instanceof ParseException) {
        throw new JwtException("There is a problem parsing the JWT: " + ex.getMessage());
      } else {
        throw new JwtException("There is a problem decoding the JWT: " + ex.getMessage());
      }
    }
  }

  public JWTClaimsSet getJWTClaimsSet(JWT parsedJwt) throws ParseException {
    Payload payload = new Payload(parsedJwt.getParsedParts()[1]);
    log.info("Payload before : {}", payload);
    Map<String, Object> json = toJSONObject(payload);
    log.info("Payload after : {}", json);
    if (json == null) {
      throw new ParseException("Payload of JWS object is not a valid JSON object", 0);
    } else {
      return JWTClaimsSet.parse(json);
    }
  }

  public Map<String, Object> toJSONObject(Payload payload) {
    String payloadStr = payload.toString();
    if (payloadStr == null) {
      return null;
    } else {
      try {
        return OBJECT_MAPPER.readValue(payloadStr, new TypeReference<>() {});
      } catch (JsonProcessingException var3) {
        return null;
      }
    }
  }
}
