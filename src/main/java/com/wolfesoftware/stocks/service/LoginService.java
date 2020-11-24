package com.wolfesoftware.stocks.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.wolfesoftware.stocks.config.JwtTokenUtil;
import com.wolfesoftware.stocks.model.Authority;
import com.wolfesoftware.stocks.model.JwtResponse;
import com.wolfesoftware.stocks.model.User;
import com.wolfesoftware.stocks.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Optional;

@Service
public class LoginService {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    UserService userService;

    @Resource
    UserRepository userRepository;



    public JwtResponse authenticateUserViaUsernameAndPassword(String userName, String password) {
        User authenticatedUser = authenticate(userName, password);
        final String token = jwtTokenUtil.generateToken(authenticatedUser);
        // If we find a ROLE_ADMIN role in the authorities for this user, set isAdmin to TRUE
        final Boolean isAdmin = authenticatedUser.getAuthorities().stream().anyMatch(authority -> authority.getRole().equals(Authority.Role.ROLE_ADMIN));
        return new JwtResponse(userName, token, isAdmin);
    }

    public JwtResponse authenticateUserViaGoogle(String idTokenString) throws GeneralSecurityException, IOException {

        //
        // Most of the code below was taken from Google's website:
        // https://developers.google.com/identity/sign-in/web/backend-auth
        //

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList("173419237297-cpt0q9mevdfq6el6r3rge968iaft7l79.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalStateException("stocks.wolfe-software.com could not use the Google Credentials.");
        }


        GoogleIdToken.Payload payload = idToken.getPayload();

        // Print user identifier
        String googleUserId = payload.getSubject();
        System.out.println("Google User ID: " + googleUserId);

        /* Here's some stuff that we have access to from Google should we ever want to use it */
        /*
        String email = payload.getEmail();
        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
        String name = (String) payload.get("name");
        String pictureUrl = (String) payload.get("picture");
        String locale = (String) payload.get("locale");
        String familyName = (String) payload.get("family_name");
        String givenName = (String) payload.get("given_name");
        */
        Optional<User> optionalUser = userRepository.findUserByGoogleid(googleUserId);
        User ourUser =  optionalUser.isPresent() ?
                        optionalUser.get() :
                        userService.createUser(googleUserId) ;

        final String token = jwtTokenUtil.generateToken(ourUser);
        final Boolean isAdmin = ourUser.getAuthorities().stream().anyMatch(authority -> authority.getRole().equals(Authority.Role.ROLE_ADMIN));

        return new JwtResponse(ourUser.getUsername(), token, isAdmin);
    }

    private User authenticate(String username, String password) throws AccessDeniedException {
        Optional<User> u = userRepository.findUserByUserName(username);
        if (u.isEmpty() || !u.get().getPassword().equals(password))
            throw new AccessDeniedException("Nope.  Try again.");
        return u.get();
    }

}
