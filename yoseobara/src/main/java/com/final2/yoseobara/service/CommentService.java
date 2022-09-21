package com.final2.yoseobara.service;

import com.final2.yoseobara.domain.Comment;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.dto.request.CommentRequestDto;
import com.final2.yoseobara.dto.response.CommentResponseDto;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.repository.CommentRepository;
import com.final2.yoseobara.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 코멘트 조회
    public List<CommentResponseDto> getComment(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostPostId(postId);

        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    // 코멘트 작성
    public CommentResponseDto createComment(CommentRequestDto requestDto, Long postId, UserDetailsImpl userDetailsImpl) {

        // 로그인 중인 멤버 아이디 가져오기
        Member member = userDetailsImpl.getMember();

        // 작성 중인 게시글
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.POST_NOT_FOUND));

        Comment commnet = commentRepository.save(Comment.builder()
                .content(requestDto.getContent())
                .member(member)
                .post(post)
                .build());

        return CommentResponseDto.builder()
                .comment(commnet)
                .build();
    }

    // 코멘트 수정
    public CommentResponseDto updateComment(CommentRequestDto requestDto, Long postId, Long commentId, UserDetailsImpl userDetailsImpl) {

        // 로그인 중인 멤버 아이디 가져오기
        Member member = userDetailsImpl.getMember();

        // 작성 중인 게시글
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.POST_NOT_FOUND));

        // 수정할 코멘트
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new InvalidValueException(ErrorCode.COMMENT_NOT_FOUND)
        );

        // 코멘트 수정 권한 확인
        if (!comment.getMember().getMemberId().equals(member.getMemberId())) {
            throw new InvalidValueException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        comment.update(requestDto.getContent());
        commentRepository.save(comment);

        return CommentResponseDto.builder()
                .comment(comment)
                .build();
    }

    // 코멘트 삭제
    public String deleteComment(Long postId, Long commentId, UserDetailsImpl userDetailsImpl) {

        // 로그인 중인 멤버 아이디 가져오기
        Member member = userDetailsImpl.getMember();

        // 작성 중인 게시글
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.POST_NOT_FOUND));

        // 삭제할 코멘트
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new InvalidValueException(ErrorCode.COMMENT_NOT_FOUND)
        );

        // 코멘트 삭제 권한 확인
        if (!comment.getMember().getMemberId().equals(member.getMemberId())) {
            throw new InvalidValueException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        commentRepository.delete(comment);

        return commentId + "번 댓글이 삭제되었습니다.";
    }
}
