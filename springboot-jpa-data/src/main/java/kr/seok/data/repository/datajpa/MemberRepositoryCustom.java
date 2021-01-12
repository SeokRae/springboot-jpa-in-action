package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;

import java.util.List;

/**
 * 사용자 커스텀 Repository
 */
public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
