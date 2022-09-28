package com.final2.yoseobara.controller;

import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.dto.request.NicknameRequestDto;
import com.final2.yoseobara.dto.request.LoginRequestDto;
import com.final2.yoseobara.dto.request.MemberRequestDto;
import com.final2.yoseobara.dto.response.MemberResponseDto;
import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RequiredArgsConstructor
@RestController
public class MemberController {
    private final MemberService memberService;

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
                                HttpServletResponse response
    ) {
        return memberService.login(requestDto, response);
    }

    // 로그인된 멤버 정보 조회
    @GetMapping("api/member/myinfo")
    public ResponseDto<?> myinfo(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        // 로그인 확인
        if (userDetailsImpl == null) {
            return ResponseDto.fail(ErrorCode.LOGIN_REQUIRED);
        }
        // 정보 가져오기
        return ResponseDto.success(MemberResponseDto.builder()
                .id(userDetailsImpl.getMember().getMemberId())
                .username(userDetailsImpl.getMember().getUsername())
                .nickname(userDetailsImpl.getMember().getNickname())
                .build());
    }
}
