package com.wolfesoftware.stocks.service;

import com.wolfesoftware.stocks.config.JwtTokenUtil;
import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.model.JwtResponse;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

@Service
public class LoginService {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    UserRepository userRepository;



    public JwtResponse authenticateUserViaUsernnameAndPassword(String userName, String password) {
        User authenticatedUser = authenticate(userName, password);
        final String token = jwtTokenUtil.generateToken(authenticatedUser);
        // If we find a ROLE_ADMIN role in the authorities for this user, set isAdmin to TRUE
        final Boolean isAdmin = authenticatedUser.getAuthorities().stream().anyMatch(authority -> authority.getRole().equals(Authority.Role.ROLE_ADMIN));
        return new JwtResponse(userName, token, isAdmin);
    }

    private User authenticate(String username, String password) throws AccessDeniedException {
        Optional<User> u = userRepository.findUserByUserName(username);
        if (u.isEmpty() || !u.get().getPassword().equals(password))
            throw new AccessDeniedException("Nope.  Try again.");
        return u.get();
    }

}
