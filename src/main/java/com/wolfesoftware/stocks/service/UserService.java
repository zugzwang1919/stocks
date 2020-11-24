package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.AuthorityRepository;
import com.wolfesoftware.stocks.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService  {

    @Resource
    private AuthorityRepository authorityRepository;

    @Resource
    private UserRepository userRepository;



    @Transactional
    public User createUser(String username, String password, String emailAddress) {

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setEmailaddress(emailAddress);
        User createdUser  = userRepository.createUser(u);

        // By default, make this user a "normal" user
        addBasicUserAuthority(u);

        // return the user that now has everything it needs to be a full fledged user
        return userRepository.updateUser(createdUser);

    }

    @Transactional
    public User createUser(String googleid) {

        User u = new User();
        u.setUsername("Google_Random_" + (int)(Math.random()*100000000));
        u.setGoogleid(googleid);
        User createdUser  = userRepository.createUser(u);

        // By default, make this user a "normal" user
        addBasicUserAuthority(u);

        // return the user that now has everything it needs to be a full fledged user
        return userRepository.updateUser(createdUser);

    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteUser(id);
    }

    private void addBasicUserAuthority(User u) {
        // By default, make this user a "normal" user
        Authority a = new Authority(u, Authority.Role.ROLE_USER);
        authorityRepository.createAuthority(a);
        List<Authority> authorities = new ArrayList<>();
        authorities.add(a);
        u.setAuthorities(authorities);
    }
}
