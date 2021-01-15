package kr.seok.querydsl.domain.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import kr.seok.querydsl.domain.Member;
import kr.seok.querydsl.domain.dto.MemberSearchCondition;
import kr.seok.querydsl.domain.dto.MemberTeamDto;
import kr.seok.querydsl.domain.repository.support.Querydsl4RepositorySupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static kr.seok.querydsl.domain.QMember.member;
import static kr.seok.querydsl.domain.QTeam.team;
import static org.springframework.util.StringUtils.isEmpty;

@Repository
public class MemberTestRepository extends Querydsl4RepositorySupport {

    public MemberTestRepository() {
        super(Member.class);
    }

    public List<Member> basicSelect() {
        return select(member)
                .from(member)
                .fetch();
    }

    public List<Member> basicSelectFrom() {
        return selectFrom(member)
                .fetch();
    }

    /* 쿼리 가져와서 페이징 처리 sort 처리까지 */
    public Page<Member> searchPageByApplyPage(
            MemberSearchCondition condition, Pageable pageable) {

        JPAQuery<Member> query =
                selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        List<Member> content = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();

        return PageableExecutionUtils.getPage(content, pageable, query::fetchCount);
    }

    /* 쿼리 가져와서 페이징 처리 sort 처리까지 */
    public Page<MemberTeamDto> searchPageByApplyPageToDto(
            MemberSearchCondition condition, Pageable pageable) {

        JPAQuery<Member> query =
                selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );

        List<Member> content = getQuerydsl()
                .applyPagination(pageable, query)
                .fetch();

        List<MemberTeamDto> collect = content.stream()
                .map(m -> new MemberTeamDto(m.getId(), m.getUsername(), m.getAge(), m.getTeam().getId(), m.getTeam().getName()))
                .collect(toList());

        return PageableExecutionUtils.getPage(collect, pageable, query::fetchCount);
    }

    /* super클래스의 추상화 applyPagination의 구현체 */
    public Page<Member> applyPagination(
            MemberSearchCondition condition, Pageable pageable) {

        /* 페이징, 쿼리, 람다 */
        return applyPagination(pageable,
                /* 내용 쿼리 */
                contentQuery -> contentQuery
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
        );
    }

    /* super클래스의 추상화 applyPagination의 구현체 */
    public Page<MemberTeamDto> applyPaginationToDto(
            MemberSearchCondition condition, Pageable pageable) {

        /* 페이징, 쿼리, 람다 */
        return applyPagination(pageable,
                /* 내용 쿼리 */
                contentQuery -> contentQuery
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ))
                .map(m -> new MemberTeamDto(m.getId(), m.getUsername(), m.getAge(), m.getTeam().getId(), m.getTeam().getName()));
    }

    /* 카운트 쿼리 분리 메서드 */
    public Page<Member> applyPagination2(
            MemberSearchCondition condition, Pageable pageable) {

        return applyPagination(
                /* 페이징 */
                pageable,
                /* 내용 쿼리 */
                contentQuery -> contentQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ),
                /* 카운트 쿼리 */
                countQuery -> countQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ));
    }
    /* 카운트 쿼리 분리 메서드 */
    public Page<MemberTeamDto> applyPagination2ToDto(
            MemberSearchCondition condition, Pageable pageable) {

        return applyPagination(
                /* 페이징 */
                pageable,
                /* 내용 쿼리 */
                contentQuery -> contentQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ),
                /* 카운트 쿼리 */
                countQuery -> countQuery
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(
                                usernameEq(condition.getUsername()),
                                teamNameEq(condition.getTeamName()),
                                ageGoe(condition.getAgeGoe()),
                                ageLoe(condition.getAgeLoe())
                        ))
                .map(m -> new MemberTeamDto(m.getId(), m.getUsername(), m.getAge(), m.getTeam().getId(), m.getTeam().getName()));
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
}
