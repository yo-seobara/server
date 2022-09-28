package com.final2.yoseobara.repository;

import com.final2.yoseobara.domain.Comment;
import com.final2.yoseobara.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPostPostId(Long postId);

    void deleteAllByPost(Post postFoundById);
}
