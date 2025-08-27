package com.zx.music.common;

import com.zx.music.bean.LoginBean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtHandlerTest {

    @Test
    void createToken() {
        String token = "fbf3a6a74df34b35a145d9bf341483cc";

        JwtHandler jwtHandler = new JwtHandler();
        jwtHandler.setJwtToken(token);

        LoginBean bean = new LoginBean();
        bean.setUsername("zhenxin");
        System.out.println(jwtHandler.createToken(bean, true));
    }
}