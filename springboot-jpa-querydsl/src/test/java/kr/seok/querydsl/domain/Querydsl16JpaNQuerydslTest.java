package kr.seok.querydsl.domain;

import kr.seok.querydsl.domain.repository.MemberQuerydslRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class Querydsl16JpaNQuerydslTest {

    @Autowired
    private MemberQuerydslRepository memberQuerydslRepository;

    @Test
    @DisplayName("Querydsl을 이용한 조회쿼리")
    public void basicQuerydslTest() {
        Member member = new Member("member1", 10);
        memberQuerydslRepository.save(member);

        /*
            쿼리 조회 하지 않아도 됨 영속성 컨텍스트에 이미 있음
         */
        Member findMember = memberQuerydslRepository.findById(member.getId()).get();
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
        List<Member> result1 = memberQuerydslRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

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
        List<Member> result2 = memberQuerydslRepository.findByUsername_Querydsl("member1");
        assertThat(result2).containsExactly(member);
    }
}
