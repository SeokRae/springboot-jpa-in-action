package kr.seok.data.repository.jpa;

import kr.seok.data.domain.Member;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class MemberJpaRepository {

    @PersistenceContext
    private EntityManager em;

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    public Member find(Long id) {
        return em.find(Member.class, id);
    }

    public void delete(Member member) {
        em.remove(member);
    }

    public List<Member> findAll() {
        /* JPQL 사용 */
        return em.createQuery(
                "select m from Member m", Member.class)
                .getResultList();
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }

    public long count() {
        return em.createQuery(
                "select count(m) from Member m", Long.class)
                /* 결과 하나 반환 */
                .getSingleResult();
    }

    public List<Member> findByUsernameAndAgeGreaterThan(String username, int age) {
        return em.createQuery(
                "select m from Member m where m.username = :username and m.age > :age", Member.class)
                .setParameter("username", username)
                .setParameter("age", age)
                .getResultList();
    }

    /* NamedQuery 샘플 그러나 잘 안씀 */
    public List<Member> findByUsername(String username) {
        return em.createNamedQuery("Member.findNamedQueryByUsername", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    /* 데이터 페이징 처리 */
    public List<Member> findAgeByPage(int age, int offset, int limit) {
        return em.createQuery(
                "select m from Member m where m.age = :age order by m.username desc", Member.class)
                .setParameter("age", age)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
    /* 총 데이터 건수 조회 */
    public long totalCount(int age) {
        return em.createQuery(
                "select count(m) from Member m where m.age = :age", Long.class)
                .setParameter("age", age)
                .getSingleResult();
    }

    /* 벌크성 수정 쿼리 */
    public int bulkAgePlus(int age) {
        return em.createQuery(
                "update Member m set m.age = m.age + 1 " +
                        "where m.age >= :age")
                .setParameter("age", age)
                .executeUpdate();
    }

}
