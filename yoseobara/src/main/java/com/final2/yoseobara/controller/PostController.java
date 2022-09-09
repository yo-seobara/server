package com.final2.yoseobara.controller;

import com.final2.yoseobara.controller.request.PostRequestDto;
import com.final2.yoseobara.controller.response.PostResponseDto;
import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.repository.PostRepository;
import com.final2.yoseobara.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")

public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;


    // 게시글 작성
    @PostMapping
    public String createPost(@RequestBody PostRequestDto postRequestDto,
                             @AuthenticationPrincipal UserDetailsImpl userDetailsImpl){
        if(userDetailsImpl.getMember() != null){
            Long memberId = userDetailsImpl.getMember().getMemberId();
            String usernamee = userDetailsImpl.getUsername();
            this.postService.createPost(postRequestDto, memberId);
            return "redirect:/api/posts";
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
    @GetMapping("/{postid}")
    public PostResponseDto getPost(@PathVariable Long postid){
        return postService.getPost(postid);
    }

    // 게시물 수정
    @PutMapping(value = "/{postid}")
    public String updatePost(Long postid,
                             @RequestPart PostRequestDto postRequestDto,
                             @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        if(userDetailsImpl.getMember() != null){
            Long memberId = userDetailsImpl.getMember().getMemberId();
            this.postService.updatePost(postid, postRequestDto,userDetailsImpl, memberId);
            return "redirect:/api/posts";
        }
        return "login";
    }

    // 게시물 삭제
    @DeleteMapping("/{postid}")
    public String deletePost(@PathVariable Long postid,@AuthenticationPrincipal UserDetailsImpl userDetails){
        try{
            postService.deletePost(postid,userDetails);
        }catch (IllegalArgumentException e){
            log.info(e.getMessage());
        }
        return "redirect:/api/posts";
    }
}
