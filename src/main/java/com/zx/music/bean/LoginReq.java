package com.zx.music.bean;

import lombok.Data;

@Data
public class LoginReq {

    private String username;

    private String password;

    private String remember;

}
