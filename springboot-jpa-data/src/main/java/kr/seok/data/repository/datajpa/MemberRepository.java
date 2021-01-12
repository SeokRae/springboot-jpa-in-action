package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import kr.seok.data.domain.dto.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>,
        /* 데이터-JPA 에서 제공하는 방식으로 인터페이스를 상속 받으면 구현 클래스의 메서드를 호출하여 실행이 됨 */
        MemberRepositoryCustom {

    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
//    @Query(name = "Member.findByUsername")
    List<Member> findByUsername(@Param("username") String username);

    /* username, age 조건으로 entity 조회 */
    @Query("select m from Member m where m.username= :username and m.age = :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);

    /* DTO로 바로 매핑 */
    @Query("select new kr.seok.data.domain.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
    List<MemberDto> findMemberDto();

    /* username 값으로 조회된 entity 리스트를 조회 */
    @Query("select m from Member m where m.username = :name")
    Member findMembers(@Param("name") String username);

    /* username 리스트로 조회된 Entity 리스트를 조회 (최적화에 좋음) */
    @Query("select m from Member m where m.username in :names")
    /* List<String>이 아니라 Collection<String>을 사용하여 범용성 있게 활용할 수 있도록 함 */
    List<Member> findByNames(@Param("names") Collection<String> names);

    /* Member 의 username 필드리스트를 출력 */
    @Query("select m.username from Member m")
    List<String> findByUsernameList();

    /* 반환타입 유연함 */
    List<Member> findListByUsername(String name);
    Member findOneByUsername(String name);
    Optional<Member> findOptionalByUsername(String name);

    /* 페이징 */
    Page<Member> findPageByAge(int age, Pageable pageable);
    Slice<Member> findSliceByAge(int age, Pageable pageable);
    List<Member> findListByAge(int age, Pageable pageable);

    /* 벌크 업데이트 후에 영속성 컨텍스트를 초기화 */
    @Modifying(clearAutomatically = true)
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgePlus(@Param("age") int age);

    /* 벌크 업데이트 후에 영속성 컨텍스트를 초기화 */
    @Modifying
    @Query("update Member m set m.age = m.age + 1 where m.age >= :age")
    int bulkAgeErrPlus(@Param("age") int age);

    @Query("select m from Member m left join fetch m.team")
    List<Member> findMemberFetchJoin();

    // #################################################################
    @Override
    /* JPQL 대신 설정으로 fetch join 효과를 보는 방법 */
    @EntityGraph(attributePaths = {"team"})
    List<Member> findAll();

    /* JPQL + EntityGraph로 fetch join 효과보는 방법 */
    @Query("select m from Member m")
    @EntityGraph(attributePaths = {"team"})
    List<Member> findMemberEntityGraph();

    /* 메서드 이름 쿼리 설정 시 fetch Join 효과 보기 위한 설정 */
    @EntityGraph(attributePaths = {"team"})
    List<Member> findEntityGraphByUsername(@Param("username") String username);

    // #################################################################

    /* 많이 쓰이지 않는 방식이지만 JPA 기본 스펙 */
    @EntityGraph("Member.all")
    @Query("select m from Member m")
    List<Member> findMemberNamedEntityGraph();


    // Hint #################################################################
    /**
     *  조회 성능에서 개선이 될 수는 있지만 효과가 크지 않다. 이미 그때는 쿼리에 문제가 있는거임
     *  많은 트래픽으로인해 이를 성능을 최적화하기 위해서 hint를 쓴다는 건 많은 효과를 보지 못함
     *  이미 레디스같은 캐시 서버를 두는게 더 효과적
     */
    @QueryHints(
            value = @QueryHint(
                    /*
                        JPA에서는 힌트라는 기능을 제공하지 않고 hibernate에서 제공하는 기능
                        변경감지 체크를 하지 않도록 설정
                    */
                    name = "org.hibernate.readOnly", value = "true")
    )
    Member findReadOnlyByUsername(String username);

    /**
     * 페이징
     *
     * forCounting :
     *  반환 타입으로 Page 인터페이스를 적용하면
     *  추가로 호출하는 페이징을 위한 count 쿼리도 쿼리에 힌트 적용가능
     *  (기본값 true)
     * @see QueryHints
     *
     */
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.readOnly", value = "true")
    },
            forCounting = true)
    Page<Member> findByUsername(String name, Pageable pageable);

    /**
     *
     * ... for update
     * 이 내용은 data-jpa의 내용에서 깊은 내용이기 땨문에 책을 참고하는 것을 추천
     * @see Lock
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Member> findLockByUsername(String name);
}
