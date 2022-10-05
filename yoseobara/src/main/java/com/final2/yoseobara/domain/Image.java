package com.final2.yoseobara.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonIgnore
    private Post post;

    private Integer imageOrder;

    private String imageUrl;

    @Builder
    public Image(Post post, Integer imageOrder, String imageUrl) {
        this.post = post;
        this.imageOrder = imageOrder;
        this.imageUrl = imageUrl;
    }
}