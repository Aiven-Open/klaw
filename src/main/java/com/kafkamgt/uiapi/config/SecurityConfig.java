package com.kafkamgt.uiapi.config;


import com.kafkamgt.uiapi.dao.UserInfo;
import com.kafkamgt.uiapi.helpers.ManageTopics;
import com.kafkamgt.uiapi.helpers.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
    ManageTopics manageTopics;

    @Autowired
    Utilities utils;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String[] staticResources  =  {
                "/images/**"
        };

        http.csrf().disable().authorizeRequests().anyRequest().permitAll()
                .and()
                .authorizeRequests()
                .antMatchers(staticResources).permitAll()
                .anyRequest().fullyAuthenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
        ;
        }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        PasswordEncoder encoder =
                PasswordEncoderFactories.createDelegatingPasswordEncoder();

        if(!utils.validateLicense()) {
            LOG.error("Invalid License, exiting...");
            System.exit(0);
            throw new Exception("Invalid License !!");
        }

        List<UserInfo> users = manageTopics.selectAllUsersInfo();
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
        UserInfo userInfo = null;
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