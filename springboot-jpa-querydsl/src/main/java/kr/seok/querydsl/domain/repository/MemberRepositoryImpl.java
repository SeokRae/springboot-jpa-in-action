package kr.seok.querydsl.domain.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.seok.querydsl.domain.Member;
import kr.seok.querydsl.domain.dto.MemberSearchCondition;
import kr.seok.querydsl.domain.dto.MemberTeamDto;
import kr.seok.querydsl.domain.dto.QMemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static kr.seok.querydsl.domain.QMember.member;
import static kr.seok.querydsl.domain.QTeam.team;
import static org.springframework.util.StringUtils.isEmpty;

public class MemberRepositoryImpl
        //extends QuerydslRepositorySupport
        implements MemberRepositoryCustom {

    /* Querydsl 사용하기 위한 선언 */
    private final JPAQueryFactory queryFactory;
    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

//    /* QuerydslRepositorySupport 상속 시 사용 */
//    public MemberRepositoryImpl() {
//        super(Member.class);
//    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    /**
     * 단순한 페이징, fetchResults() 사용
     */
    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {

        /*
            Querydsl이 제공하는 fetchResults() 를 사용하면 내용과 전체 카운트를 한번에 조회할 수 있다.
            (실제 쿼리는 2번 호출)
         */
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                /* fetchResult() 는 카운트 쿼리 실행시 필요없는 order by 는 제거 */
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 복잡한 페이징
     * 데이터 조회 쿼리와, 전체 카운트 쿼리를 분리
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
        /* 데이터 조회용 쿼리 */
        List<MemberTeamDto> content = getContents(condition, pageable);

        /* 페이지 조회 쿼리 */
        long total = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression usernameEq(String username) {
        return isEmpty(username) ? null : member.username.eq(username);
    }
    private BooleanExpression teamNameEq(String teamName) {
        return isEmpty(teamName) ? null : team.name.eq(teamName);
    }
    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }


    /**
     * 단순한 페이징, fetchResults() 사용
     */
    @Override
    public Page<MemberTeamDto> searchPageExecuteUtil(MemberSearchCondition condition, Pageable pageable) {

        List<MemberTeamDto> content = getContents(condition, pageable);

        /* PageableExecutionUtils.getPage()로 최적화: JPAQuery에서 쿼리를 갖고 있다가 람다에서 필요할 때 호출 */
        JPAQuery<Member> countQuery = queryFactory
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        /* 전체 데이터가 페이지당 노출해야하는 데이터보다 작은 경우 카운트 쿼리 생략 */
        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
    }

    /* 스프링 데이터 정렬(Sort) */
    @Override
    public Page<MemberTeamDto> searchPageSort(MemberSearchCondition condition, Pageable pageable) {

        JPAQuery<Member> query = queryFactory.selectFrom(member);
        /* 정렬 */
        for (Sort.Order o : pageable.getSort()) {
            PathBuilder<Member> pathBuilder = new PathBuilder<>(member.getType(), member.getMetadata());
            query.orderBy(new OrderSpecifier(o.isAscending() ? Order.ASC : Order.DESC, pathBuilder.get(o.getProperty())));
        }

        List<MemberTeamDto> content = query.fetch()
                .stream()
                .map(member -> new MemberTeamDto(member.getId(), member.getUsername(), member.getAge(), member.getTeam().getId(), member.getTeam().getName()))
                .collect(Collectors.toList());

        JPAQuery<Member> countQuery = queryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
    }

    private List<MemberTeamDto> getContents(MemberSearchCondition condition, Pageable pageable) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

}
