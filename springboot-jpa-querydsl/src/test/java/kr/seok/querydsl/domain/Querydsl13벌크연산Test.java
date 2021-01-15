package kr.seok.querydsl.domain;

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
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class Querydsl13벌크연산Test {

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
    @DisplayName("쿼리 한 번에 대량 데이터 수정")
    void testCase1() {
        /*
            update
                member
            set
                username=?
            where
                age<?
         */

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        /* 로직상으로 벌크 연산 후 조회가 필요한 경우 */
        em.flush();
        em.clear();

        /* 이런 로직에 대한 검증은 코드로 확인할 수 없음. 꼭 눈으로 확인해야 함 */
        List<Member> fetch = queryFactory.selectFrom(member).fetch();
        /* 영속성 컨텍스트와 DB 데이터 확인 */
        fetch.forEach(member -> System.out.println("member -> " + member));

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("기존 숫자에 1 더하기")
    void testCase2() {
        /*
            update
                member
            set
                age=age+?
         */
        long count = queryFactory
                .update(member)
                .set(
                        member.age, member.age.add(1)
                )
                .execute();
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("기존 숫자에 1 곱하기")
    void testCase3() {
        /*
            update
                member
            set
                age=age*?
         */
        long count = queryFactory
                .update(member)
                .set(
                        member.age, member.age.multiply(1)
                )
                .execute();
        assertThat(count).isEqualTo(4);
    }

    @Test
    @DisplayName("대량 데이터 삭제")
    void testCase4() {
        /*
            update
                member
            set
                age=age*?
         */
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
        assertThat(count).isEqualTo(4);
    }

}
