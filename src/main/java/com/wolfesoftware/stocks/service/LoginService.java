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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${google.signin.client.id}")
    private String googleSignInClientId;

    public JwtResponse authenticateUserViaUsernameAndPassword(String userName, String password) {
        User authenticatedUser = authenticate(userName, password);
        final String token = jwtTokenUtil.generateToken(authenticatedUser);
        // If we find a ROLE_ADMIN role in the authorities for this user, set isAdmin to TRUE
        final Boolean isAdmin = authenticatedUser.getAuthorities().stream().anyMatch(authority -> authority.getRole().equals(Authority.Role.ROLE_ADMIN));
        final String refreshToken = jwtTokenUtil.generateRefreshToken(authenticatedUser.getId());
        return new JwtResponse(userName, token, isAdmin, refreshToken);
    }

    public JwtResponse authenticateUserViaGoogle(String idTokenString) throws GeneralSecurityException, IOException {

        //
        // Most of the code below was taken from Google's website:
        // https://developers.google.com/identity/sign-in/web/backend-auth
        //

        // Verify that the IdToken passed is recognized by Google as being valid for our website.
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new JacksonFactory())
                .setAudience(Collections.singletonList(googleSignInClientId))
                .build();
        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new IllegalStateException("stocks.wolfe-software.com could not use the Google Credentials.");
        }

        // Use the Subject of the Payload from the Token as a key into our "User" table
        // It should be of the format "122278940104464674469"
        GoogleIdToken.Payload payload = idToken.getPayload();
        String googleUserId = payload.getSubject();


        // If this google credentialed user, has been here before, we should be able to find him in the User table
        // If not, create a new "User" NOTE:  This user will not have a username, password, or emailaddress
        Optional<User> optionalUser = userRepository.findUserByGoogleid(googleUserId);
        User ourUser = optionalUser.orElseGet(() -> userService.createUser(googleUserId));

        // Finally, create a token and a JwtResponse that the UI can use going forward
        final String token = jwtTokenUtil.generateToken(ourUser);
        final Boolean isAdmin = ourUser.getAuthorities().stream().anyMatch(authority -> authority.getRole().equals(Authority.Role.ROLE_ADMIN));

        final String refreshToken = jwtTokenUtil.generateRefreshToken(ourUser.getId());

        return new JwtResponse(ourUser.getUsername(), token, isAdmin, refreshToken);
    }

    public JwtResponse getNewToken(String refreshTokenString) throws GeneralSecurityException, IOException {
        // Make sure that this is a valid Refresh Token
        if (!jwtTokenUtil.validateRefreshToken(refreshTokenString)) {
            throw new AccessDeniedException("Nope.  Try again.");
        }
        // Make sure that the User Id contained in the Refresh Token is valid
        Long userId = jwtTokenUtil.getUserIdFromToken(refreshTokenString);
        Optional<User> u = userRepository.findUserById(userId);
        if (u.isEmpty()) {
            throw new AccessDeniedException("Nope.  Try again.");
        }
        User ourUser = u.get();
        final Boolean isAdmin = ourUser.getAuthorities().stream().anyMatch(authority -> authority.getRole().equals(Authority.Role.ROLE_ADMIN));
        // Get a new token
        String newToken = jwtTokenUtil.generateToken(ourUser);
        // Get a new refresh token
        String refreshToken = jwtTokenUtil.generateRefreshToken(userId);
        // Build & return the response
        return new JwtResponse(ourUser.getUsername(), newToken, isAdmin, refreshToken);
    }

    private User authenticate(String username, String password) throws AccessDeniedException {
        Optional<User> u = userRepository.findUserByUserName(username);
        if (u.isEmpty() || !u.get().getPassword().equals(password))
            throw new AccessDeniedException("Nope.  Try again.");
        return u.get();
    }

}
