package kr.seok.querydsl.domain;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static kr.seok.querydsl.domain.QMember.member;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class Querydsl14SQLFunctionTest {

    @PersistenceContext
    private EntityManager em;

    /* Multi Thread 환경에서 사용할 수 있도록 설계 되어 있음*/
    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    @DisplayName("replace 함수 사용")
    void testCase1() {
        /*
            select
                function('replace', member1.username, ?1, ?2)
            from
                Member member1
        */
        /*
            select
                replace(member0_.username, ?, ?) as col_0_0_
            from
                member member0_ limit ?
         */
        String result = queryFactory
                .select(
                        Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})",
                        member.username, "member", "M")
                )
                .from(member)
                .fetchFirst();
        System.out.println(result);
    }
    @Test
    @DisplayName("lower 함수 사용 -> function('lower', member1.username)")
    void testCase2() {
        /*
            select
                member1.username
            from
                Member member1
            where
                member1.username = function('lower', member1.username)
        */
        /*
            select
                member0_.username as col_0_0_
            from
                member member0_
            where
                member0_.username=lower(member0_.username) limit ?
         */
        String result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(
                        Expressions.stringTemplate("function('lower', {0})", member.username)
                ))
                .fetchFirst();
        System.out.println(result);
    }

    @Test
    @DisplayName("querydsl 용 lower 함수 사용 -> lower(member1.username)")
    void testCase3() {
        /*
            select
                member1.username
            from
                Member member1
            where
                member1.username = lower(member1.username)
        */
        /*
            select
                member0_.username as col_0_0_
            from
                member member0_
            where
                member0_.username=lower(member0_.username) limit ?
         */
        String result = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(member.username.lower()))
                .fetchFirst();
        System.out.println(result);
    }
}
