package com.final2.yoseobara.service;



import com.final2.yoseobara.domain.Image;
import com.final2.yoseobara.domain.UserDetailsImpl;
import com.final2.yoseobara.dto.request.MapRequestDto;
import com.final2.yoseobara.dto.request.PostRequestDto;
import com.final2.yoseobara.dto.response.PostResponseDto;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.repository.*;
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
    private final ImageRepository imageRepository;

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
                    .myHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
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
                .myHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, postId))
                .imageUrls(imageRepository.findImageUrls(postId))
                .build();
    }

    // Post 범위 내 조회
    public List<PostResponseDto> getPostListByBounds(MapRequestDto mapRequestDto) {
        List<Post> posts = postRepository.findAllByBounds(mapRequestDto.getBounds().get("South_West").get("lng"),
                mapRequestDto.getBounds().get("North_East").get("lng"),
                mapRequestDto.getBounds().get("South_West").get("lat"),
                mapRequestDto.getBounds().get("North_East").get("lat"));

        List<PostResponseDto> postList = new ArrayList<>();

        // 만약 로그인한 상태라면
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long memberId = Objects.equals(principal.toString(), "anonymousUser") ? -1L : ((UserDetailsImpl) principal).getMember().getMemberId();

        for (Post post : posts) {
            PostResponseDto postResponseDto = PostResponseDto.builder()
                    .post(post)
                    .myHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
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
                            .myHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
                            .build());
            }
        }

        // 검색 조건이 있으면 검색에 쓰이는 파라미터만 키워드를 값으로 가지고 나머진 null 임
        Slice<Post> postSlice = postRepository.findAllByTitleContainingOrContentContainingOrMember_NicknameContaining(title, content, nickname, page);
        Slice<PostResponseDto> postDtoSlice = postSlice.map(
                post -> PostResponseDto.builder()
                        .post(post)
                        .myHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
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
                        .memberId(post.getMember().getMemberId())
                        .heart(post.getHeart())
                        .myHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, post.getPostId()))
                        .build()
        );
        return postDtoSlice;
    }


    // Post 생성
    public PostResponseDto createPost(PostRequestDto postRequestDto, String thumnailUrl, List<String> imageUrls, Long memberId) {
        Member memberFoundById = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidValueException(ErrorCode.USER_NOT_FOUND)); // 에러코드 수정

        //String address = mapService.getAddress(postRequestDto.getLocation().get("lat"), postRequestDto.getLocation().get("lng"));

        // 게시물 생성
        Post post = Post.builder()
                //.member(memberFoundById)
                .title(postRequestDto.getTitle())
                .content(postRequestDto.getContent())
                .address(postRequestDto.getAddress())
                .location(postRequestDto.getLocation())
                .thumbnailUrl(thumnailUrl)
                .build();
        // 멤버 정보 추가
        post.mapToMember(memberFoundById);
        // DB에 저장
        postRepository.save(post);

        // 이미지 저장
        for (String imageUrl : imageUrls) {
            Image image = Image.builder()
                    .post(post)
                    .imageOrder(imageUrls.indexOf(imageUrl))
                    .imageUrl(imageUrl)
                    .build();
            imageRepository.save(image);
        }

        return PostResponseDto.builder()
                .post(post)
                .imageUrls(imageUrls)
                .build();
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

        // 총 이미지 1개 이상 3개 이하
        int newImageCount = newImages[0].isEmpty() ? 0 : newImages.length;  // multipartfile은 null이 아니라 빈 배열이 들어오는 듯
        if (post.getImage().size() - postRequestDto.getDeleteImageOrders().size() + newImageCount > 3) {
            throw new InvalidValueException(ErrorCode.POST_IMAGE_MAX);
        } else if (post.getImage().size() - postRequestDto.getDeleteImageOrders().size() + newImageCount < 1) {
            throw new InvalidValueException(ErrorCode.POST_IMAGE_REQUIRED);
        }

        List<String> imageUrls = new ArrayList<>();

        // 삭제할 이미지는 삭제하고 나머지는 순서 새로 매김
        if (Objects.nonNull(postRequestDto.getDeleteImageOrders())) {
            List<Image> images = imageRepository.findAllByPost_PostIdOrderByImageOrder(postId);
            int order = 0;
            for (int i = 0; i < images.size(); i++) {
                if (postRequestDto.getDeleteImageOrders().contains(i)) {
                    imageRepository.deleteByImageId(images.get(i).getImageId());
                } else {
                    images.get(i).setImageOrder(order);
                    imageRepository.save(images.get(i));
                    imageUrls.add(images.get(i).getImageUrl());
                    order++;
                }
            }
        }

        // 새로운 이미지가 존재하면 저장
        if (!newImages[0].isEmpty()) {
            List<String> newImageUrls = s3Service.uploadFile(newImages);
            int order = post.getImage().size() - postRequestDto.getDeleteImageOrders().size();
            for (String newImageUrl : newImageUrls) {
                Image image = Image.builder()
                        .post(post)
                        .imageOrder(order)  // 뒤로 추가한다
                        .imageUrl(newImageUrl)
                        .build();
                imageRepository.save(image);
                imageUrls.add(newImageUrl);
                order++;
            }
        }


        // 썸네일 가진 이미지 삭제 시 새로운 썸네일 생성
        if (postRequestDto.getDeleteImageOrders().contains(0)) {
            // 모든 작업 반영된 후의 첫번째 이미지
            String targetImageUrl = imageRepository.findByPost_PostIdAndImageOrder(postId, 0).getImageUrl();
            // 새 썸네일 파일 만들기
            MultipartFile file = s3Service.convertUrlToMultipartFile(targetImageUrl);
            // 기존 썸네일 삭제
            s3Service.deleteFile(post.getThumbnailUrl());
            // 새 썸네일 업로드
            post.setThumbnailUrl(s3Service.uploadThumbnail(file, targetImageUrl.split("/")[3]));
        }

        // 게시물 내용 수정
        post.update(postRequestDto.getTitle(), postRequestDto.getContent(), postRequestDto.getAddress(), postRequestDto.getLocation());

        return PostResponseDto.builder()
                .post(post)
                .imageUrls(imageUrls)
                .myHeart(heartRepository.existsByMember_MemberIdAndPostId(memberId, postId))
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

        // 게시물에 달린 좋아요 삭제
        heartRepository.deleteAllByPostId(postFoundById.getPostId());

        // 게시물에 달린 이미지 삭제
        imageRepository.deleteAllByPost(postFoundById);

        // 게시물 이미지 삭제
        for (String imageUrl : postFoundById.getImage().stream().map(Image::getImageUrl).toList()) {
            s3Service.deleteFile(imageUrl);
        }
        
        // 썸네일 이미지 삭제
        s3Service.deleteFile(postFoundById.getThumbnailUrl());

        // 게시물 데이터 삭제
        postRepository.delete(postFoundById);
    }
    @Transactional
    public Post selectBoardDetail(Long PostId){
        Post post = postRepository.findById(PostId).get();
        post.updateView(post.getView());
        postRepository.save(post);
        return post;
    }
}
