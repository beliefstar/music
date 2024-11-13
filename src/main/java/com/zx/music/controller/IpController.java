package com.zx.music.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/ip")
public class IpController {

    @PostMapping("/unlock")
    public String unlock() {
        return "ok";
    }
}
