package io.aiven.klaw.helpers;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

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
    return getUserName(
        SecurityContextHolder.getContext().getAuthentication().getPrincipal(), preferredUsername);
  }
}
