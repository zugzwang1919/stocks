/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.config;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        Long userId = null;
        String tokenString = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            tokenString = requestTokenHeader.substring(7);
            try {
                userId = jwtTokenUtil.getUserIdFromToken(tokenString);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired");
            }
        } else if (requestTokenHeader != null) {
            logger.warn("JWT Token does not begin with Bearer String");
        }
        // Once we get the token validate it.
        if (userId != null && userId > 0 && SecurityContextHolder.getContext().getAuthentication() == null) {

            // if token is valid, reshape it into a form that Spring can understand and tell it to use it for this context
            if (jwtTokenUtil.validateToken(tokenString)) {

                // Set the username and granted authorities portion of the UserPasswordAuthenticationToken
                List<SimpleGrantedAuthority> grantedAuthorities = jwtTokenUtil.buildGrantedAuthoritiesFromToken(tokenString);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userId, null, grantedAuthorities);

                // Set the details portion of the UserPasswordAuthenticationToken
                Map<String, Long> details = new HashMap<>();
                details.put("ID", jwtTokenUtil.getUserIdFromToken(tokenString));
                usernamePasswordAuthenticationToken.setDetails(details);

                // Tell Spring to use this newly created UserPasswordAuthenticationToken for this context
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

}
