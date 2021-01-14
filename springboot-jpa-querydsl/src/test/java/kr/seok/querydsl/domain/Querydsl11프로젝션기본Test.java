package kr.seok.querydsl.domain;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.seok.querydsl.domain.dto.MemberDto;
import kr.seok.querydsl.domain.dto.QMemberDto;
import kr.seok.querydsl.domain.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static kr.seok.querydsl.domain.QMember.*;

@SpringBootTest
@Transactional
public class Querydsl11프로젝션기본Test {

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
    @DisplayName("프로젝션 대상이 하나인 경우")
    void testCase1() {
        /*
            select
                member0_.username as col_0_0_
            from
                member member0_
         */
        /* 프로젝션 대상이 하나인 경우 */
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        result.forEach(s -> System.out.println("username : " + s));
    }


    @Test
    @DisplayName("distinct 사용 테스트")
    void projectionDistinct() {
        /*
            select
                distinct member0_.username as col_0_0_
            from
                member member0_
         */
        List<String> result = queryFactory
                .select(member.username).distinct()
                .from(member)
                .fetch();

        result.forEach(s -> System.out.println("username : " + s));
    }

    @Test
    @DisplayName("프로젝션 대상이 둘 이상인 경우, 튜플 조회")
    void testCase2() {
        /*
            select
                member0_.username as col_0_0_,
                member0_.age as col_1_0_
            from
                member member0_
         */
        /* 프로젝션 대상이 하나인 경우 */
        List<Tuple> result = queryFactory
                .select(
                        member.username,
                        member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username=" + username);
            System.out.println("age=" + age);
        }
    }

    @Test
    @DisplayName("순수 JPA에서 DTO 조회 테스트")
    void testCaseJPAToDto() {
        /*
            select
                new kr.seok.querydsl.domain.dto.MemberDto(m.username,
                m.age)
            from
                Member m
        */
        /*
            select
                member0_.username as col_0_0_,
                member0_.age as col_1_0_
            from
                member member0_
         */
        List<MemberDto> result = em.createQuery(
                "select new kr.seok.querydsl.domain.dto.MemberDto(m.username, m.age) "
                        + "from Member m"
                , MemberDto.class)
                .getResultList();
        result.forEach(member -> System.out.println("member : " + member));
    }

    @Test
    @DisplayName("Projections setter 활용")
    void testCaseProjectionSetter() {

        /*
            select
                member0_.username as col_0_0_,
                member0_.age as col_1_0_
            from
                member member0_
         */
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        result.forEach(member -> System.out.println("member : " + member));
    }
    @Test
    @DisplayName("Projections fields 활용")
    void testCaseProjectionFields() {

        /*
            select
                member0_.username as col_0_0_,
                member0_.age as col_1_0_
            from
                member member0_
         */
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        result.forEach(member -> System.out.println("member : " + member));
    }

    @Test
    @DisplayName("Projections 별칭 사용하기 ExpressionUtils.as(source,alias)")
    void testCaseProjectionsAlias() {

        QMember memberSub = new QMember("memberSub");

        /*
            select
                member0_.username as col_0_0_,
                (select
                    max(member1_.age)
                from
                    member member1_) as col_1_0_
            from
                member member0_
         */
        List<UserDto> fetch = queryFactory
                .select(
                        Projections.fields(
                                UserDto.class,
                                /* alias 설정 */
                                member.username.as("name"),

                            /* subQuery 필드나, 서브 쿼리에 별칭 적용 */
                            ExpressionUtils.as(
                                    JPAExpressions
                                            .select(memberSub.age.max())
                                            .from(memberSub), "age")
                        )
                ).from(member)
                .fetch();
        fetch.forEach(user -> System.out.println("user -> " + user));
    }

    @Test
    @DisplayName("Projections 생성자를 사용하여 데이터 조회")
    void projectionsConstructor() {
        /*
            select
                member0_.username as col_0_0_,
                member0_.age as col_1_0_
            from
                member member0_
         */
        /*
            member -> MemberDto(username=member1, age=10)
            member -> MemberDto(username=member2, age=20)
            member -> MemberDto(username=member3, age=30)
            member -> MemberDto(username=member4, age=40)
         */
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
        result.forEach(member -> System.out.println("member -> " + member));
    }

    /**
     * @see QMemberDto
     * QType -> DTO 로 변환해서 조회
     */
    @Test
    @DisplayName("@QueryProjection 어노테이션을 이용하여 Q파일 조회하기")
    void queryProjectionBasic() {

        /*
            select
                member0_.username as col_0_0_,
                member0_.age as col_1_0_
            from
                member member0_
         */
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        result.forEach(member -> System.out.println("member -> " + member));
    }
}
