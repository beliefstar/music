package com.zx.music.controller;

import cn.hutool.core.util.StrUtil;
import com.zx.music.bean.LoginBean;
import com.zx.music.bean.LoginReq;
import com.zx.music.bean.LoginRes;
import com.zx.music.common.JwtHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtHandler jwtHandler;

    @PostMapping("/login")
    public LoginRes login(@RequestBody LoginReq loginReq) {
        String username = loginReq.getUsername();
        String password = loginReq.getPassword();

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return LoginRes.error("参数不能为空");
        }

        if (!password.contains(username)) {
            return LoginRes.error("登录失败");
        }
        password = password.replace(username, "");
        if (!Objects.equals(password, "1115")) {
            return LoginRes.error("登录失败");
        }

        LoginBean b = new LoginBean();
        b.setUsername(loginReq.getUsername());

        String token = jwtHandler.createToken(b, StrUtil.equalsIgnoreCase(loginReq.getRemember(), "on"));
        return LoginRes.success(loginReq.getUsername(), token);
    }
}
