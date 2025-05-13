package com.urlshortener.demo.User;

import java.util.Set;

public class UserResponse {

    private String username;
    private Set<String> roles;

    public UserResponse(String username, Set<String> roles) {
        this.username = username;
        this.roles = roles;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
