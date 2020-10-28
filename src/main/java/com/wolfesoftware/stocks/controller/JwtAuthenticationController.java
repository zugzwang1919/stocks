/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.controller;


import com.wolfesoftware.stocks.config.JwtTokenUtil;
import com.wolfesoftware.stocks.model.JwtResponse;
import com.wolfesoftware.stocks.service.JwtUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationController.class);

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestParam String userName, @RequestParam String password) throws Exception {
        logger.debug("Inside createAuthenticationToken() for user {}", userName);
        authenticate(userName, password);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        logger.debug("Inside createAuthenticationToken() for user {} UserDetails should have been created based on data in DB", userName);
        final String token = jwtTokenUtil.generateToken(userDetails);
        // If we find a ROLE_ADMIN role in the authorities for this user, set isAdmin to TRUE
        final Boolean isAdmin = userDetails.getAuthorities().stream().anyMatch(simpleGrantedAuthority -> simpleGrantedAuthority.toString().equals("ROLE_ADMIN"));
        ResponseEntity<?> responseEntity =  ResponseEntity.ok(new JwtResponse(token, isAdmin));
        logger.debug("Leaving createAuthenticationToken() for user {}", userName);
        return responseEntity;
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
