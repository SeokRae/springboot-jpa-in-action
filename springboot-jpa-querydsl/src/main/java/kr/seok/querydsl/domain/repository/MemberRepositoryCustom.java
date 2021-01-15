package kr.seok.querydsl.domain.repository;

import kr.seok.querydsl.domain.dto.MemberSearchCondition;
import kr.seok.querydsl.domain.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {
    List<MemberTeamDto> search(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageExecuteUtil(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDto> searchPageSort(MemberSearchCondition condition, Pageable pageable);
}
