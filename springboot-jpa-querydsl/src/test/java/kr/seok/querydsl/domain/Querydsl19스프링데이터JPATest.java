package kr.seok.querydsl.domain;

import kr.seok.querydsl.domain.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class Querydsl19스프링데이터JPATest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("스프링 데이터 JPA 정적쿼리 동작 확인 테스트")
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);
        /* 데이터 조회를 확인하기 위한 테스트 */
        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        /*
            select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
         */
        List<Member> result1 = memberRepository.findAll();
        assertThat(result1).containsExactly(member);

        /*
            select
                generatedAlias0
            from
                Member as generatedAlias0
            where
                generatedAlias0.username=:param0
        */
        /*
            select
                member0_.member_id as member_i1_1_,
                member0_.age as age2_1_,
                member0_.team_id as team_id4_1_,
                member0_.username as username3_1_
            from
                member member0_
            where
                member0_.username=?
         */
        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

}
