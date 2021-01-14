package kr.seok.querydsl.domain;

import com.querydsl.core.QueryResults;
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
public class Querydsl3리턴타입Test {

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
    @DisplayName("리턴타입 List")
    void testCase1() {
        //List
        /*
         select
            member0_.member_id as member_i1_1_,
            member0_.age as age2_1_,
            member0_.team_id as team_id4_1_,
            member0_.username as username3_1_
        from
            member member0_
         */
        List<Member> memberList = queryFactory
                .selectFrom(member)
                .fetch();
        assertThat(memberList.size()).isEqualTo(4);
        memberList.forEach(m -> System.out.println("member : " + m));
    }

    @Test
    @DisplayName("리턴타입 단건")
    void testCase2() {
        //단 건
        Member findMember1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        assert findMember1 != null;
        assertThat(findMember1.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("조회된 데이터 중 가장 처음 데이터")
    void testCase3() {
        //처음 한 건 조회
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();
        assertThat(findMember2.getUsername()).isEqualTo("member1");
    }

    @Test
    @DisplayName("페이징에서 사용")
    void testCase4() {
        //페이징에서 사용
        /*
            // 페이징 쿼리
            select
                count(member0_.member_id) as col_0_0_
            from
                member member0_
            // 데이터 쿼리
            select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
         */
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        long total = results.getTotal();
        long limit = results.getLimit();
        long offset = results.getOffset();
        List<Member> results1 = results.getResults();

        assertThat(total).isEqualTo(4);
        assertThat(offset).isEqualTo(0);
        assertThat(limit).isEqualTo(9223372036854775807L);

        results1.forEach(m -> System.out.println("member -> " + m));
    }

    @Test
    @DisplayName("count 쿼리로 변경")
    void testCase5() {
        //count 쿼리로 변경
        /*
            select
                count(member0_.member_id) as col_0_0_
            from
                member member0_
         */
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();

        assertThat(count).isEqualTo(4);
    }
}
