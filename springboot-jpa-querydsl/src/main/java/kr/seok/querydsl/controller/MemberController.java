package kr.seok.querydsl.controller;

import kr.seok.querydsl.domain.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberJpaRepository memberJpaRepository;
//    @GetMapping("/v1/members")
//    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
//        return memberJpaRepository.search(condition);
//    }

}
