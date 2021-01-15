package kr.seok.querydsl;

import kr.seok.querydsl.domain.Member;
import kr.seok.querydsl.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local")
@Component
@RequiredArgsConstructor
public class InitMember {
    private final InitMemberService initMemberService;
    /* 스프링 실행 시 데이터 생성 */
    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    /* @PostConstruct와  @Transactional를 같이 사용할 수 없어서 service 생성 */
    @Component
    static class InitMemberService {
        @PersistenceContext
        EntityManager em;

        @Transactional
        public void init() {
            /* team A, B 생성 */
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");
            em.persist(teamA);
            em.persist(teamB);

            /* 두 팀에 속하는 멤버 생성 */
            for (int i = 0; i < 100; i++) {
                Team selectedTeam = i % 2 == 0 ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }
    }
}
