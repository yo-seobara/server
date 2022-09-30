package com.final2.yoseobara.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Table
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Heart {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long Id;

    @Column
    @NonNull
    private Long postId;

    @ManyToOne
    @JoinColumn
    private Member member;
}