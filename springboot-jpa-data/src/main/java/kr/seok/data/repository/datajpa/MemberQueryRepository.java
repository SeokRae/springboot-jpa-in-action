package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 비즈니스 로직적으로 분리하려는 경우 클래스로 구현하여 사용
 */
@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {

    private final EntityManager em;

    public List<Member> findAllMembers() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

}
