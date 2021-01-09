package kr.seok.shop.service;

import kr.seok.shop.domain.Member;
import kr.seok.shop.domain.repository.MemberJpaRepository;
import kr.seok.shop.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
/* 해당 클래스는 select 하는 로직이 많아 기본적으로 readOnly = true 를 기본으로 설정 */
@Transactional(readOnly = true)
/* final 예약어가 설정된 필드를 생성자 주입 */
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final MemberJpaRepository memberJpaRepository;
    /**
     * 회원가입
     */
    @Transactional //변경
    public Long join(Member member) {

        validateDuplicateMember(member); //중복 회원 검증
        memberJpaRepository.save(member);

        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberJpaRepository.findByName(
                /* 멀티 스레드 환경에서 비즈니스 로직이 실행되는 경우 duplicate 오류 발생이 가능하므로 member.name 필드에 unique 설정 필요 */
                member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberJpaRepository.findById(id).get();
        member.setName(name);
    }

    /**
     * 전체 회원 조회
     */
    public List<Member> findMembers() {
        return memberJpaRepository.findAll();
    }

    public Member findOne(Long memberId) {
        return memberJpaRepository.findById(memberId).get();
    }
}
