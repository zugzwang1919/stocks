package com.wolfesoftware.stocks.controller;

import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.AuthorityRepository;
import com.wolfesoftware.stocks.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthorityRepository authorityRepository;

    @PostMapping(path = "")
    User create(@RequestBody User user) {
        User u = userRepository.createUser(user);
        // By default, make this user a "normal" user
        Authority a = new Authority(u, Authority.Role.ROLE_USER);
        authorityRepository.createAuthority(a);
        List<Authority> authorities = new ArrayList<>();
        authorities.add(a);
        u.setAuthorities(authorities);
        return userRepository.updateUser(u);
    }

    // NOTE: This is only accessible by an Admin based on settings in application.properties
    @DeleteMapping(path = "/{id}")
    void delete(@PathVariable("id") Long id) {
        userRepository.deleteUser(id);
    }

}
