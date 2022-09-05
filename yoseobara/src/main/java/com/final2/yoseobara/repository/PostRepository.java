package com.final2.yoseobara.repository;


import com.final2.yoseobara.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAll();
    <T> List<T> findAllBy(Class<T> type);

    Optional<Post> findById(Long postId);
}
