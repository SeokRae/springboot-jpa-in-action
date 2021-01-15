package kr.seok.querydsl.domain;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
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
public class Querydsl12동적쿼리Test {

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
    @DisplayName("동적쿼리 where 조건을 한 번에 작성하는 방식")
    public void 동적쿼리_BooleanBuilder() {
        /* 이름, 나이로 검색 */
        String usernameParam = "member1";
        Integer ageParam = 10;

        /* 쿼리 조회 메서드 */
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
        List<Member> result = searchMember1(usernameParam, ageParam);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    /* 하나의 쿼리조건을 한번에 작성하는 방법 */
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder(
                /* 필수값이 존재하는 경우 생성자에 추가 */
                // member.username.eq(usernameCond)
        );

        if (usernameCond != null)
            builder.and(member.username.eq(usernameCond));
        if (ageCond != null)
            builder.and(member.age.eq(ageCond));

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    @DisplayName("동적쿼리 where 조건 별로 작성하는 방법")
    public void 동적쿼리_WhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    /* 조건 쿼리를 메서드별로 작성하는 방법 */
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                /* where 조건에 null 값은 무시되는 특징을 활용하여 각 조건마다 메서드를 생성 */
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }
    /* 메서드를 다른 쿼리에서도 재사용 가능 */
    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    @Test
    @DisplayName("동적쿼리 where 조건 메서드 재사용 테스트")
    public void 동적쿼리_whereAll() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember3(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    /* 조건 쿼리를 메서드별로 작성하는 방법 */
    private List<Member> searchMember3(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        /* null 체크는 주의해서 처리 */
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }
}
