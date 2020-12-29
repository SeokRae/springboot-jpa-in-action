package kr.seok.shop.domain.repository;

import kr.seok.shop.domain.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(value = false)
    public void 회원_등록_테스트() {
        // given
        Member member = new Member();
        member.setName("memberA");

        // when
        memberRepository.save(member);
        List<Member> findMembers = memberRepository.findByName("memberA");

        // then (검증)
        Member findMember = findMembers.get(0);
        assertThat(findMember.getName()).isEqualTo("memberA");

        /* JPA 엔티티 유일성 보장 :: 영속성 컨첵스트 내에서 ID 값이 같은 경우 식별자가 같은 경우 같은 엔티티라고 볼 수 있다. (1차 캐시) */
        assertThat(findMember).isEqualTo(member);
    }
}
