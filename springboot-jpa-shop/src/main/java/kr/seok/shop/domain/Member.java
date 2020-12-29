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

    /* 실무에서는 검증 로직이 있어도 멀티 쓰레드 상황을 고려해서 회원 테이블의 회원명 컬럼에 유니크 제 약 조건을 추가하는 것이 안전하다. */
    @Column(unique = true)
    private String name;

    /* 내장 타입 필드 활용 */
    @Embedded
    private Address address;

    /* 읽기 전용 매핑 필드 설정 */
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
