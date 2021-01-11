package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;
import kr.seok.data.domain.dto.MemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
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
}
