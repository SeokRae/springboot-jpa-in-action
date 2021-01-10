package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import kr.seok.data.repository.jpa.MemberJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring-data-jpa 기반 코드 테스트
 */
@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("Spring-Data-JPA 기반 코드: Member 객체 확인")
    void testMember() {
        Member member = new Member("memberA");
        Member save = memberRepository.save(member);
        /*
            insert 후에 select 없이 데이터를 확인할 수 있는 이유
            트랜잭션 내에 영속성 컨텍스트에서 데이터를 관리하고 있어서 DB까지 조회하지 않고 1차 캐시 내에서 조회가 가능
         */
        Member findMember = memberRepository.findById(save.getId()).get();

        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }
}
