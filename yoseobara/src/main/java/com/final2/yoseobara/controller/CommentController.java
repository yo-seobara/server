package com.final2.yoseobara.controller;

import com.final2.yoseobara.dto.comment.CommentRequestDto;
import com.final2.yoseobara.response.ResponseDto;
import com.final2.yoseobara.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;


    // 코멘트 조회
    @GetMapping
    public ResponseDto getComment(@PathVariable Long postId) {
        return ResponseDto.success(commentService.getComment(postId));
    }

    // 코멘트 작성
    public ResponseDto createComment(CommentRequestDto requestDto, @PathVariable Long postId) {
        return ResponseDto.success(commentService.createComment(requestDto, postId));
    }

    // 코멘트 수정
    @RequestMapping("/{commentId}")
    public ResponseDto updateComment(CommentRequestDto requestDto, @PathVariable Long postId, @PathVariable Long commentId) {
        return ResponseDto.success(commentService.updateComment(requestDto, postId, commentId));
    }

    // 코멘트 삭제
    @RequestMapping("/{commentId}")
    public ResponseDto deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        return ResponseDto.success(commentService.deleteComment(postId, commentId));
    }
}
