package com.wolfesoftware.stocks.model;

import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

/**
 * Entity implementation class for Entity: Stock
 *
 */
@Entity
@Table(name="authority")
public class Authority extends PersistentEntity {



    @ManyToOne
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Authority() {

    }

    public Authority(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public enum Role { ROLE_ADMIN, ROLE_USER };

} 