package io.aiven.klaw.config;

import io.aiven.klaw.auth.KwAuthenticationFailureHandler;
import io.aiven.klaw.auth.KwAuthenticationSuccessHandler;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

public class ConfigUtils {

  protected static String[] getStaticResources(boolean coralEnabled) {

    List<String> staticResources =
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
                "/reset/token",
                "/reset/password",
                "/getRoles",
                "/getTenantsInfo",
                "/getBasicInfo",
                "/getAllTeamsSUFromRegisterUsers",
                "/registerUser",
                "/resetMemoryCache/**",
                "/getActivationInfo**",
                "/v3/**",
                "/cache/**"));

    if (coralEnabled) {
      staticResources.add("/assets/coral/**");
    }

    // convert List<String> to String[] for requestMatchers
    return staticResources.toArray(new String[0]);
  }

  protected static void applyHttpSecurityConfig(
      HttpSecurity http,
      boolean coralEnabled,
      KwAuthenticationSuccessHandler successHandler,
      KwAuthenticationFailureHandler failureHandler)
      throws Exception {

    http.csrf(
            csrf -> {
              csrf.ignoringRequestMatchers("/logout");
              csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
              csrf.csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler());
            })
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(getStaticResources(coralEnabled))
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(
            oauth ->
                oauth
                    .successHandler(successHandler)
                    .failureHandler(failureHandler)
                    .loginPage("/login")
                    .permitAll())
        .logout(
            logout ->
                logout
                    .invalidateHttpSession(true)
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login")
                    .addLogoutHandler(
                        new HeaderWriterLogoutHandler(
                            new ClearSiteDataHeaderWriter(
                                ClearSiteDataHeaderWriter.Directive.CACHE,
                                ClearSiteDataHeaderWriter.Directive.COOKIES,
                                ClearSiteDataHeaderWriter.Directive.STORAGE))));
  }
}
