package com.urlshortener.demo.User;

public class UserRegistrationRequest {

    //TODO: Add fields for registration request.

    private String username;

    private String password;

    public UserRegistrationRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }



}
