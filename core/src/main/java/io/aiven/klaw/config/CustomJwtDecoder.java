package io.aiven.klaw.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;

/*
Below class is only applicable for keycloak provider with legacy version (10.x)
Known issue in keycloak https://issues.redhat.com/browse/KEYCLOAK-14309
If token contains duplicate keys, json parser in spring framework fails. Hence custom json parser is applied here below.
*/
@Slf4j
class CustomJwtDecoder implements JwtDecoder {

  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static class CustomJwtDecoderFactory implements JwtDecoderFactory<ClientRegistration> {
    public JwtDecoder createDecoder(ClientRegistration reg) {
      return new CustomJwtDecoder();
    }
  }

  @Override
  public Jwt decode(String token) throws JwtException {
    JWT jwt;
    try {
      jwt = JWTParser.parse(token);
      return createJwt(token, jwt);
    } catch (ParseException e) {
      log.error("Token parsing exception : ", e);
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
      log.error("Exception while creating JWT : ", ex);
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
      log.error("Payload of JWS object is not a valid JSON object");
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
      } catch (JsonProcessingException ex) {
        log.error("Exception while transforming payload to json object ", ex);
        return null;
      }
    }
  }
}
