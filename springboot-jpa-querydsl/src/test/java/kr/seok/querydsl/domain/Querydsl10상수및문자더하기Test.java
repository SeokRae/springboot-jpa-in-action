package kr.seok.querydsl.domain;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static kr.seok.querydsl.domain.QMember.member;

@SpringBootTest
@Transactional
public class Querydsl10상수및문자더하기Test {

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
    @DisplayName("추가 컬럼으로 상수 출력하기")
    void testCase1() {

        /*
            select
                member0_.username as col_0_0_
            from
                member member0_ limit ?
         */
        Tuple result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetchFirst();
        /* [member1, A] */
        System.out.println(result);
    }

    @Test
    @DisplayName("concat으로 문자 더하기")
    void testCase2() {

        /*
            select
                concat(concat(member1.username, ?1), str(member1.age))
            from
                Member member1
            where
                member1.username = ?2
         */
        /*
        select
            ((member0_.username||?)||cast(member0_.age as char)) as col_0_0_
        from
            member member0_
        where
            member0_.username=?
         */
        String result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("문자 더하기 : " + result);
    }
}
