//package com.final2.yoseobara.controller;
//
//import com.final2.yoseobara.domain.Member;
//import com.final2.yoseobara.domain.UserDetailsImpl;
//import com.final2.yoseobara.repository.MemberRepository;
//import com.final2.yoseobara.repository.PostRepository;
//import com.final2.yoseobara.service.S3Service;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestPart;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@Transactional
//@RequiredArgsConstructor
//@RestController
//public class S3Controller {
//    private final S3Service s3Service;
//    private final PostRepository postRepository;
//    private final MemberRepository memberRepository;
//
//    // 마이페이지에서 유저 정보 수정 시 유저의 사진 업로드 또는 수정 가능.
//    @PostMapping("/users/{userId}/image")
//    public String S3UserImageUpload(@RequestPart MultipartFile file,
//                                    @PathVariable(name = "userId") Long Id,
//                                    @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) throws IOException {
//        String username = userDetailsImpl.getUsername();
//        Member memberFound = memberRepository.findById(Id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 유저는 존재하지 않습니다."));
//
//        if(userDetailsImpl.getUsername().equals(Id)){
//            String memberImageURL = this.s3Service.S3UserImageUpload(file, Id, username).getImageUrl();
//            memberFound.mapTomember(memberImageURL);
//            return "redirect:/users/mypage";
//        }
//        return "login";
//    }
//}