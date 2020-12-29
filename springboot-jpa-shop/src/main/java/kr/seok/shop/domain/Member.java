package kr.seok.shop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String name;

    /* 내장 타입 필드 활용 */
    @Embedded
    private Address address;

    /* 읽기 전용 매핑 필드 설정 */
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
