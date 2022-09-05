package com.final2.yoseobara.domain;

import com.final2.yoseobara.model.Timestamped;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter // get 함수를 일괄적으로 만들어줍니다.
@NoArgsConstructor // 기본 생성자를 만들어줍니다.
@Entity // DB 테이블 역할을 합니다.
public class Post extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_ID")
    private Long postid;

    @Column(length = 50, nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String username;

    @ManyToOne
    @JoinColumn(name = "member_member_id")
    private Member member;
    @Column
    private Float address;

//    @Column
//    private List<String> images;

    public void setMember(Member member) {
        this.member = member;
    }

    @Builder
    public Post(String title, String content, Float address) {
        this.title = title;
        this.content = content;
//        this.images = images;
        this.address = address;
    }

    public void update(String title, String content, Long address) {
        this.title = title;
        this.content = content;
//        this.images = images;
        this.address = Float.valueOf(address);
    }

    public void mapTomember(Member memberFoundById) {
        this.member = memberFoundById;
        memberFoundById.mapToContents(this);
    }

}


