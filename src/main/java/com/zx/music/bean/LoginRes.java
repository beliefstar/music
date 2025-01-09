package com.zx.music.bean;

import lombok.Data;

@Data
public class LoginRes {

    private Integer code;

    private String message;

    private String username;

    private String token;

    public static LoginRes error(String message) {
        LoginRes loginRes = new LoginRes();
        loginRes.setCode(-1);
        loginRes.setMessage(message);
        return loginRes;
    }

    public static LoginRes success(String username, String token) {
        LoginRes loginRes = new LoginRes();
        loginRes.setCode(200);
        loginRes.setUsername(username);
        loginRes.setToken(token);
        return loginRes;
    }
}
