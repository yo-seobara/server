package com.final2.yoseobara.repository;

import com.final2.yoseobara.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // post 엔티티를 맵핑한다면
    // List<Comment> findAllByPostPostId(Long postId);

    List<Comment> findAllByPostId(Long postId);
}
