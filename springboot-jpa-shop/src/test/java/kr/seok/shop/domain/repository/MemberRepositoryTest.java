package kr.seok.shop.domain.repository;

import kr.seok.shop.domain.Member;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @Transactional
    @Rollback(value = false)
    public void testCase1() {
        // given
        Member member = new Member();
//        member.setUserName("memberA");
//
//        // when
//        Long saveId = memberRepository.save(member);
//        Member findMember = memberRepository.find(saveId);
//
//        // then (검증)
//        assertThat(findMember.getId()).isEqualTo(member.getId());
//        assertThat(findMember.getUserName()).isEqualTo(member.getUserName());

        /* JPA 엔티티 유일성 보장 :: 영속성 컨첵스트 내에서 ID 값이 같은 경우 식별자가 같은 경우 같은 엔티티라고 볼 수 있다. (1차 캐시) */
//        assertThat(findMember).isEqualTo(member);
    }
}
