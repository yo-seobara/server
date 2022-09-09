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
    @Column
    private Long postid;

    @Column(length = 50)
    private String title;

    @Column
    private String content;
    @Column
    private Float address;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberid", foreignKey = @ForeignKey(name = "FK_member_post"))
    private Member member;


//    @Column
//    private List<String> images;



    @Builder
    public Post(String title, String content,Float address) {
        this.title = title;
        this.content = content;
//        this.images = images;
        this.address = address;
    }

    public void update(String title, String content, Float address) {
        this.title = title;
        this.content = content;
//        this.images = images;
        this.address =address;
    }

    public void mapTomember(Member memberFoundById) {
        this.member = memberFoundById;
        memberFoundById.mapToContents(this);
    }

}


