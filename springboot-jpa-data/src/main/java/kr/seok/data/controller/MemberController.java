package kr.seok.data.controller;

import kr.seok.data.domain.Member;
import kr.seok.data.domain.dto.MemberDto;
import kr.seok.data.repository.datajpa.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberRepository memberRepository;

//    @PostConstruct
//    public void init() {
//        for(int i = 0 ; i < 100 ; i++) {
//            memberRepository.save(new Member("user" + i, i));
//        }
//
//    }

    @GetMapping("/members/{id}")
    public String findMember(@PathVariable("id") Long id) {
        return memberRepository.findById(id).get().getUsername();
    }

    /* 도메인 클래스 컨버터 기능 */
    @GetMapping("/members2/{id}")
    public String findMember(@PathVariable("id") Member member) {
        /*
            데이터 조회용으로만 쓸 것
            memberRepository를 통해 쿼리 조회를 수행 함
             select
                member0_.member_id as member_i1_0_0_,
                member0_.created_date as created_2_0_0_,
                member0_.last_modified_date as last_mod3_0_0_,
                member0_.created_by as created_4_0_0_,
                member0_.last_modified_by as last_mod5_0_0_,
                member0_.age as age6_0_0_,
                member0_.team_id as team_id8_0_0_,
                member0_.username as username7_0_0_
            from
                member member0_
            where
                member0_.member_id=?

            도메인 클래스 컨버터도 리파지토리를 사용해서 엔티티를 찾음
            잘 활용하기 위해서는 JPA에 대한 깊은 이해를 필요로 함
         */
        return member.getUsername();
    }

    /* Member entity 페이징 - 글로벌 설정 */
    @GetMapping("/members")
    public Page<Member> list(Pageable pageable) {
        /* Pageable 인터페이스를 파라미터로 사용하는 경우 binding 될 때 pageRequest 객체를 생성하여 전달 */
        return memberRepository.findAll(pageable);
    }

    /* DTO로 변환 - 글로벌 설정 */
    @GetMapping("/members/dto")
    public Page<MemberDto> listSimple(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(member ->
                        new MemberDto(member.getId(), member.getUsername(), null)
                );
    }

    /* 페이징 어노테이션을 사용하는 방법 */
    @GetMapping("/members/default")
    public Page<MemberDto> listDefaultPage(
            @PageableDefault(size = 12, sort = "username", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return memberRepository.findAll(pageable)
                .map(MemberDto::new);
    }
}
