package com.final2.yoseobara.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.final2.yoseobara.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Getter

public class PostResponseDto {
    private Long postid;
    private String title;
    private String content;
    private String address;
    private HashMap<String, Float> location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime modifiedAt;

    private Long view; // 조회수 계산
    private Long heart; // 좋아요 계산
    private String nickname; // 로그인된 작성자의 닉네임 받아오기

    @Builder // 이미지와 썸네일 추가하기
    public PostResponseDto(Post post, Long view, Long heart, String nickname) {
        this.postid = post.getPostId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.address = post.getAddress();
        this.location = post.getLocation();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.view = view;
        this.heart = heart;
        this.nickname = nickname;
    }
}