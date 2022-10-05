package com.final2.yoseobara.repository;

import com.final2.yoseobara.domain.Comment;
import com.final2.yoseobara.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByPostPostId(Long postId, Pageable page);

    void deleteAllByPost(Post postFoundById);

    Page<Comment> findAllByMember_MemberId(Long memberId, Pageable page);

    @Query("select p from Post p")
    public Slice<Post> findAllDefault(Pageable pageable);
}
