package com.final2.yoseobara.controller;

import com.final2.yoseobara.domain.Post;
import com.final2.yoseobara.dto.request.PostRequestDto;
import com.final2.yoseobara.dto.response.PostResponseDto;
import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.repository.PostRepository;
import com.final2.yoseobara.service.PostService;
import com.final2.yoseobara.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")

public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;
    private final S3Service s3Service;


    // 게시글 작성
    @PostMapping
    public ResponseDto<?> createPost(@RequestPart PostRequestDto postRequestDto,
                                     @RequestPart(required = false) MultipartFile[] images,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
        // 로그인 확인
        if(userDetailsImpl.getMember() == null){
            return ResponseDto.fail(ErrorCode.LOGIN_REQUIRED);
        }

        // 로그인된 유저의 아이디
        Long memberId = userDetailsImpl.getMember().getMemberId();

        // 이미지 파일 1개 이상 3개 이하
        if(images == null){
            return ResponseDto.fail(ErrorCode.POST_IMAGE_REQUIRED);
        } else if (images.length > 3){
            return ResponseDto.fail(ErrorCode.POST_IMAGE_MAX);
        }

        // 이미지 S3 업로드
        List<String> imageUrls = s3Service.uploadFile(images);

        return ResponseDto.success(postService.createPost(postRequestDto, imageUrls, memberId));
    }

    // 게시글 전체 조회
    @GetMapping
    public List<PostResponseDto> getPostList(){
        return postService.getPostList();
    }

    // 게시물 상세 조회
    @ResponseBody
    @GetMapping("/{postId}")
    public PostResponseDto getPost(@PathVariable Long postId){
        return postService.getPost(postId);
    }

    // 게시물 페이지네이션
    @GetMapping("/paging")
    public List<PostResponseDto> getPostListPaging(@RequestParam int page){
        return null;
    }

    // 게시물 수정
    @PutMapping("/{postId}")
    public ResponseDto<?> updatePost(@PathVariable Long postId,
                                     @RequestPart PostRequestDto postRequestDto,
                                     @RequestPart(required = false) MultipartFile[] newImages,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {

        // 로그인 확인
        if(userDetailsImpl.getMember() == null){
            return ResponseDto.fail(ErrorCode.LOGIN_REQUIRED);
        }

        // 로그인된 유저의 아이디
        Long memberId = userDetailsImpl.getMember().getMemberId();

        // 타겟 게시물 수정 (새 이미지 있을 때, 없을 때 상관 없음)
        PostResponseDto post = postService.updatePost(postId, postRequestDto, memberId, newImages);

        return ResponseDto.success(post);
    }

    // 게시물 삭제
    @DeleteMapping("/{postId}")
    public ResponseDto<?> deletePost(@PathVariable Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetailsImpl){

        // 로그인 확인
        if(userDetailsImpl.getMember() == null){
            return ResponseDto.fail(ErrorCode.LOGIN_REQUIRED);
        }

        // 로그인된 유저의 아이디
        Long memberId = userDetailsImpl.getMember().getMemberId();

        // 게시물 데이터 삭제
        try{
            postService.deletePost(postId, memberId);
        } catch (IllegalArgumentException e){
            return ResponseDto.fail(ErrorCode.DELETE_FAILED);
        }

        return ResponseDto.success(postId + "번 게시물이 삭제되었습니다.");
    }
}
