package com.final2.yoseobara.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequestDto {

    @NotBlank
    @Pattern(regexp = "^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$")
    private String username;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z\\d]{5,}$") //대소문자숫자 상관없이 최소 5자리.
    private String password;

    @NotBlank
    private String passwordCheck;

    @NotBlank
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-zA-Z0-9-_]{2,10}$") //한글영어숫자 상관없이 최소2자리~10자리.
    private String nickname;

//
//    @NotBlank
//    private String nicknameCheck;
}

