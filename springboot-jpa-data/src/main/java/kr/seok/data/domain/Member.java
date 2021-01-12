package kr.seok.data.domain;

import kr.seok.data.domain.base.BaseEntity;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
@NamedQuery(
        name="Member.findNamedQueryByUsername",
        query="select m from Member m where m.username = :username"
)
@NamedEntityGraph(name = "Member.all", attributeNodes = @NamedAttributeNode("team"))
public class Member extends BaseEntity implements Serializable {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    /* 연관관계의 주인은 member > 외래 키 값을 변경 가능 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    /* member 생성 시: 이름 */
    public Member(String username) {
        this(username, 0);
    }

    /* member 생성 시: 이름, 나이 */
    public Member(String username, int age) {
        this(username, age, null);
    }

    /* member 생성 시: 이름, 나이, 팀 */
    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }
    /* member 팀 변경 */
    public void changeTeam(Team team) {
        this.team = team;
        team
                /* Member 데이터를 Team 쪽에 설정 */
                .getMembers().add(this);
    }
}
