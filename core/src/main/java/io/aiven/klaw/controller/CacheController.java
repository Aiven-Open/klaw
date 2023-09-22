package io.aiven.klaw.controller;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.service.HARestMessagingService;
import io.aiven.klaw.service.JwtTokenUtilService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@ConditionalOnProperty(prefix = "klaw.core.ha", name = "enable", matchIfMissing = false)
@RequestMapping("/cache")
@Slf4j
public class CacheController {

  public static final String CACHE_ADMIN = "CACHE_ADMIN";
  @Autowired private ManageDatabase manageDatabase;

  @Autowired JwtTokenUtilService jwtTokenUtilService;

  @PostMapping(
      value = "/tenant/{tenantId}/entityType/environment/id/{id}",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> addEnvToCache(
      @PathVariable("tenantId") Integer tenantId,
      @PathVariable("id") Integer id,
      @Valid @RequestBody Env env,
      @RequestHeader(name = "Authorization") String token)
      throws KlawNotAuthorizedException {
    jwtTokenUtilService.validateRole(token, HARestMessagingService.CACHE_ADMIN);
    manageDatabase.addEnvToCache(tenantId, env, true);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @DeleteMapping(
      value = "/tenant/{tenantId}/entityType/environment/id/{id}",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> removeEnvFromCache(
      @PathVariable("tenantId") Integer tenantId,
      @PathVariable("id") Integer id,
      @RequestHeader(name = "Authorization") String token)
      throws KlawNotAuthorizedException {
    jwtTokenUtilService.validateRole(token, HARestMessagingService.CACHE_ADMIN);
    manageDatabase.removeEnvFromCache(tenantId, id, true);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
