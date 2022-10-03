package com.final2.yoseobara.repository;

import com.final2.yoseobara.domain.Heart;
import com.final2.yoseobara.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeartRepository extends JpaRepository<Heart, Long> {

    Optional<Heart> findHeartByMemberAndPostId(Member member, Long postId);

    Boolean existsByMember_MemberIdAndPostId(Long memberId, Long postId);
}