/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.model;

public class JwtResponse {

    private final String username;
    private final String token;
    private final Boolean isAdmin;
    private final String refreshToken;

    public JwtResponse(String username, String token, Boolean isAdmin, String refreshToken) {
        this.username = username;
        this.token = token;
        this.isAdmin = isAdmin;
        this.refreshToken = refreshToken;
    }

    public String getUsername() {
        return this.username;
    }
    public String getToken() {
        return this.token;
    }
    public Boolean getAdmin() {
        return isAdmin;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
}
