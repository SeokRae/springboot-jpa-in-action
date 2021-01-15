package kr.seok.querydsl.domain;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.seok.querydsl.domain.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@SpringBootTest
@Transactional
class Querydsl24불완전라이브러리들Test {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("QuerydslPredicateExecutor 에서 지원하는 Predicate를 사용하여 조회 테스트")
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

        QMember member = QMember.member;
        /*
            select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
            where
                (
                    member0_.age between ? and ?
                )
                and member0_.username=?
         */
        BooleanExpression condition =
                member.age.between(10, 40)
                        .and(
                        member.username.eq("member1"));

        Iterable<Member> result = memberRepository.findAll(condition);
        /*
            member -> Member(id=3, username=member1, age=10)
         */
        for(Member item : result) {
            System.out.println("member -> " + item);
        }
    }
}
