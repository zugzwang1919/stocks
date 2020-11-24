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

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil implements Serializable {

    private static final long serialVersionUID = -2550185165626007488L;
    public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

    @Value("${jwt.secret}")
    private String secret;



    /*****  EXAMINING a TOKEN *****/



    public Boolean validateToken(String token) {
        final Long userId = getUserIdFromToken(token);
        return (userId != null && userId > 0 && !isTokenExpired(token));
    }

    public List<SimpleGrantedAuthority> buildGrantedAuthoritiesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String commaSeparatedListOfClaims = (String) claims.get("ROLES");
        Set<String> setOfRoles = StringUtils.commaDelimitedListToSet(commaSeparatedListOfClaims);
        List<SimpleGrantedAuthority> listOfGrantedAuthorities = setOfRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return listOfGrantedAuthorities;
    }
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        // This is a bit confusing.  Even though when the claim is generated, Id is a LONG, once it has been
        // encoded, sent to the client, returned from the client, and decoded, it has magically become and INTEGER
        Integer idInteger =  (Integer) claims.get("ID");
        Long idLong = Long.valueOf(idInteger);
        return idLong;

    }

    // Private methods for examining a token

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    private Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
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
        claims.put("ID", authenticatedUser.getId());
        // Put the User's ROLE(S) (e.g. ADMIN_ROLE, USER_ROLE) in the claims
        claims.put("ROLES", StringUtils.collectionToDelimitedString(authorities, ","));

        return doGenerateToken(claims, authenticatedUser.getId().toString());
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
    //   compaction of the JWT to a URL-safe string
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }


}
