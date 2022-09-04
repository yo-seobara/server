package com.final2.yoseobara.controller;

import com.final2.yoseobara.controller.request.PostRequestDto;
import com.final2.yoseobara.controller.response.PostResponseDto;
import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.repository.PostRepository;
import com.final2.yoseobara.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")

public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;


    // 게시글 작성
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public String createPost(@RequestPart PostRequestDto postRequestDto,
                             @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                             @RequestPart(required = false) MultipartFile file){
        if(userDetailsImpl.getMember() != null){
            Long memberId = userDetailsImpl.getMember().getMemberId();
            String usernamee = userDetailsImpl.getUsername();
            this.postService.createPost(postRequestDto, memberId);
            return "redirect:/posts";
        }
        return "login";
    }

    // 게시글 전체 조회
    @GetMapping
    public List<PostResponseDto> getPostList(){
        return postService.getPostList();
    }

    // 게시물 상세 조회
    @ResponseBody
    @GetMapping("/{post_id}")
    public PostResponseDto getPost(@PathVariable Long post_id){
        return postService.getPost(post_id);
    }

    // 게시물 수정
    @PutMapping(value = "/{post_id}", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE}, headers = ("content-type=multipart/*"))
    public String updatePost(@PathVariable(name = "post_id") Long post_id,
                             @RequestPart PostRequestDto postRequestDto,
                             @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
                             @RequestPart(required = false) MultipartFile file) {
        if(userDetailsImpl.getMember() != null){
            Long memberId = userDetailsImpl.getMember().getMemberId();
            this.postService.updatePost(post_id, postRequestDto, userDetailsImpl, file, memberId);
            return "redirect:/posts";
        }
        return "login";
    }

    // 게시물 삭제
    @DeleteMapping("/{post_id}")
    public String deletePost(@PathVariable Long post_id,@AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            postService.deletePost(post_id,userDetails);
        }catch (IllegalArgumentException e){
            log.info(e.getMessage());
        }
        return "redirect:/posts";
    }
}
