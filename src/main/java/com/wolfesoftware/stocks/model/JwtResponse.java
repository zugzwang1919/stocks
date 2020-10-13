/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wolfesoftware.stocks.model;

public class JwtResponse {

    private final String jwttoken;
    private final Boolean isAdmin;

    public JwtResponse(String jwttoken, Boolean isAdmin) {
        this.jwttoken = jwttoken;
        this.isAdmin = isAdmin;
    }

    public String getToken() {
        return this.jwttoken;
    }
    public Boolean getAdmin() {
        return isAdmin;
    }
}
