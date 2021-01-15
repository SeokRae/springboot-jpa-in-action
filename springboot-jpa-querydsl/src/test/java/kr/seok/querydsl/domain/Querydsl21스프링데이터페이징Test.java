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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class Querydsl21스프링데이터페이징Test {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("단순 페이징")
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

        PageRequest page = PageRequest.of(0, 3);
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
                    on member0_.team_id=team1_.team_id limit ?
         */
        Page<MemberTeamDto> result = memberRepository.searchPageSimple(condition, page);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting("username")
                .contains("member1");
    }
    @Test
    @DisplayName("데이터와 카운트를 분리해서 조회하는 페이징")
    void testCase2() {
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

        PageRequest page = PageRequest.of(0, 3);
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
        /*
            select
                count(member0_.member_id) as col_0_0_
            from
                member member0_
            left outer join
                team team1_
                    on member0_.team_id=team1_.team_id
         */
        Page<MemberTeamDto> result = memberRepository.searchPageComplex(condition, page);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent())
                .extracting("username")
                .contains("member1");
    }

}
