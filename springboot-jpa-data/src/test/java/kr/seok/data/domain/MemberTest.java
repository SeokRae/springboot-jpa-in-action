package kr.seok.data.domain;

import kr.seok.data.repository.datajpa.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional
@SpringBootTest
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test
    @Rollback(false)
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        /* 팀 생성 및 영속성 컨텍스트 저장 */
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        /* 멤버 생성 및 영속성 컨텍스트 저장 */
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        //초기화
        em.flush();
        /* 1차 캐시 내용도 비우기 */
        em.clear();

        List<Member> members = em.createQuery(
                "select m from Member m", Member.class)
                .getResultList();

        /* 지연로딩 확인하기 -> 로그로 확인 가능 */
        for (Member member : members) {
            System.out.println("member=" + member);
            System.out.println("-> member.team=" + member.getTeam());
        }
    }

    @Autowired
    MemberRepository memberRepository;

    /*  Jpa에서 제공하는 이벤트를 이용하는 방법 */
    @Test
    public void JpaEventBaseEntity() throws Exception {
        //given
        Member member = new Member("member1");
        /* create 이벤트가 수행되는 시점 확인 */
        memberRepository.save(member); // @PrePersist

        Thread.sleep(100);
        member.setUsername("member2");

        /* update 이벤트가 수행되는 시점 확인 */
        em.flush(); // @PreUpdate
        em.clear();

        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        System.out.println("findMember.createdDate = " + findMember.getCreatedDate());
        System.out.println("findMember.createdBy = " + findMember.getCreatedBy());
//        System.out.println("findMember.updatedDate = " + findMember.getUpdatedDate());
        System.out.println("findMember.lastModifiedDate = " + findMember.getLastModifiedDate());
        System.out.println("findMember.lastModifiedBy = " + findMember.getLastModifiedBy());
    }
}
