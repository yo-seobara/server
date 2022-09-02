package com.final2.yoseobara.service;



import com.final2.yoseobara.dto.PostRequestDto;
import com.final2.yoseobara.dto.PostResponseDto;
import com.final2.yoseobara.model.Post;
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
    private final S3Service s3Service;

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
    public PostResponseDto getPost(Long post_id) {
        Post post = postRepository.findById(post_id).orElseThrow(
                () -> new IllegalArgumentException("Couldn't find the post")
        );
        return PostResponseDto.builder()
                .post(post)
                .build();
    }

    // Post 생성
    @Transactional
    public Post createPost(PostRequestDto requestDto, UserDetailsImpl userDetails, MultipartFile file, Long memberId) {
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        String imageUrl = "";
        if (memberRepository.findById(userDetails.getmember().getId()).isPresent()) {
            if (!file.isEmpty()) {
                imageUrl = s3Service.uploadFile(file);
            }
        }
        Post post = Post.builder()
                .title(requestDto.getTitle())
                .contents(requestDto.getContents())
                .address(requestDto.getAddress()).imageUrl(imageUrl).build();
        post.mapTomember(memberFoundById);
        postRepository.save(post);
        return post;
    }

    // Post 수정
    @Transactional
    public PostResponseDto updatePost(Long post_id,
                                      PostRequestDto postRequestDto,
                                      UserDetailsImpl userDetails,
                                      MultipartFile file,
                                      Long memberId) {
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
        Post postFoundById = postRepository.findById(post_id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        String imageUrl = "";
        if (memberRepository.findById(userDetails.getmember().getId()).isPresent()) {
            if (!file.isEmpty()) {
                imageUrl = s3Service.uploadFile(file);
            }
        }
        postFoundById.update(postRequestDto.getTitle(), postRequestDto.getContents(), postRequestDto.getAddress(), postRequestDto.getPrice(), imageUrl);
        postFoundById.mapToMember(memberFoundById);
        postRepository.save(postFoundById);
        return PostResponseDto.builder().post(postFoundById).build();
    }

    // Post 삭제
    @Transactional
    public void deletePost(Long post_id, UserDetailsImpl userDetails) throws IllegalArgumentException {
        Post post = postRepository.findById(post_id).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );
        String loginMember = userDetails.getMember().getMembername();
        String author = post.getMember().getMembername();
        if (author.equals(loginMember)) {
            postRepository.deleteById(post_id);
        } else {
            throw new IllegalArgumentException("해당 게시글에 대한 삭제 권한이 없습니다.");
        }
    }
}
