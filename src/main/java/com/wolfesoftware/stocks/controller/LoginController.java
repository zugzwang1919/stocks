/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.controller;


import com.wolfesoftware.stocks.config.JwtTokenUtil;
import com.wolfesoftware.stocks.model.JwtResponse;
import com.wolfesoftware.stocks.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class LoginController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private LoginService loginService;

    @Value("${quick.login.username}")
    private String quickLoginUserName;

    @Value("${quick.login.password}")
    private String quickLoginPassword;


    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestParam String userName, @RequestParam String password) {
        logger.debug("Inside createAuthenticationToken() for user {}", userName);
        JwtResponse jwtResponse = loginService.authenticateUserViaUsernameAndPassword(userName, password);
        ResponseEntity<?> responseEntity = ResponseEntity.ok(jwtResponse);
        logger.debug("Leaving createAuthenticationToken() for user {}", userName);
        return responseEntity;
    }

    @RequestMapping(value = "/quickauthenticate")
    public ResponseEntity<?> createAuthenticationToken() {
        logger.info("Someone has requested to quickly login to the group/anonymous account.");
        ResponseEntity<?> response = createAuthenticationToken(quickLoginUserName, quickLoginPassword);
        logger.info("Someone has successfully quickly logged into the group/anonymous account.");
        return response;

    }

    @RequestMapping(value = "/authenticatewithgoogle")
    public ResponseEntity<?> createAuthenticationTokenFromGoogleAccessToken(String token) throws GeneralSecurityException, IOException {
        JwtResponse jwtResponse = loginService.authenticateUserViaGoogle(token);
        ResponseEntity<?> responseEntity = ResponseEntity.ok(jwtResponse);
        return responseEntity;
    }

    @RequestMapping(value = "/refreshtoken", method = RequestMethod.POST)
    public ResponseEntity<?> refreshToken(@RequestParam String refreshToken) throws GeneralSecurityException, IOException {
        JwtResponse jwtResponse = loginService.getNewToken(refreshToken);
        ResponseEntity<?> responseEntity = ResponseEntity.ok(jwtResponse);
        return responseEntity;
    }


}

