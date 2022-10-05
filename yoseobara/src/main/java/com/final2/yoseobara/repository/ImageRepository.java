package com.final2.yoseobara.repository;

import com.final2.yoseobara.domain.Image;
import com.final2.yoseobara.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("select i.imageUrl from Image i where i.post.postId = :postId order by i.imageOrder")
    List<String> findImageUrls(Long postId);

    Image findByPost_PostIdAndImageOrder(Long postId, Integer deleteImageOrder);

    List<Image> findAllByPost_PostIdOrderByImageOrder(Long postId);

    @Transactional
    void deleteByImageId(Long imageId);

    void deleteAllByPost(Post postFoundById);
}
