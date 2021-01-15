package kr.seok.querydsl.domain;

import kr.seok.querydsl.domain.dto.MemberSearchCondition;
import kr.seok.querydsl.domain.dto.MemberTeamDto;
import kr.seok.querydsl.domain.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class Querydsl20사용자정의리포지토리Test {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("사용자 정의 레포지토리를 구현한 기존 레포지토리 인터페이스에서 호출 테스트")
    void testCase3() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35); // 35세 이상
        condition.setAgeLoe(40); // 40세 이하
        condition.setTeamName("teamB"); // teamB

        /*
            select
                member0_.member_id as col_0_0_,
                member0_.username as col_1_0_,
                member0_.age as col_2_0_,
                team1_.team_id as col_3_0_,
                team1_.name as col_4_0_
            from
                member member0_
            left outer join
                team team1_
                    on member0_.team_id=team1_.team_id
            where
                team1_.name=?
                and member0_.age>=?
                and member0_.age<=?
         */
        List<MemberTeamDto> result = memberRepository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }
}
