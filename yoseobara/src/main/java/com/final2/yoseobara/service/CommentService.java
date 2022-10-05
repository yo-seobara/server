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
import com.final2.yoseobara.shared.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 게시글 코멘트 조회
    public List<CommentResponseDto> getComment(Long postId, Pageable pageable) {

        Sort sort = pageable.getSort().and(Sort.by("createdAt").descending());
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        List<Comment> comments = commentRepository.findAllByPostPostId(postId, page);

        return comments.stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());
    }

    // 내가 쓴 코멘트 조회 (페이지)
    public Page getMyComment(Long memberId, Pageable pageable) {

        // 정렬의 디폴트는 최신순
        Sort sort = pageable.getSort().and(Sort.by("createdAt").descending());
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Comment> commentPage = commentRepository.findAllByMember_MemberId(memberId, page);
        Page<CommentResponseDto> commentPageDto = commentPage.map(
                comment -> CommentResponseDto.builder()
                .comment(comment)
                .build());
        return commentPageDto;
    }

    // 코멘트 작성
    public CommentResponseDto createComment(CommentRequestDto requestDto, Long postId, UserDetailsImpl userDetailsImpl) {

        // 로그인 중인 멤버 아이디 가져오기
        Member member = userDetailsImpl.getMember();

        // 작성 중인 게시글
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.POST_NOT_FOUND));

        Comment comment = commentRepository.save(Comment.builder()
                .content(requestDto.getContent())
                .member(member)
                .post(post)
                .build());

        return CommentResponseDto.builder()
                .comment(comment)
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
        if (!(comment.getMember().getMemberId().equals(member.getMemberId()) || member.getAuthority().equals(Authority.ROLE_ADMIN))) {
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
        if (!(comment.getMember().getMemberId().equals(member.getMemberId()) || member.getAuthority().equals(Authority.ROLE_ADMIN))) {
            throw new InvalidValueException(ErrorCode.COMMENT_UNAUTHORIZED);
        }

        commentRepository.delete(comment);

        return commentId + "번 댓글이 삭제되었습니다.";
    }
}
