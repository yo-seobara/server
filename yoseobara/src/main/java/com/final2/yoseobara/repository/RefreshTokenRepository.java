package com.final2.yoseobara.repository;

import com.final2.yoseobara.domain.RefreshToken;
import com.final2.yoseobara.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
}
