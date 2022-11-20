package io.aiven.klaw.config;

import com.azure.spring.cloud.autoconfigure.aad.AadWebSecurityConfigurerAdapter;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/*
User Authentication based on Azure Active Directory.
Configure klaw.login.authentication.type to "azuread"
Configure spring.cloud.azure.active-directory.* properties
*/
@ConditionalOnExpression(
    "'${klaw.enable.sso}'.equals('false') && '${klaw.login.authentication.type}'.equals('azuread')")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfigAad extends AadWebSecurityConfigurerAdapter {
  @Value("${klaw.coral.enabled:false}")
  private boolean coralEnabled;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    log.info("Azure AD Configured");
    super.configure(http);
    ConfigUtils.applyHttpSecurityConfig(http, coralEnabled);
  }

  @Bean
  public InMemoryUserDetailsManager inMemoryUserDetailsManager() throws Exception {
    final Properties globalUsers = new Properties();
    return new InMemoryUserDetailsManager(globalUsers);
  }
}
