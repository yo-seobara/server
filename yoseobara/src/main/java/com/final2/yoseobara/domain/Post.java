package com.final2.yoseobara.domain;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Getter // get 함수를 일괄적으로 만들어줍니다.
@NoArgsConstructor // 기본 생성자를 만들어줍니다.
@Entity // DB 테이블 역할을 합니다.
@TypeDef(name = "json", typeClass = JsonType.class)
public class Post extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @Column(length = 50, nullable = false) // 길이 제한 둘건지?
    private String title;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @Column
    private String address; // 주소

    @Column(columnDefinition = "json") // 원하는 것 -> { Lat: 좌표값, lng: 좌표값 }
    @Type(type = "json") // 괜찮은 건지 모르겠음. 작동되는 것 보고 수정할 듯
    private HashMap<String,Float> location;

    @Column
    @ElementCollection(targetClass = String.class) // 나중에 썸네일만 받고, 이미지들은 따로 엔티티를 만들어 맵핑하여 관리하는 게 좋겠다.
    private List<String> imageUrls;

    public void setMember(Member member) {
        this.member = member;
    }

    @Builder
    public Post(Member member, String title, String content,String address, HashMap<String,Float> location, List<String> imageUrls) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.address = address;
        this.location = new LinkedHashMap<>();
        this.location.put("lat",location.get("lat"));
        this.location.put("lng",location.get("lng"));
        this.imageUrls = imageUrls;
    }

    public void update(Member member, String title, String content, String address, HashMap<String,Float> location, List<String> imageUrls) {
        this.member = member;
        this.title = title;
        this.content = content;
        this.address = address;
        this.location.replace("lat",location.get("lat"));
        this.location.replace("lng",location.get("lng"));
        this.imageUrls = imageUrls;
    }

    public void mapToMember(Member memberFoundById) {
        this.member = memberFoundById;
        memberFoundById.mapToContents(this);
    }

}


