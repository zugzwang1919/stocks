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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;



@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;



    @Override
    @Transactional
    public org.springframework.security.core.userdetails.User loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findUserByUserName(username);
        if (user.isPresent()) {
            User u = user.get();
            List<GrantedAuthority> authorities = u.getAuthorities().stream().
                                                map(a -> new SimpleGrantedAuthority(a.getRole().toString())).
                                                collect(Collectors.toList());
            return new org.springframework.security.core.userdetails.User(u.getUsername(), u.getPassword(), authorities);
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }

}
