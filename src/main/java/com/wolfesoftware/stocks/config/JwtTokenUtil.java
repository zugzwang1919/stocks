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

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Boolean validateToken(String token) {
        final String username = getUsernameFromToken(token);
        return (username != null && !isTokenExpired(token));
    }

    public List<SimpleGrantedAuthority> buildGrantedAuthoritiesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        String commaSeparatedListOfClaims = (String) claims.get("ROLES");
        Set<String> setOfRoles = StringUtils.commaDelimitedListToSet(commaSeparatedListOfClaims);
        List<SimpleGrantedAuthority> listOfGrantedAuthorities = setOfRoles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        return listOfGrantedAuthorities;
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
        Map<String, Object> claims = new HashMap<>();
        List<String> authorities = authenticatedUser.getAuthorities().stream().map(authority -> authority.getRole().toString()).collect(Collectors.toList());
        String value = StringUtils.collectionToDelimitedString(authorities, ",");
        String key = "ROLES";
        claims.put(key, value);
        return doGenerateToken(claims, authenticatedUser.getUsername());
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
