package com.final2.yoseobara.service;



import com.final2.yoseobara.dto.request.PostRequestDto;
import com.final2.yoseobara.dto.response.PostResponseDto;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.dto.response.ResponseDto;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.repository.MemberRepository;
import com.final2.yoseobara.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;

    // Post 리스트 조회 - responseDto의 @Builder와 연계됨.
    public List<PostResponseDto> getPostList() {
        List<Post> posts = postRepository.findAll();
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
                //.view(post.getView())
                //.heart(post.getHeart())
                .nickname(post.getMember().getNickname())
                .build();
    }

    // Post 생성
    public PostResponseDto createPost(PostRequestDto requestDto, List<String> imageUrls, Long memberId) {
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다.")); // 에러코드 수정

        // 게시물 생성
        Post post = Post.builder()
                //.member(memberFoundById)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .address(requestDto.getAddress())
                .location(requestDto.getLocation())
                .imageUrls(imageUrls)
                .build();
        // 멤버 정보 추가
        post.mapToMember(memberFoundById);
        // DB에 저장
        postRepository.save(post);
        
        PostResponseDto postResponseDto = PostResponseDto.builder()
                .post(post)
                .view(0L)
                .heart(0L)
                .nickname(memberFoundById.getNickname())
                .build();
        return postResponseDto;
    }

    // Post 수정 -> 이미지 수정 어떤 방식으로 할까
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto postRequestDto, Long memberId, MultipartFile[] newImages) {

        // 로그인된 유저
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.USER_NOT_FOUND));

        // 타겟 게시물 학인
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.POST_NOT_FOUND));

        // 로그인된 유저와 게시글 작성자가 같은지 확인
        if (!Objects.equals(memberFoundById.getMemberId(), post.getMember().getMemberId())) {
            throw new InvalidValueException(ErrorCode.POST_UNAUTHORIZED);
        }

        // 이미지가 존재하면 S3와 DB 업데이트
        if (newImages != null) {
            // 새로운 이미지 3개 이하
            if(newImages.length > 3) {
                throw new InvalidValueException(ErrorCode.POST_IMAGE_MAX);
            }

            List<String> imageUrls = s3Service.updateFile(post.getImageUrls(), post.getThumbnailUrl(), newImages);
            // 서브리스트 문제 때문에 임시로 만든 변수
            List<String> temp = new ArrayList<>(imageUrls.subList(1, imageUrls.size()));
            post.setImageUrls(temp);
            post.setThumbnailUrl(imageUrls.get(0));
        }

        // 나머지 데이터 업데이트
        post.update(postRequestDto.getTitle(), postRequestDto.getContent(), postRequestDto.getAddress(),
                postRequestDto.getLocation());
        // 멤버 정보 추가
        post.mapToMember(memberFoundById);
        // DB에 저장
        postRepository.save(post);

        return PostResponseDto.builder().post(post).build();
    }

    // Post 삭제
    @Transactional
    public void deletePost(Long postId, Long memberId) throws IllegalArgumentException {

        // 로그인된 유저 객체
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.USER_NOT_FOUND));
        // 타겟 게시물
        Post postFoundById = postRepository.findById(postId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.POST_NOT_FOUND));

        // 로그인된 유저와 게시글 작성자가 같은지 확인
        if (!Objects.equals(memberFoundById.getMemberId(), postFoundById.getMember().getMemberId())) {
            throw new InvalidValueException(ErrorCode.POST_UNAUTHORIZED);
        }

        // 게시물 이미지 삭제
        for (String imageUrl : postFoundById.getImageUrls()) {
            s3Service.deleteFile(imageUrl);
        }
        
        // 썸네일 이미지 삭제
        s3Service.deleteFile(postFoundById.getThumbnailUrl());

        // 게시물 데이터 삭제
        postRepository.delete(postFoundById);
    }
}
