package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import kr.seok.data.domain.Team;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;

/**
 * 명세를 정의하기 위한 Specification 인터페이스를 구현
 *
 * 명세를 정의할 때는 toPredicate(...) 메서드만 구현하면 되는데
 *
 * JPA Criteria의 Root , CriteriaQuery , CriteriaBuilder 클래스를 파라미터 제공
 */
public class MemberSpec {

    public static Specification<Member> teamName(final String teamName) {
        return new Specification<Member>() {
            @Override
            public Predicate toPredicate(Root<Member> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

                if (StringUtils.isEmpty(teamName)) {
                    return null;
                }

                /* Member Inner Join Team */
                Join<Member, Team> t = root.join("team", JoinType.INNER); //회원과 조인
                /* Team.name */
                return builder.equal(t.get("name"), teamName);
            }
        };
    }

    public static Specification<Member> username(final String username) {
        return new Specification<Member>() {
            @Override
            public Predicate toPredicate(Root<Member> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                /* Member.username == username */
                return builder.equal(root.get("username"), username);
            }
        };
    }
}
