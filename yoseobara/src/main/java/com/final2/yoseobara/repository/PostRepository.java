package com.final2.yoseobara.repository;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRepository {

    private Long id;
    private String title;
    private String content;
    private String writer;
    private Long address;
}
