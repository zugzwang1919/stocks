package com.wolfesoftware.stocks.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * Entity implementation class for Entity: Stock
 *
 */
@Entity
@Table(name="user")
public class User extends PersistentEntity {


    @Size(max=32)
    private String username;

    // NOTE: JSON properties are defined on the getters and setters so
    // that a caller can create a new user (and specify a password), but
    // the user's password is never returned to any caller
    @Size(max=32)
    private String password;

    @Size(max=255)
    private String emailaddress;

    // NOTE: People can log in with either user/password/email  OR
    // NOTE: using their Google credentials.
    private String googleid;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Authority> authorities;


    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    // Never include the password in any JSON that may be returned to a caller
    @JsonIgnore
    public String getPassword() {
        return password;
    }

    // Allow a caller to create a user from JSON that was passed in by the caller
    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String emailAddress) {
        this.emailaddress = emailAddress;
    }

    // Google does not recommend passing the user's GoogleId between UI and server,
    // so don't ever return this to the client
    @JsonIgnore
    public String getGoogleid() {
        return googleid;
    }

    public void setGoogleid(String googleid) {
        this.googleid = googleid;
    }

    public List<Authority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }




}
