package com.final2.yoseobara.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.final2.yoseobara.model.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter

public class PostResponseDto {
    private Long id;
    private String title;
    private String nickname;
    private String post;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime modifiedAt;
    private int countReply;

    @Builder
    public PostResponseDto(Post content, int countReply) {
        this.id = content.getId();
        this.title = content.getTitle();
        this.nickname = content.getNickname();
        this.post = content.getPost();
        this.modifiedAt = content.getModifiedAt();
        this.countReply = countReply;
    }
}