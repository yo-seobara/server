package com.final2.yoseobara.controller.response;

import com.final2.yoseobara.controller.request.TokenDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private TokenDto token;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
