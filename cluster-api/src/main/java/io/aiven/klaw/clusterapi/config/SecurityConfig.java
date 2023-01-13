package io.aiven.klaw.clusterapi.config;

import io.aiven.klaw.clusterapi.services.JwtTokenUtilService;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Slf4j
@Configuration
public class SecurityConfig {
  @Value("${klaw.clusterapi.access.username:kwuser}")
  private String clusterApiUser;

  @Lazy @Autowired private UserDetailsService userDetailsService;

  private final JwtTokenUtilService jwtTokenUtilService;

  public SecurityConfig(JwtTokenUtilService jwtTokenUtilService) {
    this.jwtTokenUtilService = jwtTokenUtilService;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf().disable();
    http.formLogin().disable();
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.authorizeHttpRequests().anyRequest().fullyAuthenticated();
    http.addFilterBefore(
        new JwtRequestFilter(userDetailsService, jwtTokenUtilService),
        UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  @Primary
  public UserDetailsService getUserDetailsService() throws Exception {
    final Properties globalUsers = new Properties();
    log.info("Loading user !!");
    try {
      globalUsers.put(clusterApiUser, ",ADMIN,enabled");
    } catch (Exception e) {
      log.error("Error : User not loaded {}.", clusterApiUser, e);
      throw new Exception("Error : Cluster Api User not loaded. Exiting.");
    }

    return new InMemoryUserDetailsManager(globalUsers);
  }
}
