/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.config;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {


    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String tokenString = null;
        // JWT Token is in the form "Bearer token". Remove Bearer word and get
        // only the Token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            tokenString = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(tokenString);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired");
            }
        } else if (requestTokenHeader != null) {
            logger.warn("JWT Token does not begin with Bearer String");
        }
        // Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = loadUserByUsername(username);
            // if token is valid configure Spring Stock to manually set
            // authentication
            if (jwtTokenUtil.validateToken(tokenString)) {
                List<SimpleGrantedAuthority> grantedAuthorities = jwtTokenUtil.buildGrantedAuthoritiesFromToken(tokenString);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, grantedAuthorities);
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // After setting the Authentication in the context, we specify
                // that the current user is authenticated. So it passes the
                // Spring Stock Configurations successfully.
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        chain.doFilter(request, response);
    }

    private org.springframework.security.core.userdetails.User loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Entering loadUserByUsername()");
        Optional<User> user = userRepository.findUserByUserName(username);
        logger.debug("Inside loadUserByUsername(). User has been retrieved from DB.");
        if (user.isPresent()) {
            User u = user.get();
            List<GrantedAuthority> authorities = u.getAuthorities().stream().
                    map(a -> new SimpleGrantedAuthority(a.getRole().toString())).
                    collect(Collectors.toList());
            org.springframework.security.core.userdetails.User returnedUser = new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(), authorities);
            logger.debug("Exiting loadUserByUsername() - User was found and UserDetails were created");
            return returnedUser;
        } else {
            logger.debug("Exiting loadUserByUsername() - User was not found.");
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }



}
