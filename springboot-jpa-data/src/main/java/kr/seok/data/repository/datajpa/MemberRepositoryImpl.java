package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import lombok.RequiredArgsConstructor;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * MemberRepository`Impl` 클래스 명의 규칙이 중요, 스프링 데이터 JPA가 읽을 수 있음
 * Impl 대신 설정으로 하려는 경우
 * @EnableJpaRepositories(basePackages = "package명....repository", repositoryImplementationPostfix = "Impl")
 *
 * 화면 쿼리, 핵심 비즈니스 쿼리
 */
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final EntityManager em;

    @Override
    public List<Member> findMemberCustom() {
        return em.createQuery(
                "select m from Member m", Member.class)
                .getResultList();
    }
}
