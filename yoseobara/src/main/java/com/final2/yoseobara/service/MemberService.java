package com.final2.yoseobara.service;

import com.final2.yoseobara.dto.request.LoginRequestDto;
import com.final2.yoseobara.dto.request.TokenDto;
import com.final2.yoseobara.dto.request.MemberRequestDto;
import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.dto.response.MemberResponseDto;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.jwt.TokenProvider;
import com.final2.yoseobara.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MemberService {


    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public ResponseDto<?> createUser(MemberRequestDto requestDto) {
        if (null != isPresentMember(requestDto.getUsername())) {
            return ResponseDto.fail(ErrorCode.DUPLICATED_USERNAME);
        }

        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
            return ResponseDto.fail(ErrorCode.PASSWORDS_NOT_MATCHED);
        }

        if (null != isPresentNickname(requestDto.getNickname())) {
            return ResponseDto.fail(ErrorCode.DUPLICATED_NICKNAME);
        }


        Member member = Member.builder()
                .username(requestDto.getUsername())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .nickname(requestDto.getNickname())
                .build();
        memberRepository.save(member);
        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getMemberId())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .build()
        );
    }

    @Transactional
    public int nicknameCheck(NicknameRequestDto nicknameRequestDto) {

        int result = 0; // 닉네임 사용가능

        if (null != isPresentNickname(nicknameRequestDto.getNickname())) {
            result = 1; // 닉네임 사용불가(중복됨)
        }

        return result;
    }



    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = isPresentMember(requestDto.getUsername());
        if (null == member) {
            return ResponseDto.fail(ErrorCode.USER_NOT_FOUND);
        }

        if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) {
            return ResponseDto.fail(ErrorCode.INVALID_USER);
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);

        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getMemberId())
                        .username(member.getUsername())
                        .nickname(member.getNickname())
                        .token(tokenDto)
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .build()
        );
    }






    @Transactional(readOnly = true)
    public Member isPresentMember(String username) {
        Optional<Member> optionalMember = memberRepository.findByUsername(username);
        return optionalMember.orElse(null);
    }

    @Transactional(readOnly = true)
    public Member isPresentNickname(String nickname) {
        Optional<Member> optionalMember = memberRepository.findByNickname(nickname);
        return optionalMember.orElse(null);
    }

    public void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("Refresh-Token", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
    }

}
