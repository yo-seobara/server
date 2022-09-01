package com.final2.yoseobara.dto.comment;

import com.final2.yoseobara.model.Comment;
import lombok.Builder;

import java.util.Date;

public class CommentResponseDto {
    private Long commentId;
    private Long postId;
    private String content;
    private String nickname;
    private Date createdAt;
    private Date modifiedAt;

    @Builder
    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getCommentId();
        this.postId = comment.getPostId();
        this.content = comment.getContent();
        this.nickname = ""; // 로그인된 멤버 아이디로 닉네임 받아오기
        this.createdAt = null; // 생성일
        this.modifiedAt = null; // 수정일
    }
}
