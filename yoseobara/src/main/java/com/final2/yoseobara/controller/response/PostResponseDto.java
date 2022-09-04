package com.final2.yoseobara.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.final2.yoseobara.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter

public class PostResponseDto {
    private Long postid;
    private String title;
    private String nickname;
    private String content;
    private Float address;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime modifiedAt;

    @Builder
    public PostResponseDto(Post post){
        this.postid = post.getPostid();
        this.title = post.getTitle();
        this.nickname = post.getNickname();
        this.content = post.getContent();
        this.modifiedAt = post.getModifiedAt();
        this.address = post.getAddress();
    }
}