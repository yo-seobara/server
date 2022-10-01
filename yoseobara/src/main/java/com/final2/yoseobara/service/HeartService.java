package com.final2.yoseobara.service;

import com.final2.yoseobara.domain.Heart;
import com.final2.yoseobara.domain.Member;
import com.final2.yoseobara.domain.Post;
import com.final2.yoseobara.dto.request.HeartDto;
import com.final2.yoseobara.exception.ErrorCode;
import com.final2.yoseobara.exception.InvalidValueException;
import com.final2.yoseobara.jwt.TokenProvider;
import com.final2.yoseobara.repository.HeartRepository;
import com.final2.yoseobara.repository.MemberRepository;
import com.final2.yoseobara.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static com.final2.yoseobara.exception.ErrorCode.MEMBER_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service
public class HeartService {

    private final HeartRepository heartRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider TokenProvider;
    private final PostRepository postRepository;
    private Member member;


    public void heart(HeartDto heartDto, String jwtToken) {
        validateToken(heartDto, jwtToken);


        // 이미 좋아요 된 게시물일 경우 409 에러
        if (findHeartWithMemberAndPostId(heartDto).isPresent()){
            throw new InvalidValueException(ErrorCode.ALREADY_HEARTED);
        }
        Heart heart = Heart.builder()
                .postId(heartDto.getPostId())
                .member(memberRepository.findById(heartDto.getMemberId()).get())
                .build();
        heartRepository.save(heart);

        updateHeartCount(heartDto.getPostId(), 1);

    }

    public void unHeart(HeartDto heartDto, String jwtToken) {
        validateToken(heartDto, jwtToken);

        Optional<Heart> heartOpt = findHeartWithMemberAndPostId(heartDto);

        if (heartOpt.isEmpty()) {
            throw new InvalidValueException(ErrorCode.HEART_NOT_FOUND);
        }

        heartRepository.delete(heartOpt.get());

        updateHeartCount(heartDto.getPostId(), -1);

    }

    private void validateToken(HeartDto heartDto, String jwtToken) {
        // 유효한 토큰인지 검증
        if (!TokenProvider.validateToken(jwtToken)) {
            throw new InvalidValueException(ErrorCode.INVALID_TOKEN);
        }
        // 해당 유저 존재하는지 검증
        Optional<Member> memberOpt = memberRepository.findById(heartDto.getMemberId());
        if (memberOpt.isEmpty()) {
            throw new InvalidValueException(MEMBER_NOT_FOUND);
        } else {
            member = memberOpt.get();
        }
    }


    public Optional<Heart> findHeartWithMemberAndPostId(@NotNull HeartDto heartDto) {
        return heartRepository.
                findHeartByMemberAndPostId(member, heartDto.getPostId());
    }

    public void updateHeartCount(Long postId, Integer plusOrMinus)  {

        Post post0tp = postRepository.findByPostId(postId);

        post0tp.setHeart(post0tp.getHeart() + plusOrMinus);
        postRepository.save(post0tp);
    }
}