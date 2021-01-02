package kr.seok.shop.domain.repository;

import kr.seok.shop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/* 스프링 빈으로 등록, JPA 예외를 스프링 기반 예외로 예외 변환 */
@Repository
@RequiredArgsConstructor
public class MemberRepository {
    /* 엔티티 메니저( EntityManager ) 주입 */
    // RequiredArgsConstructor추가로 인하여 @PersistenceContext(JPA 표준) 어노테이션 생략 (자동 주입)
    private final EntityManager em;

    /* 트랜잭션이 종료될 때 commit 처리 */
    public void save(Member member) {
        em.persist(member);
    }

    public Member findOne(Long id) {
        return em.find(Member.class, id);
    }

    public List<Member> findAll() {
        /* JPQL */
        return em.createQuery(
                "select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) {
        return em.createQuery(
                /* 객체를 대상으로 조회 */
                "select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
