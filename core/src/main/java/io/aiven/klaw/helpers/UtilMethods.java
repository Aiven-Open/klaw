package io.aiven.klaw.helpers;

import static io.aiven.klaw.helpers.KwConstants.DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.config.ManageDatabase;
import io.aiven.klaw.dao.Env;
import io.aiven.klaw.dao.UserInfo;
import io.aiven.klaw.model.ApiResponse;
import io.aiven.klaw.model.TopicConfigurationRequest;
import io.aiven.klaw.model.enums.ClusterStatus;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@Slf4j
public class UtilMethods {
  public static String getUserName(Object principal, String preferredUsername) {
    if (principal instanceof DefaultOAuth2User defaultOAuth2User) {
      return (String) defaultOAuth2User.getAttributes().get(preferredUsername);
    } else if (principal instanceof String) {
      return (String) principal;
    } else {
      return ((UserDetails) principal).getUsername();
    }
  }

  public static String getUserName(String preferredUsername) {
    return getUserName(getPrincipal(), preferredUsername);
  }

  public static Object getPrincipal() {
    return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
  }

  /**
   * @param jsonConfig a json string normally stored in JsonParams in the Topic Object
   * @param mapper an Object Mapper that is used to turn the string to an Object
   * @return A map of advanced config properties
   */
  public static Map<String, String> createAdvancedConfigFromJson(
      String jsonConfig, ObjectMapper mapper) {

    return createTopicConfigurationRequestFromJson(jsonConfig, mapper)
        .getAdvancedTopicConfiguration();
  }

  /**
   * @param jsonConfig a json string normally stored in JsonParams in the Topic Object
   * @param mapper an Object Mapper that is used to turn the string to an Object
   * @return The TopicConfigurationRequest
   */
  public static TopicConfigurationRequest createTopicConfigurationRequestFromJson(
      String jsonConfig, ObjectMapper mapper) {

    try {
      if (null != jsonConfig) {
        return mapper.readValue(jsonConfig, TopicConfigurationRequest.class);
      }
    } catch (JsonProcessingException e) {
      // ignore this error while executing the req.
      log.error("Error in parsing topic config ", e);
    }
    return new TopicConfigurationRequest();
  }

  public static void updateEnvStatus(
      ResponseEntity<ApiResponse> response,
      ManageDatabase manageDatabase,
      int tenantId,
      String environmentId) {

    if (response.getStatusCode().is2xxSuccessful()
        && (response.getBody() != null && response.getBody().isSuccess())) {
      UtilMethods.updateLatestStatus(
          ClusterStatus.ONLINE, manageDatabase, tenantId, Integer.parseInt(environmentId));
    } else {
      if (response.getStatusCode().is5xxServerError()) {
        UtilMethods.updateLatestStatus(
            ClusterStatus.NOT_KNOWN, manageDatabase, tenantId, Integer.parseInt(environmentId));
      }
    }
  }

  public static void updateLatestStatus(
      ClusterStatus clusterStatus, ManageDatabase manageDatabase, int tenantId, int envId) {

    Optional<Env> opt = manageDatabase.getEnv(tenantId, envId);

    if (opt.isPresent()) {
      Env e = opt.get();
      final LocalDateTime statusTime = LocalDateTime.now(ZoneOffset.UTC);
      e.setEnvStatus(clusterStatus);
      e.setEnvStatusTime(statusTime);
      e.setEnvStatusTimeString(DATE_TIME_DDMMMYYYY_HHMMSS_FORMATTER.format(statusTime));
      manageDatabase.addEnvToCache(tenantId, e, false);
    }
  }

  public static UserInfo getUserInfoFromAuthentication(ManageDatabase db, String username) {
    return db.getHandleDbRequests().getUsersInfo(username);
  }
}
