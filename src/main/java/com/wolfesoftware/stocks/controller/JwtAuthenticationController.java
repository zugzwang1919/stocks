/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.wolfesoftware.stocks.service.JwtUserDetailsService;
import com.wolfesoftware.stocks.config.JwtTokenUtil;
import com.wolfesoftware.stocks.model.JwtResponse;

@RestController
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestParam String userName, @RequestParam String password) throws Exception {
        authenticate(userName, password);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        final String token = jwtTokenUtil.generateToken(userDetails);
        // If we find a ROLE_ADMIN role in the authorities for this user, set isAdmin to TRUE
        final Boolean isAdmin = userDetails.getAuthorities().stream().
                                filter(simpleGrantedAuthority -> simpleGrantedAuthority.toString().equals("ROLE_ADMIN")).count() > 0;
        return ResponseEntity.ok(new JwtResponse(token, isAdmin));
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
