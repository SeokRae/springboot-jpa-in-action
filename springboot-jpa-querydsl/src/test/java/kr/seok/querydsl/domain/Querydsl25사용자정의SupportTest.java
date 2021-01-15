package kr.seok.querydsl.domain;

import kr.seok.querydsl.domain.dto.MemberSearchCondition;
import kr.seok.querydsl.domain.dto.MemberTeamDto;
import kr.seok.querydsl.domain.repository.MemberTestRepository;
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
class Querydsl25사용자정의SupportTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberTestRepository memberTestRepository;

    @Test
    @DisplayName("Querydsl4RepositorySupport 기반 테스트 searchPageByApplyPage()")
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

        /*
            select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
            left outer join
                team team1_
                    on member0_.team_id=team1_.team_id limit ?
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

        Sort sort = Sort.by("team.id").descending()
                .and(Sort.by("id").ascending());

        PageRequest page = PageRequest.of(0, 4, sort);
        Page<MemberTeamDto> members = memberTestRepository.searchPageByApplyPageToDto(condition, page);

        members.forEach(member -> {
            System.out.println("member -> " + member);
        });
    }
    @Test
    @DisplayName("Querydsl4RepositorySupport 기반 테스트 applyPaginationToDto()")
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

        Sort sort = Sort.by("team.id").descending()
                .and(Sort.by("id").ascending());

        PageRequest page = PageRequest.of(0, 4, sort);
        Page<MemberTeamDto> members = memberTestRepository.applyPaginationToDto(condition, page);

        members.forEach(member -> {
            System.out.println("member -> " + member);
        });
    }
    @Test
    @DisplayName("Querydsl4RepositorySupport 기반 테스트 applyPagination2ToDto()")
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

        Sort sort = Sort.by("team.id").descending()
                .and(Sort.by("id").ascending());

        PageRequest page = PageRequest.of(0, 4, sort);
        Page<MemberTeamDto> members = memberTestRepository.applyPagination2ToDto(condition, page);

        members.forEach(member -> {
            System.out.println("member -> " + member);
        });
    }
}
