package com.final2.yoseobara.service;

import com.final2.yoseobara.dto.comment.CommentRequestDto;
import com.final2.yoseobara.dto.comment.CommentResponseDto;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.model.Comment;
import com.final2.yoseobara.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    // 코멘트 조회
    public List<CommentResponseDto> getComment(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostId(postId);

        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    // 코멘트 작성
    public CommentResponseDto createComment(CommentRequestDto requestDto, Long postId) {

        // 로그인 중인 멤버 아이디 가져오기
        Long memberId = 0L;

        Comment commnet = commentRepository.save(requestDto.toComment(memberId, postId));
        return new CommentResponseDto(commnet);
    }

    // 코멘트 수정
    public CommentResponseDto updateComment(CommentRequestDto requestDto, Long postId, Long commentId) {

        // 로그인 중인 멤버 아이디 가져오기
        Long memberId = 0L;

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new InvalidValueException(ErrorCode.COMMENT_NOT_FOUND)
        );

        comment.update(requestDto.getContent());
        return new CommentResponseDto(comment);
    }

    // 코멘트 삭제
    public String deleteComment(Long postId, Long commentId) {

            // 로그인 중인 멤버 아이디 가져오기
            Long memberId = 0L;

            Comment comment = commentRepository.findById(commentId).orElseThrow(
                    () -> new InvalidValueException(ErrorCode.COMMENT_NOT_FOUND)
            );

            commentRepository.delete(comment);

            return commentId + " 댓글이 삭제되었습니다.";
    }
}
