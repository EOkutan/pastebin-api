package com.pastebin.demo.dto;

public class LoginResponseDto {
    public LoginResponseDto setToken(String token) {
        this.token = token;
        return this;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public LoginResponseDto setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    private String token;

    private long expiresIn;

    public String getToken() {
        return token;
    }

}
