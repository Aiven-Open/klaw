package io.aiven.klaw.service.jwt.util;

public interface JwtConstant {
  String AUTHORIZATION_HEADER_STRING = "Authorization";
  String TOKEN_BEARER_PREFIX = "Bearer ";
  long ACCESS_TOKEN_EXPIRATION = 60L; // minutes
}
