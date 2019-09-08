package com.kafkamgt.uiapi.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UtilService {
    public boolean checkAuthorizedSU(){
        UserDetails userDetails = getUserDetails();

        GrantedAuthority ga = userDetails.getAuthorities().iterator().next();
        String authority = ga.getAuthority();
        if(!authority.equals("ROLE_SUPERUSER"))
            return false;
        else
            return true;
    }

    public String getUserName(){
        UserDetails userDetails = (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }

    public UserDetails getUserDetails(){
        return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
