package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
