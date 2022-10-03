package com.final2.yoseobara.service;



import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.dto.request.MapRequestDto;
import com.final2.yoseobara.dto.request.PostRequestDto;
import com.final2.yoseobara.dto.response.PostResponseDto;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.repository.CommentRepository;
import com.final2.yoseobara.repository.HeartRepository;
import com.final2.yoseobara.repository.MemberRepository;
import com.final2.yoseobara.repository.PostRepository;
import com.final2.yoseobara.shared.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RequiredArgsConstructor
@Service
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final S3Service s3Service;
    private final MapService mapService;
    private final HeartRepository heartRepository;

    // Post 리스트 조회 - responseDto의 @Builder와 연계됨.
    public List<PostResponseDto> getPostList() {
        List<Post> posts = postRepository.findAll();
        List<PostResponseDto> postList = new ArrayList<>();

        // 만약 로그인한 상태라면
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Objects.equals(principal.toString(), "anonymousUser") ? -1L : ((UserDetailsImpl) principal).getMember().getMemberId();

        for (Post post : posts) {
            PostResponseDto postResponseDto = PostResponseDto.builder()
                    .post(post)
                    .imageUrls(post.getImageUrls())
                    .nickname(post.getMember().getNickname())
                    .heart(post.getHeart())
                    .isHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
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

        // 만약 로그인한 상태라면
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Objects.equals(principal.toString(), "anonymousUser") ? -1L : ((UserDetailsImpl) principal).getMember().getMemberId();

        return PostResponseDto.builder()
                .post(post)
                //.view(post.getView())
                .heart(post.getHeart())
                .isHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, postid))
                .imageUrls(post.getImageUrls())
                .nickname(post.getMember().getNickname())
                .build();
    }

    // Post 범위 내 조회
    public List<PostResponseDto> getPostListByBounds(MapRequestDto mapRequestDto) {
        List<Post> posts = postRepository.findAllByBounds(mapRequestDto.getBounds().get("South_West").get("lng"),
                mapRequestDto.getBounds().get("North_East").get("lng"),
                mapRequestDto.getBounds().get("South_West").get("lat"),
                mapRequestDto.getBounds().get("North_East").get("lat"));

        List<PostResponseDto> postList = new ArrayList<>();

        for (Post post : posts) {
            PostResponseDto postResponseDto = PostResponseDto.builder()
                    .post(post)
                    .imageUrls(post.getImageUrls())
                    .nickname(post.getMember().getNickname())
                    .heart(post.getHeart())
                    .build();
            postList.add(postResponseDto);
        }

        return postList;
    }

    // Post 슬라이스 -> 무한스크롤은 페이지보다 슬라이스가 좋음 (카운트를 하지 않아서)
    public Slice<PostResponseDto> getPostSlice(String search, String keyword, Pageable pageable) {

        // 만약 로그인한 상태라면
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Objects.equals(principal.toString(), "anonymousUser") ? -1L : ((UserDetailsImpl) principal).getMember().getMemberId();

        // 정렬의 디폴트는 최신순
        Sort sort = pageable.getSort().and(Sort.by("createdAt").descending());
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // 검색 조건이 아니면 검색에 쓰이면 안되므로 널
        String title = null;
        String content = null;
        String nickname = null;
        if (Objects.nonNull(search)) {
            switch (search) {
                case "title":
                    title = keyword;
                    break;
                case "content":
                    content = keyword;
                    break;
                case "nickname":
                    nickname = keyword;
                    break;
                // 검색 파라미터 없으면 빈문자열임 -> 검색 안하고 전체 조회
                case "":
                    return postRepository.findAllDefault(page).map(post -> PostResponseDto.builder()
                            .post(post)
                            .imageUrls(post.getImageUrls())
                            .nickname(post.getMember().getNickname())
                            .heart(post.getHeart())
                            .isHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
                            .build());
            }
        }

        // 검색 조건이 있으면 검색에 쓰이는 파라미터만 키워드를 값으로 가지고 나머진 null 임
        Slice<Post> postSlice = postRepository.findAllByTitleContainingOrContentContainingOrMember_NicknameContaining(title, content, nickname, page);
        Slice<PostResponseDto> postDtoSlice = postSlice.map(
                post -> PostResponseDto.builder()
                        .post(post)
                        .imageUrls(post.getImageUrls())
                        .nickname(post.getMember().getNickname())
                        .heart(post.getHeart())
                        .isHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
                        .build()
        );
        return postDtoSlice;
    }


    // 닉네임으로 Post 리스트 조회 (유저페이지)
    public Page<PostResponseDto> getPostPageByNickname(String nickname, String search, String keyword, Pageable pageable) {

        // nickname으로 멤버 존재하는지 확인
        if (memberRepository.findByNickname(nickname).isEmpty()) {
            throw new InvalidValueException(ErrorCode.USER_NOT_FOUND);
        }

        // 만약 로그인한 상태라면
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Objects.equals(principal.toString(), "anonymousUser") ? -1L : ((UserDetailsImpl) principal).getMember().getMemberId();

        // 정렬의 디폴트는 최신순
        Sort sort = pageable.getSort().and(Sort.by("createdAt").descending());
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // jpa문을 and로 바꿔봄
        String title = "";
        String content = "";
        if (Objects.nonNull(search)) {
            switch (search) {
                case "title" -> title = keyword;
                case "content" -> content = keyword;
            }
        }

        Page<Post> postSlice = postRepository.findAllByMember_NicknameAndTitleContainingAndContentContaining(nickname, title, content, page);
        Page<PostResponseDto> postDtoSlice = postSlice.map(
                post -> PostResponseDto.builder()
                        .post(post)
                        .imageUrls(post.getImageUrls())
                        .nickname(post.getMember().getNickname())
                        .heart(post.getHeart())
                        .isHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
                        .build()
        );
        return postDtoSlice;
    }


    // Post 생성
    public PostResponseDto createPost(PostRequestDto postRequestDto, List<String> imageUrls, Long memberId) {
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다.")); // 에러코드 수정

        String address = mapService.getAddress(postRequestDto.getLocation().get("lat"), postRequestDto.getLocation().get("lng"));


        // 게시물 생성
        Post post = Post.builder()
                //.member(memberFoundById)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .address(address)
                .location(postRequestDto.getLocation())
                .imageUrls(imageUrls)
                .build();
        // 멤버 정보 추가
        post.mapToMember(memberFoundById);
        // DB에 저장
        postRepository.save(post);
        
        PostResponseDto postResponseDto = PostResponseDto.builder()
                .post(post)
                .imageUrls(post.getImageUrls())
                .nickname(memberFoundById.getNickname())
                .heart(post.getHeart())
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

        // 로그인된 유저와 게시글 작성자가 같지 않거나 관리자가 아니면 수정 불가
        if (!(Objects.equals(memberFoundById.getMemberId(), post.getMember().getMemberId()) || memberFoundById.getAuthority() == Authority.ROLE_ADMIN)) {
            throw new InvalidValueException(ErrorCode.POST_UNAUTHORIZED);
        }

        // 이미지가 존재하면 S3와 DB 업데이트 -> 이미지가 없으면 기존 이미지 유지이긴 한데 1개 이상으로 설정해서 일단 그냥 덮어씌우는 걸로
        // 기존 이미지인지 확인 기능 찾아보기 (파일의 URL 혹은 메타데이터 등?)
        if (newImages != null) {
            // 이미지 1개 이상 3개 이하
            if(newImages.length > 3) {
                throw new InvalidValueException(ErrorCode.POST_IMAGE_MAX);
            } else if (newImages.length < 1) {
                throw new InvalidValueException(ErrorCode.POST_IMAGE_REQUIRED);
            }

            List<String> imageUrls = s3Service.updateFile(post.getImageUrls(), post.getThumbnailUrl(), newImages);
            // 서브리스트 문제 때문에 임시로 만든 변수
            List<String> temp = new ArrayList<>(imageUrls.subList(1, imageUrls.size()));
            post.setImageUrls(temp);
            post.setThumbnailUrl(imageUrls.get(0));
        }

        String address = mapService.getAddress(postRequestDto.getLocation().get("lat"), postRequestDto.getLocation().get("lng"));

        // 나머지 데이터 업데이트
        post.update(postRequestDto.getTitle(), postRequestDto.getContent(), address, postRequestDto.getLocation());
        // 멤버 정보 추가
        //post.mapToMember(memberFoundById);
        // DB에 저장
        postRepository.save(post);

        return PostResponseDto.builder()
                .post(post)
                .imageUrls(post.getImageUrls())
                .nickname(memberFoundById.getNickname())
                .heart(post.getHeart())
                .build();
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

        // 로그인된 유저와 게시글 작성자가 같지 않거나 관리자가 아니면 삭제 불가
        if (!(Objects.equals(memberFoundById.getMemberId(), postFoundById.getMember().getMemberId()) || memberFoundById.getAuthority() == Authority.ROLE_ADMIN)) {
            throw new InvalidValueException(ErrorCode.POST_UNAUTHORIZED);
        }

        // 게시물에 달린 댓글 삭제
        commentRepository.deleteAllByPost(postFoundById);

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
