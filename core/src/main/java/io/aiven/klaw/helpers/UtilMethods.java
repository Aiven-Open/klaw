package io.aiven.klaw.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.aiven.klaw.model.TopicConfigurationRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@Slf4j
public class UtilMethods {
  public static String getUserName(Object principal, String preferredUsername) {
    if (principal instanceof DefaultOAuth2User) {
      DefaultOAuth2User defaultOAuth2User = (DefaultOAuth2User) principal;
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
}
