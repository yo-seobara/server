package com.final2.yoseobara.controller;

import antlr.Token;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.final2.yoseobara.dto.request.NicknameRequestDto;
import com.final2.yoseobara.dto.request.LoginRequestDto;
import com.final2.yoseobara.dto.request.MemberRequestDto;
import com.final2.yoseobara.dto.request.TokenDto;
import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.service.KakaoMemberService;
import com.final2.yoseobara.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;

    private final KakaoMemberService kakaoMemberService;

    @RequestMapping(value = "/api/member/signup", method = RequestMethod.POST)
    public ResponseDto<?> signup(@RequestBody @Valid MemberRequestDto requestDto) {
        return memberService.createUser(requestDto);
    }

    @RequestMapping(value = "/api/member/signup/nicknameCheck", method = RequestMethod.POST)
    public int nicknameCheck(@RequestBody @Valid NicknameRequestDto nicknameRequestDto) {
        return memberService.nicknameCheck(nicknameRequestDto);
    }

    @RequestMapping(value = "/api/member/login", method = RequestMethod.POST)
    public ResponseDto<?> login(@RequestBody @Valid LoginRequestDto requestDto,
                                HttpServletResponse response) {
        return memberService.login(requestDto, response);
    }

    @GetMapping("/member/kakao/callback")
    public TokenDto kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        return kakaoMemberService.kakaoLogin(code, response);
    }






}
