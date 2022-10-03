package com.final2.yoseobara.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Getter
public class PostResponseDto {
    private Long postId;
    private String title;
    private String content;
    private String address;
    private HashMap<String, Double> location;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime modifiedAt;
    private List<String> imageUrls;
    private String thumbnailUrl;
    private Long view;
    private Long heart; // 좋아요 계산
    private Boolean myHeart; // 좋아요 여부
    private String nickname; // 로그인된 작성자의 닉네임 받아오기
    private Long memberId;

    @Builder // 이미지와 썸네일 추가하기
    public PostResponseDto(Post post, List<String> imageUrls, String nickname, Long heart, Long view, Long memberId) {
        this.postId = post.getPostId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.address = post.getAddress();
        this.location = new HashMap<>();
        this.location.put("lng", post.getLng());
        this.location.put("lat", post.getLat());
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.imageUrls = imageUrls;
        this.thumbnailUrl = post.getThumbnailUrl();
        this.view = view;
        this.heart = heart;
        this.myHeart = myHeart != null && myHeart; // null 일 때 false
        this.nickname = nickname;
        this.memberId = memberId;
    }
}