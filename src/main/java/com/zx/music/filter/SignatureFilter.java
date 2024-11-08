package com.zx.music.filter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SignatureFilter extends OncePerRequestFilter {

    @Value("${HW_IOT_FORWARD_TOKEN:fbf3a6a74df34b35a145d9bf341483cc}")
    public String token;

    @Autowired
    private IpBlackHandler ipBlackHandler;

    public static final String UNLOCK_SUFFIX = "/api/ip/unlock";

    public static final String BASIC_PREFIX = "Basic ";

    public static final int NONE = -1;
    public static final int SUCCESS = 0;
    public static final int FORBIDDEN = 1;
    public static final int UNAUTHORIZED = 2;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        int code = internalFilter(request, response, filterChain);
        if (code == SUCCESS) {
            filterChain.doFilter(request, response);
            return;
        }
        if (code == FORBIDDEN) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (code == UNAUTHORIZED) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("WWW-Authenticate", "Basic realm=\"nav music\"");
        }
    }

    private int internalFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        String uri = request.getRequestURI();

        String ip = JakartaServletUtil.getClientIP(request);
        boolean isUnlockReq = uri.endsWith(UNLOCK_SUFFIX);
        if (ipBlackHandler.contains(ip) && !isUnlockReq) {
            return FORBIDDEN;
        }

        int code = tryAuth(request, response, filterChain);
        if (code >= 0) {
            if (code == SUCCESS) {
                return SUCCESS;
            }
            return FORBIDDEN;
        }

        String timestamp = getParam(request, "timestamp");
        String nonce = getParam(request, "nonce");
        String signature = getParam(request, "signature");
        if (checkTimestamp(timestamp) && checkSignature(nonce, timestamp, signature, token)) {
            if (isUnlockReq) {
                ipBlackHandler.remove(ip);
            }
            return SUCCESS;
        }
        if (uri.startsWith("/api")) {
            ipBlackHandler.add(ip);
        }
        return UNAUTHORIZED;
    }

    private int tryAuth(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        String authorization = request.getHeader("Authorization");
        if (StrUtil.isBlank(authorization)) {
            return NONE;
        }

        if (!authorization.startsWith(BASIC_PREFIX)) {
            return UNAUTHORIZED;
        }
        authorization = authorization.substring(BASIC_PREFIX.length());

        if (StrUtil.isBlank(authorization)) {
            return UNAUTHORIZED;
        }

        authorization = Base64.decodeStr(authorization);

        List<String> split = StrUtil.split(authorization, ":", true, true);
        if (split.size() != 2) {
            return UNAUTHORIZED;
        }
        String username = split.get(0);
        String password = split.get(1);

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return UNAUTHORIZED;
        }

        if (!password.contains(username)) {
            return UNAUTHORIZED;
        }
        password = password.replace(username, "");
        if (!Objects.equals(password, "1115")) {
            return UNAUTHORIZED;
        }
        return SUCCESS;
    }

    private boolean checkTimestamp(String timestamp) {
        if (!NumberUtil.isLong(timestamp)) {
            return false;
        }
        long milli = NumberUtil.parseLong(timestamp);
        long limit = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10);
        return milli > limit;
    }

    public boolean checkSignature(String nonce, String timestamp, String signature, String token) {
        String serverSignature = buildSignature(nonce, timestamp, token);
        return StrUtil.isNotEmpty(serverSignature) && serverSignature.equals(signature);
    }

    private static String buildSignature(String nonce, String timestamp, String token) {
        List<String> list = new ArrayList<>();
        list.add(token);
        if (StrUtil.isNotEmpty(nonce)) {
            list.add(nonce);
        }
        if (StrUtil.isNotEmpty(timestamp)) {
            list.add(timestamp);
        }
        Collections.sort(list);
        StringBuilder signatureBuilder = new StringBuilder();
        for (String s : list) {
            signatureBuilder.append(s);
        }
        return DigestUtils.sha256Hex(signatureBuilder.toString());
    }

    private String getParam(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        if (StrUtil.isBlank(value)) {
            value = request.getParameter(name);
        }
        return value;
    }

    public static void main(String[] args) {

        String s = "bmFtZTpwYXNzd29yZA==";
        System.out.println(Base64.decodeStr(s));

        if (true) return;

        String timestamp = Long.toString(System.currentTimeMillis());
        String nonce = UUID.randomUUID().toString();
        String token = "fbf3a6a74df34b35a145d9bf341483cc";
        String signature = buildSignature(nonce, timestamp, token);

        System.out.println(timestamp);
        System.out.println(nonce);
        System.out.println(signature);

        HttpResponse response = HttpRequest.post("http://154.12.55.187:8080/api/ip/unlock")
                .header("timestamp", timestamp)
                .header("nonce", nonce)
                .header("signature", signature).execute();
        System.out.println(response.getStatus());
        System.out.println(response.body());
        response.close();

    }
}
