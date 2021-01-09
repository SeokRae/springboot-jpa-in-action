package kr.seok.shop.domain.repository;

import kr.seok.shop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/* Spring Jpa Repository 사용하기 */
public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    List<Member> findByName(String name);
}
