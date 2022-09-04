package com.final2.yoseobara.controller.request;

import lombok.*;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class S3RequestDto {
    private String imageUrl;
}
