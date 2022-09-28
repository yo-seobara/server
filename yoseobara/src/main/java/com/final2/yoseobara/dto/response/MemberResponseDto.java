package com.final2.yoseobara.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.final2.yoseobara.dto.request.TokenDto;
import com.final2.yoseobara.shared.Authority;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Authority authority;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TokenDto token;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime modifiedAt;
}