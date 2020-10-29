/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;



@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Resource
    private UserRepository userRepository;

    private final static Logger logger = LoggerFactory.getLogger(JwtUserDetailsService.class);


    @Override
    @Transactional

    public org.springframework.security.core.userdetails.User loadUserByUsername(String username) throws UsernameNotFoundException {
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
