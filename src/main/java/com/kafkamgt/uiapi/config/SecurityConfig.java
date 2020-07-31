package com.kafkamgt.uiapi.config;


import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.service.UtilService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.*;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private ManageDatabase manageTopics;

    @Autowired
    private UtilService utils;

    @Value("${custom.org.name}")
    private String orgName;

    @Autowired
    private Environment environment;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String[] staticResources = {
                "/pages-login.html", "/images/**", "/static/**",
                "/kafkawize/**", "/*.js", "/**",
                "/public/**",
                "/.svg", "/.ico", "/.eot", "/.woff2",
                "/.ttf", "/.woff", "/.html", "/.js"
        };

        http
            .authorizeRequests()
            .antMatchers(staticResources).permitAll()
            .anyRequest()
            .fullyAuthenticated().and().formLogin()
            .loginPage("/login")
            .permitAll().and()
            .csrf().disable().authorizeRequests().anyRequest().permitAll()
            .and()
            .logout().logoutSuccessUrl("/login");
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();
        List<UserInfo> users = new ArrayList<>();
        if(orgName.equals("Your company name."))
        {
            LOG.error("Invalid organization configured !!");
            System.exit(0);
            throw new Exception("Invalid organization configured !!");
        }
//        if(! (environment.getActiveProfiles().length >0
//                && environment.getActiveProfiles()[0].equals("integrationtest"))) {
//            HashMap<String, String> licenseMap = utils.validateLicense();
//            if (!licenseMap.get("LICENSE_STATUS").equals(Boolean.TRUE.toString())) {
//                LOG.error(invalidKeyMessage);
//                System.exit(0);
//                throw new Exception(invalidKeyMessage);
//            }
//        }
        try {
            users = manageTopics.selectAllUsersInfo();
        }catch(Exception e){
            throw new Exception("Please check if tables are created.");
        }

        if(users.size()==0)
            throw new Exception("Please check if insert scripts are executed.");

        Iterator<UserInfo> iter = users.iterator();
        UserDetailsManagerConfigurer<AuthenticationManagerBuilder, InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder>>.UserDetailsBuilder userDetailsBuilder = null;

        UserInfo userInfo = iter.next();
        userDetailsBuilder = auth.inMemoryAuthentication()
                .passwordEncoder(encoder)
                .withUser(userInfo.getUsername()).password(encoder.encode(userInfo.getPwd())).roles(userInfo.getRole());

       while(iter.hasNext()){
            userInfo = iter.next();
            userDetailsBuilder
                    .and()
                    .withUser(userInfo.getUsername()).password(encoder.encode(userInfo.getPwd())).roles(userInfo.getRole());
        }

        auth.userDetailsService(inMemoryUserDetailsManager());
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
            globalUsers.put(userInfo.getUsername(),encoder.encode(userInfo.getPwd())+","+
            userInfo.getRole()+",enabled");
        }

        return new InMemoryUserDetailsManager(globalUsers);
    }
}