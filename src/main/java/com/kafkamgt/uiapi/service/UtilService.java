package com.kafkamgt.uiapi.service;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UtilService {

    @Autowired
    ManageDatabase manageDatabase;

    RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    public BoundStatement getBoundStatement(Session session, String query){
        return new BoundStatement(session.prepare(query));
    }

    public Cluster getCluster(String clusterConnHost, int clusterConnPort, CodecRegistry myCodecRegistry){

        return Cluster
                .builder()
                .addContactPoint(clusterConnHost)
                .withPort(clusterConnPort)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withCodecRegistry(myCodecRegistry)
                .withoutJMXReporting()
                .withoutMetrics()
                .withSocketOptions(
                        new SocketOptions()
                                .setConnectTimeoutMillis(10000))
                .build();
    }

    String getAuthority(UserDetails userDetails){
        HandleDbRequests reqsHandle = manageDatabase.getHandleDbRequests();

        return "ROLE_" + reqsHandle.getUsersInfo(userDetails.getUsername()).getRole();
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
//        return ga.getAuthority();
    }

    boolean checkAuthorizedAdmin_SU(UserDetails userDetails){
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = getAuthority(userDetails);
        return authority.equals("ROLE_SUPERUSER") || authority.equals("ROLE_ADMIN");
    }

    boolean checkAuthorizedSU(UserDetails userDetails){
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = getAuthority(userDetails);
        return authority.equals("ROLE_SUPERUSER");
    }

    boolean checkAuthorizedAdmin(UserDetails userDetails){
//        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = getAuthority(userDetails);
        return authority.equals("ROLE_ADMIN");
    }

    public Authentication getAuthentication(){
        return SecurityContextHolder.getContext().getAuthentication();
    }


}
