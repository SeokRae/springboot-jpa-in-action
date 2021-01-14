package kr.seok.querydsl.domain;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static kr.seok.querydsl.domain.QMember.member;
import static kr.seok.querydsl.domain.QTeam.team;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class Querydsl7조인Test {

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
     * 팀A에 소속된 모든 회원
     */
    @Test
    public void join() {
        /* select
                member1
            from
                Member member1
            inner join
                member1.team as team
            where
                team.name = ?1
         */
        List<Member> result = queryFactory
                .selectFrom(member)
                /* inner join == join */
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    public void innerJoin() {
        /*
            select
                member1
            from
                Member member1
            inner join
                member1.team as team
            where
                team.name = ?1
        */
        List<Member> result = queryFactory
                .selectFrom(member)
                /* inner join == join */
                .innerJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        /*
            카르테시안 곱 조인 이후 where에서 처리
            select
                member1
            from
                Member member1,
                Team team
            where
                member1.username = team.name
         */
        /*
            select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_ cross
            join
                team team1_
            where
                member0_.username=team1_.name
         */
        List<Member> result = queryFactory
                .select(member)
                /* outer 조인이 불가능 */
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='teamA'
     */
    @Test
    public void join_on_filtering() {

        /*
            select
                member1,
                team
            from
                Member member1
            left join
                member1.team as team with team.name = ?1

            select
                member0_.member_id as member_i1_1_0_,
                team1_.team_id as team_id1_2_1_,
                member0_.age as age2_1_0_,
                member0_.team_id as team_id4_1_0_,
                member0_.username as username3_1_0_,
                team1_.name as name2_2_1_
            from
                member member0_
            left outer join
                team team1_
                    on member0_.team_id=team1_.team_id
                    and (
                        team1_.name=?
                    )
         */
        /*
            tuple = [Member(id=3, username=member1, age=10), Team(id=1, name=teamA)]
            tuple = [Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
            tuple = [Member(id=5, username=member3, age=30), null]
            tuple = [Member(id=6, username=member4, age=40), null]
         */
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                /* left outer join 처리 */
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예)회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        /*
            select
                member1,
                team
            from
                Member member1
            left join
                Team team with member1.username = team.name
        */
        /*
            select
                member0_.member_id as member_i1_1_0_,
                team1_.team_id as team_id1_2_1_,
                member0_.age as age2_1_0_,
                member0_.team_id as team_id4_1_0_,
                member0_.username as username3_1_0_,
                team1_.name as name2_2_1_
            from
                member member0_
            left outer join
                team team1_
                    on (
                        member0_.username=team1_.name
                    )
         */

        /*
            t=[Member(id=3, username=member1, age=10), null]
            t=[Member(id=4, username=member2, age=20), null]
            t=[Member(id=5, username=member3, age=30), null]
            t=[Member(id=6, username=member4, age=40), null]
            t=[Member(id=7, username=teamA, age=0), Team(id=1, name=teamA)]
            t=[Member(id=8, username=teamB, age=0), Team(id=2, name=teamB)]
         */
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                /* 연관관계 없는 엔티티 외부(outer) 조인 */
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    @DisplayName("fetch join이 없이 조회하는 경우")
    public void fetchJoinNo() {
        em.flush();
        em.clear();

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
         */
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded =
                emf.getPersistenceUnitUtil()
                        /* 엔티티가 로딩되었는지 안되어있는지 확인 */
                        .isLoaded(findMember.getTeam());

        assertThat(loaded)
                /* 적용되지 않음을 확인 */
                .as("페치 조인 미적용").isFalse();
    }

    @Test
    @DisplayName("fetch join으로 조회하는 경우")
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        /*
            select
                member0_.member_id as member_i1_1_0_,
                team1_.team_id as team_id1_2_1_,
                member0_.age as age2_1_0_,
                member0_.team_id as team_id4_1_0_,
                member0_.username as username3_1_0_,
                team1_.name as name2_2_1_
            from
                member member0_
            inner join
                team team1_
                    on member0_.team_id=team1_.team_id
            where
                member0_.username=?
         */
        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                /* fetch join 확인 */
                    .fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded =
                emf.getPersistenceUnitUtil()
                        /* 엔티티가 로딩되었는지 안되어있는지 확인 */
                        .isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치 조인 적용").isTrue();
    }
}
