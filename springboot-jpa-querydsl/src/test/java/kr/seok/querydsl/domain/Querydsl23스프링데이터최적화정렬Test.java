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
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class Querydsl23스프링데이터최적화정렬Test {

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

        /* 정렬 테스트 team desc, member asc */
        Sort sort = Sort.by("team.id").descending().and(Sort.by("id").ascending());
        PageRequest page = PageRequest.of(0, 10, sort);
        /*
            select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
            order by
                member0_.team_id desc,
                member0_.member_id asc
         */
        Page<MemberTeamDto> result = memberRepository.searchPageSort(condition, page);

        result.forEach(memberTeam -> System.out.println("memberTeam -> " + memberTeam));
    }

}
