package com.final2.yoseobara.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.final2.yoseobara.domain.Comment;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
public class CommentResponseDto {
    private Long postId;
    private String nickname;
    private Long commentId;
    private String content;
    private Long memberId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Builder
    public CommentResponseDto(Comment comment) {
        this.memberId = comment.getMember().getMemberId();
        this.postId = comment.getPost().getPostId();
        this.nickname = comment.getMember().getNickname();
        this.commentId = comment.getCommentId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.modifiedAt = comment.getModifiedAt();
    }
}
