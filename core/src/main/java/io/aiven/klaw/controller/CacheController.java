package io.aiven.klaw.controller;

import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.error.KlawNotAuthorizedException;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.service.MailUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cache")
@Slf4j
public class CacheController {

  @Autowired private ManageDatabase manageDatabase;

  @PreAuthorize("hasRole('CACHE_ADMIN') or hasRole('SUPERADMIN')")
  @PostMapping(
      value = "/environment/tenant/{tenantId}/id/{id}",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> addEnvToCache(
      @PathVariable("tenantId") Integer tenantId,
      @PathVariable("id") Integer id,
      @Valid @RequestBody Env env)
  {
    manageDatabase.addEnvToCache(tenantId, env, true);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_SUPERADMIN')")
  @DeleteMapping(
      value = "/environment/tenant/{tenantId}/id/{id}",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ApiResponse> removeEnvFromCache(
      @PathVariable("tenantId") Integer tenantId, @PathVariable("id") Integer id)
       {
    manageDatabase.removeEnvFromCache(tenantId, id, true);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
