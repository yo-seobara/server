package com.final2.yoseobara.controller;


import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.dto.request.CommentRequestDto;
import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
@RestController
public class CommentController {
    private final CommentService commentService;

    // 게시글 코멘트 조회
    @GetMapping
    public ResponseDto<?> getComment(@PathVariable Long postId) {
        return ResponseDto.success(commentService.getComment(postId));
    }

    // 코멘트 작성
    @PostMapping
    public ResponseDto<?> createComment(@RequestBody CommentRequestDto requestDto, @PathVariable Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseDto.success(commentService.createComment(requestDto, postId, userDetailsImpl));
    }

    // 코멘트 수정
    @PutMapping("/{commentId}")
    public ResponseDto<?> updateComment(@RequestBody CommentRequestDto requestDto, @PathVariable Long postId, @PathVariable Long commentId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseDto.success(commentService.updateComment(requestDto, postId, commentId, userDetailsImpl));
    }

    // 코멘트 삭제
    @DeleteMapping("/{commentId}")
    public ResponseDto<?> deleteComment(@RequestBody @PathVariable Long postId, @PathVariable Long commentId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        return ResponseDto.success(commentService.deleteComment(postId, commentId, userDetailsImpl));
    }
}
