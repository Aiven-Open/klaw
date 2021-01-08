package com.kafkamgt.uiapi.config;


import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.filters.KwRequestFilter;
import com.kafkamgt.uiapi.service.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.*;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private ManageDatabase manageTopics;

    @Autowired
    private UtilService utils;

    @Value("${kafkawize.org.name}")
    private String orgName;

    @Value("${kafkawize.invalidkey.msg}")
    private String invalidKeyMessage;

    @Autowired
    private Environment environment;

    @Autowired
    private KwRequestFilter kwRequestFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String[] staticResources = {
                "/assets/**","/js/**","/home","/home/**","/register"
        };

        http
                .csrf().disable()
            .authorizeRequests()
            .antMatchers(staticResources).permitAll()
            .anyRequest()
            .fullyAuthenticated().and().formLogin()
            .loginPage("/login")
            .permitAll().and()
            .authorizeRequests().anyRequest().permitAll()
            .and()
                .authorizeRequests().antMatchers(staticResources).permitAll()
                .and()
            .logout().logoutSuccessUrl("/login");

        //         Add a filter to validate the username/pwd with every request
        http.addFilterBefore(kwRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        List<UserInfo> users ;
        if(orgName.equals("Your company name."))
        {
            LOG.error("Invalid organization configured !!");
            System.exit(0);
            throw new Exception("Invalid organization configured !!");
        }
        if(! (environment.getActiveProfiles().length >0
                && environment.getActiveProfiles()[0].equals("integrationtest"))) {
            HashMap<String, String> licenseMap = utils.validateLicense();
            if (!licenseMap.get("LICENSE_STATUS").equals(Boolean.TRUE.toString())) {
                LOG.error(invalidKeyMessage);
                System.exit(0);
                throw new Exception(invalidKeyMessage);
            }
        }
        try {
            users = manageTopics.selectAllUsersInfo();
        }catch(Exception e){
            throw new Exception("Please check if tables are created.");
        }

        if(users.size()==0)
            throw new Exception("Please check if insert scripts are executed.");

        Iterator<UserInfo> iter = users.iterator();
        UserDetailsManagerConfigurer<AuthenticationManagerBuilder, InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder>>.UserDetailsBuilder userDetailsBuilder ;

        UserInfo userInfo = iter.next();
        userDetailsBuilder = auth.inMemoryAuthentication()
                .passwordEncoder(encoder)
                .withUser(userInfo.getUsername())
                .password(encoder.encode(base64DecodePwd((userInfo.getPwd()))))
                .roles(userInfo.getRole());

        while(iter.hasNext()){
             userInfo = iter.next();
             userDetailsBuilder
                     .and()
                     .withUser(userInfo.getUsername())
                     .password(encoder.encode(base64DecodePwd(userInfo.getPwd())))
                     .roles(userInfo.getRole());
         }

        auth.userDetailsService(inMemoryUserDetailsManager());
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        final Properties globalUsers = new Properties();
        List<UserInfo> users = manageTopics.selectAllUsersInfo();
        Iterator<UserInfo> iter = users.iterator();
        UserInfo userInfo;
        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        while(iter.hasNext()){
            userInfo = iter.next();
            globalUsers.put(userInfo.getUsername(),
                    encoder.encode(base64DecodePwd(userInfo.getPwd()))+","+
            userInfo.getRole()+",enabled");
        }

        return new InMemoryUserDetailsManager(globalUsers);
    }

    private String base64DecodePwd(String pwd){
        return new String(Base64.getDecoder().decode(pwd));
    }
}
