/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.controller;


import com.wolfesoftware.stocks.config.JwtTokenUtil;
import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.model.JwtResponse;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Optional;

@RestController
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestParam String userName, @RequestParam String password) throws Exception {
        logger.debug("Inside createAuthenticationToken() for user {}", userName);

        User authenticatedUser = authenticate(userName, password);
        final String token = jwtTokenUtil.generateToken(authenticatedUser);
        // If we find a ROLE_ADMIN role in the authorities for this user, set isAdmin to TRUE
        final Boolean isAdmin = authenticatedUser.getAuthorities().stream().anyMatch(authority -> authority.getRole().equals(Authority.Role.ROLE_ADMIN));
        ResponseEntity<?> responseEntity =  ResponseEntity.ok(new JwtResponse(token, isAdmin));
        logger.debug("Leaving createAuthenticationToken() for user {}", userName);
        return responseEntity;
    }

    private User authenticate(String username, String password) throws Exception {

        Optional<User> u = userRepository.findUserByUserName(username);
        if (u.isEmpty() || !u.get().getPassword().equals(password))
            throw new AccessDeniedException("Nope.  Try again.");
        return u.get();
    }
}
