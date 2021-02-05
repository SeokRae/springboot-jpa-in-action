package kr.seok.querydsl.repository.querydsl;

import antlr.StringUtils;
import com.google.common.base.Strings;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.seok.querydsl.dto.AreaDto;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static kr.seok.querydsl.domain.QAreaEntity.areaEntity;
import static org.apache.logging.log4j.ThreadContext.isEmpty;

public class AreaQuerydslRepositoryImpl implements AreaQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public AreaQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    private List<AreaDto> getAreaList() {
        return queryFactory
                .select(Projections.fields(
                        AreaDto.class,
                        areaEntity.id,
                        areaEntity.depth1Nm,
                        areaEntity.depth2Nm,
                        areaEntity.depth3Nm,
                        areaEntity.depth4Nm,
                        areaEntity.useYn
                ))
                .from(areaEntity)
                .orderBy(areaEntity.id.desc())
                .fetch();
    }

    @Override
    public Map<String, Set<AreaDto>> getGroupByDepth1Nm() {
        List<AreaDto> areaList = getAreaList();
        return areaList.stream()
                .filter(areaDto -> !Strings.isNullOrEmpty(areaDto.getDepth1Nm()))
                .collect(
                        groupingBy(AreaDto::getDepth1Nm, toSet())
                );
    }

    @Override
    public Map<String, Set<AreaDto>> getGroupByDepth2Nm() {
        List<AreaDto> areaList = getAreaList();
        return areaList.stream()
                .filter(areaDto -> !Strings.isNullOrEmpty(areaDto.getDepth2Nm()))
                .collect(
                        groupingBy(AreaDto::getDepth2Nm, toSet())
                );
    }

    @Override
    public Map<String, Set<AreaDto>> getGroupByDepth3Nm() {
        List<AreaDto> areaList = getAreaList();
        return areaList.stream()
                .filter(areaDto -> !Strings.isNullOrEmpty(areaDto.getDepth3Nm()))
                .collect(
                        groupingBy(AreaDto::getDepth3Nm, toSet())
                );
    }

    @Override
    public Map<String, Set<AreaDto>> getGroupByDepth4Nm() {
        List<AreaDto> areaList = getAreaList();
        return areaList.stream()
                .filter(areaDto -> !Strings.isNullOrEmpty(areaDto.getDepth4Nm()))
                .collect(
                        groupingBy(AreaDto::getDepth4Nm, toSet())
                );
    }
}
