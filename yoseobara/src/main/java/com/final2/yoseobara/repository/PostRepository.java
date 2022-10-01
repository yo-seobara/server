package com.final2.yoseobara.repository;


import com.final2.yoseobara.domain.Heart;
import com.final2.yoseobara.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAll();
    <T> List<T> findAllBy(Class<T> type);

    Optional<Post> findById(Long postId);

    Slice<Post> findAllByTitleContainingOrContentContainingOrMember_NicknameContaining(String title, String content, String nickname, Pageable pageable);

    @Query("select p from Post p")
    public Slice<Post> findAllDefault(Pageable pageable);

    // 범위 내 location 조회
    @Query("select p from Post p where p.lng between :swLng and :neLng and p.lat between :swLat and :neLat")
    public List<Post> findAllByBounds(@Param("swLng") Double swLng, @Param("neLng") Double neLng, @Param("swLat") Double swLat, @Param("neLat") Double neLat);

    Post findByPostId(Long postId);

    @Modifying
    @Query("update Post p set p.view = p.view + 1 where p.postId = :postId")
    int updateView(Long PostId);
}
