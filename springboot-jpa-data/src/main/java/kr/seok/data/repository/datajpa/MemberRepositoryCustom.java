package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Member;

import java.util.List;

public interface MemberRepositoryCustom {
    List<Member> findMemberCustom();
}
