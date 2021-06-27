/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.config;

import com.wolfesoftware.stocks.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    public static final int ACCESS_TOKEN_TIME_TO_LIVE = 5 * 60 * 1000;  // Five minutes
    // public static final int ACCESS_TOKEN_TIME_TO_LIVE = 5 * 60 * 1000;  // Debug value
    public static final int REFRESH_ACCESS_TOKEN_TIME_TO_LIVE = 2 * 60 * 60 * 1000; // Two hours
    // public static final int REFRESH_ACCESS_TOKEN_TIME_TO_LIVE = 15 * 60 * 1000; // Debug value

    @Value("${jwt.secret}")
    private String secret;

    private static final String ACCESS_TOKEN_NAME = "Access Token";
    private static final String REFRESH_TOKEN_NAME = "Refresh Token";

    private static final String USER_ID_KEY_NAME = "USER_ID";
    private static final String USER_ROLES_KEY_NAME = "USER_ROLES";


    /*****  EXAMINING a TOKEN *****/


    public Boolean validateToken(String token) {
        if (!isTokenExpired(token) && isAccessToken(token)) {
            final Long userId = getUserIdFromToken(token);
            return (userId != null && userId > 0);
        }
        return false;
    }
    public Boolean validateRefreshToken(String refreshTokenString) {
        return !isTokenExpired(refreshTokenString) && isRefreshToken(refreshTokenString);
    }

    public List<SimpleGrantedAuthority> buildGrantedAuthoritiesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String commaSeparatedListOfClaims = (String) claims.get(USER_ROLES_KEY_NAME);
        Set<String> setOfRoles = StringUtils.commaDelimitedListToSet(commaSeparatedListOfClaims);
        List<SimpleGrantedAuthority> listOfGrantedAuthorities = setOfRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return listOfGrantedAuthorities;
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        // This is a bit confusing.  Even though when the claim is generated, Id is a LONG, once it has been
        // encoded, sent to the client, returned from the client, and decoded, it has magically become and INTEGER
        Integer idInteger =  (Integer) claims.get(USER_ID_KEY_NAME);
        Long idLong = Long.valueOf(idInteger);
        return idLong;

    }

    // Private methods for examining a token

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Boolean isAccessToken(String token) {
        String tokenType = getSubjectFromToken(token);
        return ACCESS_TOKEN_NAME.equals(tokenType);
    }

    private Boolean isRefreshToken(String token) {
        String tokenType = getSubjectFromToken(token);
        return REFRESH_TOKEN_NAME.equals(tokenType);
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private String getSubjectFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }


    /******   CREATING A NEW TOKEN ******/

    public String generateToken(User authenticatedUser) {
        // NOTE:  Three things saved in the token
        // 1) The subject is set to the UserId - We used to put the username here, but with the ability to login through
        //    Google (and other Oauth2 mechanisms), users don't need to create username/password/emailAddress
        // 2) Authentication Claims associated with being having ROLE_USER and/or ROLE_ADMIN
        // 3) A UserId Claim explicitly (This is an historic holdover from when the username was the subject.
        //    Now, this is the same value as the subject.)


        Map<String, Object> claims = new HashMap<>();
        List<String> authorities = authenticatedUser.getAuthorities().stream().map(authority -> authority.getRole().toString()).collect(Collectors.toList());
        // Put the User's ID in the claims
        claims.put(USER_ID_KEY_NAME, authenticatedUser.getId());
        // Put the User's ROLE(S) (e.g. ADMIN_ROLE, USER_ROLE) in the claims
        claims.put(USER_ROLES_KEY_NAME, StringUtils.collectionToDelimitedString(authorities, ","));

        return doGenerateToken(claims, ACCESS_TOKEN_NAME, ACCESS_TOKEN_TIME_TO_LIVE);
    }

    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        // Put the User's ID in the claims
        claims.put(USER_ID_KEY_NAME, userId);

        return doGenerateToken(claims, REFRESH_TOKEN_NAME, REFRESH_ACCESS_TOKEN_TIME_TO_LIVE);

    }

    //while creating the token -
    // 1. Set the claims created by the caller
    // 2. Set the pre-defined claims used by most tokens, like Subject, Time Issued, Expiration Time
    // 3. Sign the JWT using the HS512 algorithm and secret key.
    // 4. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject, int timeToLiveInMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + timeToLiveInMillis))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }


}
