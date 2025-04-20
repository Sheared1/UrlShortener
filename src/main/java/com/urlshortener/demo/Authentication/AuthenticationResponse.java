package com.urlshortener.demo.Authentication;

public class AuthenticationResponse {

    private String token;

    public AuthenticationResponse(String token) {
        this.token = token;
    }

    //Default constructor
    public AuthenticationResponse() {

    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

}
