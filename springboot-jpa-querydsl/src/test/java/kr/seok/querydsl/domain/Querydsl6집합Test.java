package kr.seok.querydsl.domain;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
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
import static kr.seok.querydsl.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class Querydsl6집합Test {

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

    /**
     * JPQL
     * select
     * COUNT(m), //회원수
     * SUM(m.age), //나이 합
     * AVG(m.age), //평균 나이
     * MAX(m.age), //최대 나이
     * MIN(m.age) //최소 나이 * from Member m
     */
    @Test
    public void aggregation() {
        /*
        select
            count(member0_.member_id) as col_0_0_,
            sum(member0_.age) as col_1_0_,
            avg(cast(member0_.age as double)) as col_2_0_,
            max(member0_.age) as col_3_0_,
            min(member0_.age) as col_4_0_
        from
            member member0_
         */
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     */
    @Test
    public void group() {

        /*
            select
                team1_.name as col_0_0_,
                avg(cast(member0_.age as double)) as col_1_0_
            from
                member member0_
            inner join
                team team1_
                    on member0_.team_id=team1_.team_id
            group by
                team1_.name
         */
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                /* join 한번 훑어보기 */
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    @DisplayName("group having 예시가 딱히 떠오르지 않음")
    void groupingHaving() {
        /*
        select
            team1_.name as col_0_0_,
            avg(cast(member0_.age as double)) as col_1_0_
        from
            member member0_
        inner join
            team team1_
                on member0_.team_id=team1_.team_id
        group by
            member0_.age
        having
            member0_.age>?
         */
        /*
        select
            team.name,
        from member
        join team
        on member.team.id = team.id
        group by team.name
         */
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg(),
                        member.count()
                )
                .from(member)
                /* join 한번 훑어보기 */
                .join(member.team, team)
                .groupBy(team.name)
                //.having()
                .fetch();
        result.forEach(m -> System.out.println("m -> " + m));
    }
}
