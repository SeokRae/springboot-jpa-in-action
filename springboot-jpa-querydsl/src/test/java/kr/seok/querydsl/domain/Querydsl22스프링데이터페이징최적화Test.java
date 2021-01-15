package kr.seok.querydsl.domain;

import kr.seok.querydsl.domain.dto.MemberSearchCondition;
import kr.seok.querydsl.domain.dto.MemberTeamDto;
import kr.seok.querydsl.domain.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class Querydsl22스프링데이터페이징최적화Test {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    /**
     * 페이지 시작이면서 컨텐츠 사이즈가 페이지 사이즈보다 작을 때 카운트 쿼리를 호출하지 않음
     */
    @Test
    @DisplayName("페이징 쿼리를 특정 조건의 경우 생략하는 최적화 페이징 쿼리")
    void testCase1() {
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

        PageRequest page = PageRequest.of(0, 4);
        /*
            select
                count(member0_.member_id) as col_0_0_
            from
                member member0_
            left outer join
                team team1_
                    on member0_.team_id=team1_.team_id
         */
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
         */
        Page<MemberTeamDto> result = memberRepository.searchPageExecuteUtil(condition, page);

        result.forEach(memberTeam -> System.out.println("memberTeam -> " + memberTeam));
        System.out.println("totalPage : " + result.getTotalPages());
        System.out.println("size : " + result.getSize());
        assertThat(result.getContent())
                .extracting("username")
                .contains("member1");
    }

}
