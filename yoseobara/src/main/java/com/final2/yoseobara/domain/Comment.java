package com.final2.yoseobara.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class Comment extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonIgnore
    private Member member;

    // post 엔티티를 맵핑하는 것과 post 아이디를 가지고 있는 것 중 어떤 것이 효율적일까
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private Post post;

    @Builder
    public Comment(String content, Member member, Post post) {
        this.content = content;
        this.member = member;
        this.post = post;
    }

    public void update(String content) {
        this.content = content;
        this.setModifiedAt(LocalDateTime.now());
    }
}
