package com.final2.yoseobara.model;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@Setter
@RequiredArgsConstructor
public class Comment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;
    private String content;
    private Long memberId;

    // post 엔티티를 맵핑하는 것과 post 아이디를 가지고 있는 것 중 어떤 것이 효율적일까
    private Long postId;

    @Builder
    public Comment(Long commentId, String content, Long memberId, Long postId) {
        this.commentId = commentId;
        this.content = content;
        this.memberId = memberId;
        this.postId = postId;
    }

    public void update(String content) {
        this.content = content;
    }
}
