package com.final2.yoseobara.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.final2.yoseobara.dto.request.KakaoTokenDto;
import com.final2.yoseobara.service.KakaoMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@RestController
public class KakaoController {

    private final KakaoMemberService kakaoMemberService;

    @GetMapping("/member/kakao/callback")
    public KakaoTokenDto kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        return kakaoMemberService.kakaoLogin(code, response);
    }
}
