package com.final2.yoseobara.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class KakaoController {
    @GetMapping("/login")
    public String kakao(){
        return "login";
    }
}