package com.kafkamgt.uiapi.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class KwRequestFilter extends OncePerRequestFilter {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean userLogged = false;
        try {
            UserDetails userDetails =
                    (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            }
        catch (Exception e){
            try {
                authenticate(request.getParameter("username"), request.getParameter("password"));
                userLogged = true;
                String getRedirectPage = validateUrl(request.getParameter("urlbar"));
                if(getRedirectPage==null)
                    response.sendRedirect("index");
                else
                    response.sendRedirect(getRedirectPage);
            } catch (Exception ignored) {}
        }

        if(!userLogged)
            chain.doFilter(request, response);
    }

    private String validateUrl(String urlFromAddressBar) {
        if(urlFromAddressBar!=null) {
            urlFromAddressBar = urlFromAddressBar.replace(contextPath + "/", "");
            if(urlFromAddressBar.equals("login?error"))
                urlFromAddressBar = "";
        }

        return urlFromAddressBar;
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(username, password));

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
