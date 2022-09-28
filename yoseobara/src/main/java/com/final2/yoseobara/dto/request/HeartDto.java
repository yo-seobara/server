package com.final2.yoseobara.dto.request;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeartDto {
    private Long postId;
    private Long memberId;
}