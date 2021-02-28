package kr.seok.querydsl.repository.querydsl;

import kr.seok.querydsl.domain.AreaEntity;
import kr.seok.querydsl.dto.AreaDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AreaQuerydslRepository {

    Map<String, Set<AreaDto>> getGroupByDepth1Nm();
    Map<String, Set<AreaDto>> getGroupByDepth2Nm();
    Map<String, Set<AreaDto>> getGroupByDepth3Nm();
    Map<String, Set<AreaDto>> getGroupByDepth4Nm();
    List<AreaDto> getAreaGroupBy();

    List<AreaDto> getMultiColumnAndRow(List<AreaEntity> areaList);
}
