package com.final2.yoseobara.dto.comment;

import com.final2.yoseobara.model.Comment;
import lombok.Getter;

@Getter
public class CommentRequestDto {
    private String content;

    public Comment toComment(Long memberId, Long postId) {
        return Comment.builder()
                // post 맵핑?
                .postId(postId)
                // member 맵핑?
                .memberId(memberId)
                .content(content)
                .build();
    }
}
