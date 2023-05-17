package io.aiven.klaw.config;

import io.aiven.klaw.auth.KwAuthenticationFailureHandler;
import io.aiven.klaw.auth.KwAuthenticationSuccessHandler;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

/*
- Provide static resources to be loaded, required by the application
- Apply HttpSecurity configs
 */
public class ConfigUtils {
  protected static List<String> getStaticResources(boolean coralEnabled) {
    List<String> staticResourcesHtmlArray =
        new ArrayList<>(
            List.of(
                "/assets/**",
                "/lib/**",
                "/js/**",
                "/home",
                "/home/**",
                "/register**",
                "/authenticate",
                "/login**",
                "/terms**",
                "/registrationReview**",
                "/forgotPassword",
                "/getDbAuth",
                "/feedback**",
                "/resetPassword",
                "/reset/token",
                "/reset/password",
                "/getRoles",
                "/getTenantsInfo",
                "/getBasicInfo",
                "/getAllTeamsSUFromRegisterUsers",
                "/registerUser",
                "/resetMemoryCache/**",
                "/userActivation**",
                "/getActivationInfo**",
                "/v3/**"));

    if (coralEnabled) {
      staticResourcesHtmlArray.add("/assets/coral/**");
    }

    return staticResourcesHtmlArray;
  }

  protected static void applyHttpSecurityConfig(
      HttpSecurity http,
      boolean coralEnabled,
      KwAuthenticationSuccessHandler kwAuthenticationSuccessHandler,
      KwAuthenticationFailureHandler kwAuthenticationFailureHandler)
      throws Exception {
    http.csrf()
        .disable()
        .authorizeHttpRequests()
        .requestMatchers(getStaticResources(coralEnabled).toArray(new String[0]))
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .oauth2Login()
        .successHandler(kwAuthenticationSuccessHandler)
        .failureHandler(kwAuthenticationFailureHandler)
        .failureUrl("/login?error")
        .loginPage("/login")
        .permitAll()
        .and()
        .logout()
        .invalidateHttpSession(true)
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login")
        .addLogoutHandler(
            new HeaderWriterLogoutHandler(
                new ClearSiteDataHeaderWriter(
                    ClearSiteDataHeaderWriter.Directive.CACHE,
                    ClearSiteDataHeaderWriter.Directive.COOKIES,
                    ClearSiteDataHeaderWriter.Directive.STORAGE)));
  }
}
