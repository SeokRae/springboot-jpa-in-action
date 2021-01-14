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
public class Querydsl2검색Test {

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
    @DisplayName("select from where and 조건 처리")
    void testCase1() {
        /*
             select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
            where
                member0_.username=?
                and member0_.age=?
         */
        Member member1 = queryFactory.selectFrom(member)
                /* .and()로 체이닝을 할 수도 있지만 .and 없이 파라미터 여러개로 날렸을 때도 and 로 처리 */
                .where(
                        member.username.eq("member1")
                                .and(member.age.eq(10))
                )
                .fetchOne();
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("select from where() 메서드에 동적 파라미터로 넣으면 알아서 AND 처리")
    void testCase2() {
        /*
             select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
            where
                member0_.username=?
                and member0_.age=?
         */
        Member member1 = queryFactory.selectFrom(member)
                /* .and()로 체이닝을 할 수도 있지만 .and 없이 파라미터 여러개로 날렸을 때도 and 로 처리 */
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("sql condition 종류")
    void testCase3() {
        Member member1 = queryFactory.selectFrom(member)
                .where(
                        member.username.eq("member1") // username = 'member1'
                ).fetchOne();

        assertThat(member1.getUsername()).isEqualTo("member1");

        List<Member> member2 = queryFactory
                .selectFrom(member)
                .where(
                        member.username.ne("member1") //username != 'member1'
                ).fetch();
        assertThat(member2.size()).isEqualTo(3);

        List<Member> member3 = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1").not() // username != 'member1'
                ).fetch();
        assertThat(member2.size()).isEqualTo(member3.size());

        List<Member> member4 = queryFactory
                .selectFrom(member)
                .where(
                        member.username.isNotNull() //이름이 is not null
                ).fetch();

        assertThat(member4.size()).isEqualTo(4);

        List<Member> member5 = queryFactory
                .selectFrom(member)
                .where(
                        member.age.in(10, 20) // age in (10,20)
                ).fetch();

        assertThat(member5.size()).isEqualTo(2);

        List<Member> member6 = queryFactory
                .selectFrom(member)
                .where(
                        member.age.notIn(10, 20) // age not in (10, 20)
                ).fetch();
        assertThat(member6.size()).isEqualTo(2);

        List<Member> member7 = queryFactory
                .selectFrom(member)
                .where(
                        member.age.between(10, 30) //between 10, 30
                ).fetch();
        assertThat(member7.size()).isEqualTo(3);

        List<Member> member8 = queryFactory
                .selectFrom(member)
                .where(
                        member.age.goe(30) // age >= 30
                ).fetch();
        assertThat(member8.size()).isEqualTo(2);

        List<Member> member9 = queryFactory
                .selectFrom(member)
                .where(
                        member.age.gt(30) // age > 30
                ).fetch();
        assertThat(member9.size()).isEqualTo(1);

        List<Member> member10 = queryFactory
                .selectFrom(member)
                .where(
                        member.age.loe(30) // age <= 30
                ).fetch();
        assertThat(member10.size()).isEqualTo(3);

        List<Member> member11 = queryFactory
                .selectFrom(member)
                .where(
                        member.age.lt(30) // age < 30
                ).fetch();
        assertThat(member11.size()).isEqualTo(2);

        List<Member> member12 = queryFactory
                .selectFrom(member)
                .where(
                        member.username.like("member%") //like 검색
                ).fetch();
        assertThat(member12.size()).isEqualTo(4);

        List<Member> member13 = queryFactory
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.contains("member") // like ‘%member%’ 검색
                ).fetch();
        assertThat(member13.size()).isEqualTo(4);

        List<Member> member14 = queryFactory
                .selectFrom(QMember.member)
                .where(
                        QMember.member.username.startsWith("member") //like ‘member%’ 검색
                ).fetch();

        assertThat(member14.size()).isEqualTo(4);
    }
}
