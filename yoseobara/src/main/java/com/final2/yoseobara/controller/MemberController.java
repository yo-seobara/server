package com.final2.yoseobara.controller;

import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.dto.response.MemberResponseDto;
import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.service.CommentService;
import com.final2.yoseobara.service.MemberService;
import com.final2.yoseobara.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.final2.yoseobara.dto.request.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;
    private final CommentService commentService;
    private final PostService postService;
    
    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    public ResponseDto<?> signup(@RequestBody @Valid MemberRequestDto requestDto) {
        return memberService.createUser(requestDto);
    }


    @RequestMapping(value = "/signup/nicknameCheck", method = RequestMethod.POST)
    public int nicknameCheck(@RequestBody @Valid NicknameRequestDto nicknameRequestDto) {
        return memberService.nicknameCheck(nicknameRequestDto);
    }


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseDto<?> login(@RequestBody @Valid LoginRequestDto requestDto,
                                HttpServletResponse response) {
        return memberService.login(requestDto, response);
    }

    // 로그인된 멤버 정보 조회
    @GetMapping("/myinfo")
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
                .authority(userDetailsImpl.getMember().getAuthority())
                .build());
    }

    // 유저페이지 리스팅 (페이지) - 멤버 아이디로
    // 정렬, 검색 가능
    @GetMapping("/posts/{memberId}")
    public ResponseDto<?> getPostPageByMemberId(@PathVariable Long memberId,
                                                @RequestParam(value = "search", defaultValue = "") String search,
                                                @RequestParam(value = "keyword", defaultValue = "") String keyword,
                                                @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseDto.success(postService.getPostPageByMemberId(memberId, search, keyword, pageable));
    }

    // 유저페이지 리스팅 (페이지) - 닉네임으로
    // 정렬, 검색 가능
    @GetMapping("/posts/{nickname}")
    public ResponseDto<?> getPostPageByNickname(@PathVariable String nickname,
                                                @RequestParam(value = "search", defaultValue = "") String search,
                                                @RequestParam(value = "keyword", defaultValue = "") String keyword,
                                                @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseDto.success(postService.getPostPageByNickname(nickname, search, keyword, pageable));
    }

    // 내가 쓴 글 리스팅 (페이지)
    // 정렬, 검색 가능
    @GetMapping("/myposts")
    public ResponseDto<?> getMyPostPage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestParam(value = "search", defaultValue = "") String search,
                                        @RequestParam(value = "keyword", defaultValue = "") String keyword,
                                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){
        if (Objects.isNull(userDetails)) {
            throw new InvalidValueException(ErrorCode.LOGIN_REQUIRED);
        }
        String myNickname = userDetails.getMember().getNickname();
        return ResponseDto.success(postService.getPostPageByNickname(myNickname, search, keyword, pageable));
    }


    // 내가 쓴 댓글 조회
    // 정렬 가능
    @GetMapping("/mycomments")
    public ResponseDto<?> getMyComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                       @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (userDetails == null) {
            return ResponseDto.fail(ErrorCode.LOGIN_REQUIRED);
        }
        return ResponseDto.success(commentService.getMyComment(userDetails.getMember().getMemberId(), pageable));
    }
}
