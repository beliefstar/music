package com.zx.music.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.JWTValidator;
import com.alibaba.fastjson2.JSON;
import com.zx.music.bean.LoginBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JwtHandler {

    @Value("${JWT_TOKEN:fbf3a6a74df34b35a145d9bf341483cc}")
    private String jwtToken;

    public static final long DayMillis = 1000L * 60 * 60 * 24;
    public static final long RememberExpire = DayMillis * 30;
    public static final long UnRememberExpire = DayMillis;

    public String getJwtToken() {
        return jwtToken;
    }

    public String createToken(LoginBean lb, boolean remember) {
        Date expire;
        if (remember) {
            expire = new Date(System.currentTimeMillis() + RememberExpire);
        } else {
            expire = new Date(System.currentTimeMillis() + UnRememberExpire);
        }
        return JWT.create()
                .setNotBefore(DateUtil.date())
                .setIssuedAt(DateUtil.date())
                .setExpiresAt(expire)
                .setKey(jwtToken.getBytes())
                .setPayload("loginBean", lb)
                .sign();
    }

    public LoginBean parseToken(String token) {
        if (JWTUtil.verify(token, jwtToken.getBytes())) {
            try {
                JWTValidator.of(token).validateDate();
            } catch (ValidateException e) {
                return null;
            }
            return JSON.parseObject(JWT.of(token).getPayload("loginBean").toString(), LoginBean.class);
        }
        return null;
    }
}
