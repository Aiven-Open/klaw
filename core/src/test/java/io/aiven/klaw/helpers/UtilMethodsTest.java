package io.aiven.klaw.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

class UtilMethodsTest {

  @Test
  public void testGetUserNameFromOAuth2User_PreferredUserName() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("preferred_username", "testUser");
    attributes.put("email", "test@example.com");

    DefaultOAuth2User oAuth2User = mock(DefaultOAuth2User.class);
    when(oAuth2User.getAttributes()).thenReturn(attributes);

    String result = UtilMethods.getUserName(oAuth2User, "preferred_username", "email");
    assertEquals("testUser", result);
  }

  @Test
  public void testGetUserNameFromOAuth2User_Email() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("email", "test@example.com");

    DefaultOAuth2User oAuth2User = mock(DefaultOAuth2User.class);
    when(oAuth2User.getAttributes()).thenReturn(attributes);

    String result = UtilMethods.getUserName(oAuth2User, "preferred_username", "email");
    assertEquals("test@example.com", result);
  }

  @Test
  public void testGetUserNameFromStringPrincipal() {
    String principal = "testUser";

    String result = UtilMethods.getUserName(principal, "preferred_username", "email");
    assertEquals("testUser", result);
  }

  @Test
  public void testGetUserNameFromUserDetails() {
    UserDetails userDetails = mock(UserDetails.class);
    when(userDetails.getUsername()).thenReturn("testUser");

    String result = UtilMethods.getUserName(userDetails, "preferred_username", "email");
    assertEquals("testUser", result);
  }
}
