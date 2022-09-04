package com.final2.yoseobara.service;



import com.final2.yoseobara.controller.request.PostRequestDto;
import com.final2.yoseobara.controller.response.PostResponseDto;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.repository.MemberRepository;
import com.final2.yoseobara.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
//    private final S3Service s3Service;

    // Post 리스트 조회 - responseDto의 @Builder와 연계됨.
    public List<PostResponseDto> getPostList() {
        List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
        List<PostResponseDto> postList = new ArrayList<>();

        for (Post post : posts) {
            PostResponseDto postResponseDto = PostResponseDto.builder()
                    .post(post)
                    .build();
            postList.add(postResponseDto);
        }

        return postList;
    }

    // Post 상세 조회
    public PostResponseDto getPost(Long postid) {
        Post post = postRepository.findById(postid).orElseThrow(
                () -> new IllegalArgumentException("Couldn't find the post")
        );
        return PostResponseDto.builder()
                .post(post)
                .build();
    }

    // Post 생성
    @Transactional
    public Post createPost(PostRequestDto requestDto,
                           Long memberId) {
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        Post post = Post.builder()
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .address(Float.valueOf(requestDto.getAddress())).build();
        postRepository.save(post);
        return post;
    }

    // Post 수정
    @Transactional
    public PostResponseDto updatePost(Long postid,
                                      PostRequestDto postRequestDto,
                                      UserDetailsImpl userDetailsImpl, MultipartFile file, Long memberId) {
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        Post postFoundById = postRepository.findById(postid)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        postFoundById.update(postRequestDto.getTitle(), postRequestDto.getContent(), postRequestDto.getAddress());
        postFoundById.mapTomember(memberFoundById);
        postRepository.save(postFoundById);
        return PostResponseDto.builder().post(postFoundById).build();
    }

    // Post 삭제
    @Transactional
    public void deletePost(Long postid, UserDetailsImpl userDetails) throws IllegalArgumentException {
        Post post = postRepository.findById(postid).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        String loginMember = userDetails.getMember().getUsername();
        String author = post.getUsername();
        if (author.equals(loginMember)) {
            postRepository.deleteById(postid);
        } else {
            throw new IllegalArgumentException("해당 게시글에 대한 삭제 권한이 없습니다.");
        }
    }
}
