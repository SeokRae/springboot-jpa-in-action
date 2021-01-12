package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import kr.seok.data.domain.Team;
import kr.seok.data.domain.dto.MemberDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Spring-data-jpa 기반 코드 테스트
 */
@SpringBootTest
@Transactional
@Rollback(false)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;

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

    @Test
    public void basicCRUD() {
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");
        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);

        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("사용자명, 나이를 기준으로 조회하는 테스트")
    public void findByUsernameAndAgeGreaterThan() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> result =
                memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("Member의 username List 조회 테스트")
    public void findMemberUsername() {
        Team team = new Team("TeamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<String> usernameList = memberRepository.findByUsernameList();
        usernameList.forEach(s -> System.out.println("s : " + s));
    }

    @Test
    @DisplayName("MemberDto List 조회 테스트")
    public void findMemberDto() {
        Team team = new Team("TeamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);

        List<MemberDto> memberDto = memberRepository.findMemberDto();
        memberDto.forEach(member -> System.out.println("Member Dto : " + member));
    }

    @Test
    @DisplayName("In 절 어덯게 들어가는지 확인하기 위한 테스트")
    public void findByNames() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);
        List<Member> byNames = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        byNames.forEach(member -> System.out.println("Member : " + member));
    }

    @Test
    public void returnType() {
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        Member m3 = new Member("CCC", 30);
        Member m4 = new Member("CCC", 40);
        memberRepository.save(m1);
        memberRepository.save(m2);
        memberRepository.save(m3);
        memberRepository.save(m4);

        /* 절대 null 이 아님 */
        List<Member> memberList = memberRepository.findListByUsername("AAA");
        assertThat(memberList.get(0).getUsername()).isEqualTo(m1.getUsername());

        /* 값이 없으면 null을 반환하므로 문제가 발생할 수 있음 */
        Member member = memberRepository.findOneByUsername("AAA");
        assertThat(member.getUsername()).isEqualTo(m1.getUsername());

        /* 단건에 대해서는 optional 처리 */
        Optional<Member> optionalMember = memberRepository.findOptionalByUsername("AAA");
        assertThat(optionalMember.get().getUsername()).isEqualTo(m1.getUsername());

        /* 값이 존재하지 않아 isEmpty 값 반환 */
        Optional<Member> optionalMember2 = memberRepository.findOptionalByUsername("DDD");
        assertThat(optionalMember2).isEmpty();

    }

    @Test
    @DisplayName("Single Result 조회에서 2건 이상 조회 되는 경우 예외처리 테스트")
    public void returnException() {
        Member m1 = new Member("CCC", 30);
        Member m2 = new Member("CCC", 40);
        memberRepository.save(m1);
        memberRepository.save(m2);

        assertThatThrownBy(() -> {
            /* 2개 이상 값이 넘어오는 경우 에외 */
            memberRepository.findOptionalByUsername("CCC");
        }).isInstanceOf(IncorrectResultSizeDataAccessException.class)
        .hasMessageContaining("query did not return a unique result");
    }

    /**
     * 두 번째 파라미터로 받은 Pagable 은 인터페이스로 설정되어 있으나
     * 실제 사용할 때는 해당 인터페이스를 구현한 org.springframework.data.domain.PageRequest 객체를 사용
     *
     * PageRequest 생성자의 첫 번째 파라미터에는 현재 페이지를, 두 번째 파라미터에는 조회할 데이터 수를 입력한다.
     *
     * 추가로 정렬 정보도 파라미터로 사용할 수 있다. 참고로 페이지는 0부터 시작한다.
     *
     * [주의] Page는 1부터 시작이 아니라 0부터 시작이다.
     *
     * [실무] 페이징은
     * @see Page
     */
    @Test
    @DisplayName("페이징 처리 테스트")
    public void page() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        // when
        PageRequest pageRequest =
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findPageByAge(10, pageRequest);

        // then
        List<Member> content = page.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?

    }

    /**
     * Infinity Scroll (더보기) 같은 경우 사용
     * @see org.springframework.data.domain.Slice
     */
    @Test
    @DisplayName("Slice를 활용하는 페이징 처리 테스트")
    public void slice() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        // when
        PageRequest pageRequest =
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Slice<Member> page = memberRepository.findSliceByAge(10, pageRequest);
        // then
        List<Member> content = page.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(page.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(page.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(page.hasNext()).isTrue(); //다음 페이지가 있는가?
    }

    /**
     * 그냥 리스트로 데이터를 조회하기
     */
    @Test
    @DisplayName("그냥 데이터를 페이징 처리 테스트")
    public void listPage() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 10));
        memberRepository.save(new Member("member3", 10));
        memberRepository.save(new Member("member4", 10));
        memberRepository.save(new Member("member5", 10));
        // when
        PageRequest pageRequest =
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        List<Member> page = memberRepository.findListByAge(10, pageRequest);

        // then
        assertThat(page.size()).isEqualTo(3); //조회된 데이터 수
    }

    @Test
    @DisplayName("실무에서 사용할 수 있도록 entity -> dto 페이징 처리 테스트")
    public void entityToDtoPage() {
        //given
        Team teamA = new Team("TeamA");
        Team teamB = new Team("TeamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        Member memberA = new Member("member1", 10);
        memberA.setTeam(teamA);
        Member memberB = new Member("member2", 10);
        memberB.setTeam(teamB);
        Member memberC = new Member("member3", 10);
        memberC.setTeam(teamA);
        Member memberD = new Member("member4", 10);
        memberD.setTeam(teamB);
        Member memberE = new Member("member5", 10);
        memberE.setTeam(teamA);

        memberRepository.save(memberA);
        memberRepository.save(memberB);
        memberRepository.save(memberC);
        memberRepository.save(memberD);
        memberRepository.save(memberE);
        // when
        PageRequest pageRequest =
                PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));
        Page<Member> page = memberRepository.findPageByAge(10, pageRequest);

        /* 페이징 처리된 entity 를 Dto로 변환 */
        Page<MemberDto> map = page.map(m ->
                MemberDto.builder()
                        .id(m.getId())
                        .username(m.getUsername())
                        /* 연관관계이기 때문에 getTeam() 할 때 매핑이 되어 있는지? 생각하기 */
                        .teamName(m.getTeam().getName())
                        .build()
        );

        // then
        List<MemberDto> content = map.getContent(); //조회된 데이터
        assertThat(content.size()).isEqualTo(3); //조회된 데이터 수
        assertThat(map.getTotalElements()).isEqualTo(5); //전체 데이터 수
        assertThat(map.getNumber()).isEqualTo(0); //페이지 번호
        assertThat(map.getTotalPages()).isEqualTo(2); //전체 페이지 번호
        assertThat(map.isFirst()).isTrue(); //첫번째 항목인가?
        assertThat(map.hasNext()).isTrue(); //다음 페이지가 있는가?
    }

    @Test
    @DisplayName("bulk 성 수정 쿼리 테스트")
    public void bulkUpdate() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @PersistenceContext
    EntityManager em;

    @Test
    @DisplayName("영속성 초기화를 하지 않고 bulk 업데이트하는 경우, 영속성 초기화로 DB에서 조회하여 bulk 연산 적용된것 확인 테스트")
    public void bulkErrorUpdate() {
        //given
        memberRepository.save(new Member("member1", 10));
        memberRepository.save(new Member("member2", 19));
        memberRepository.save(new Member("member3", 20));
        memberRepository.save(new Member("member4", 21));
        memberRepository.save(new Member("member5", 40));

        //when
        /* 영속성 컨텍스트를 초기화하지 않은 bulk 연산 메서드 */
        int resultCount = memberRepository.bulkAgeErrPlus(20);


        List<Member> member5 = memberRepository.findByUsername("member5");
        /* bulk 연산을 했음에도 DB와 연속성에 있는 데이터가 다른 내용 확인 */
        assertThat(member5.get(0).getAge()).isEqualTo(40);

        /* 영속성 컨텍스트를 비워 DB에서 조회가 될 수 있도록 함 */
        em.clear();

        /* 영속성 컨텍스트에 있던 데이터가 아니라 DB에서 새로 꺼내 bulk 연산이 적용된 데이터 확인 */
        List<Member> memberFlush = memberRepository.findByUsername("member5");
        assertThat(memberFlush.get(0).getAge()).isEqualTo(41);

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy() throws Exception {
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamB));

        /* DB 반영 */
        em.flush();
        /* 영속성 컨텍스트 clear */
        em.clear();

        //when
        /* 한 번의 조회로 member를 모두 조회 */
        List<Member> members = memberRepository.findAll();
        //then
        members.forEach(member -> {
            /*
                kr.seok.data.domain.Team$HibernateProxy$6aGAMZdv 프록시 초기화:
                실제 데이터베이스에 쿼리를 날려 데이터를 조회
                N + 1 문제라고 칭함
             */
            System.out.println("member -> " + member.getTeam().getClass());
            System.out.println("member -> " + member.getTeam().getName());
        });
    }

    @Test
    public void findMemberLazyFetchJoin() throws Exception {
        //member1 -> teamA
        //member2 -> teamB
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        teamRepository.save(teamA);
        teamRepository.save(teamB);

        memberRepository.save(new Member("member1", 10, teamA));
        memberRepository.save(new Member("member2", 20, teamB));

        /* DB 반영 */
        em.flush();
        /* 영속성 컨텍스트 clear */
        em.clear();

        //when
        /*
         * 연관관계가 있는 엔티티를 한 번에 모두 조회
         *
         *     select
         *         member0_.member_id as member_i1_0_0_,
         *         team1_.team_id as team_id1_1_1_,
         *         member0_.age as age2_0_0_,
         *         member0_.team_id as team_id4_0_0_,
         *         member0_.username as username3_0_0_,
         *         team1_.name as name2_1_1_
         *     from
         *         member member0_
         *     left outer join
         *         team team1_
         *             on member0_.team_id=team1_.team_id
         */
        List<Member> members = memberRepository.findMemberFetchJoin();
        //then
        members.forEach(member -> {
            /*
                프록시 초기화가 아닌 Team 엔티티가 조회되어 프록시로 초기화 작업을 하지 않음
             */
            System.out.println("member -> " + member.getTeam().getClass());
            System.out.println("member -> " + member.getTeam().getName());
        });
    }

    @Test
    public void queryHint() {
        //given
        memberRepository.save(new Member("member1", 10));
        em.flush(); /* db에 쿼리 호출 및 sync 맞추기 */
        em.clear(); /* 영속성 컨텍스트를 비우기 */

        //when
        Member member = memberRepository.findReadOnlyByUsername("member1");
        /* 기본적으로는 dirty checking 기능으로 변경사항을 감지하여 update 쿼리 호출 */
        member.setUsername("member2");

        em.flush(); //Update Query 실행X

    }
}
