package kr.seok.querydsl.domain;

import kr.seok.querydsl.domain.dto.MemberSearchCondition;
import kr.seok.querydsl.domain.dto.MemberTeamDto;
import kr.seok.querydsl.domain.repository.MemberQuerydslRepository;
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
class Querydsl17QuerydslOptimizeTest {

    @Autowired
    private EntityManager em;
    @Autowired
    private MemberQuerydslRepository memberQuerydslRepository;

    @Test
    public void searchTest() {
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
                member1.id as memberId,
                member1.username,
                member1.age,
                team.id as teamId,
                team.name as teamName
            from
                Member member1
            left join
                member1.team as team
            where
                team.name = ?1
                and member1.age >= ?2
                and member1.age <= ?3
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
            where
                team1_.name=?
                and member0_.age>=?
                and member0_.age<=?
         */
        List<MemberTeamDto> result =
                memberQuerydslRepository.searchByBuilder(condition);

        result.forEach(memberTeam -> System.out.println("memberTeam -> " + memberTeam));
        assertThat(result).extracting("username").containsExactly("member4");
    }
    @Test
    public void search2Test() {
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
//        condition.setAgeGoe(35); // 35세 이상
//        condition.setAgeLoe(40); // 40세 이하
        condition.setTeamName("teamB"); // teamB

        /*
            select
                member1.id as memberId,
                member1.username,
                member1.age,
                team.id as teamId,
                team.name as teamName
            from
                Member member1
            left join
                member1.team as team
            where
                team.name = ?1
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
            where
                team1_.name=?
         */
        List<MemberTeamDto> result =
                memberQuerydslRepository.searchByBuilder(condition);

        result.forEach(memberTeam -> System.out.println("memberTeam -> " + memberTeam));
        assertThat(result).extracting("username").containsExactly("member3", "member4");
    }

    @Test
    @DisplayName("동적쿼리 where절 파라미터를 사용 테스트")
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

        List<MemberTeamDto> result =
                memberQuerydslRepository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    @DisplayName("동적쿼리 where절 queryFactory 분리 메서드 호출")
    void testCase4() {
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

        List<Member> result =
                memberQuerydslRepository.searchFindMember(condition);

        assertThat(result).extracting("username").containsExactly("member4");
    }
}
