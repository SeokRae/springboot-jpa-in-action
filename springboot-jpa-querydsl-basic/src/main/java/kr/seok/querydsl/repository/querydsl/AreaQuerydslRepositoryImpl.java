package kr.seok.querydsl.repository.querydsl;

import com.google.common.base.Strings;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.seok.querydsl.domain.AreaEntity;
import kr.seok.querydsl.dto.AreaDto;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;
import static kr.seok.querydsl.domain.QAreaEntity.areaEntity;

public class AreaQuerydslRepositoryImpl implements AreaQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public AreaQuerydslRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    private QBean<AreaDto> getFields() {
        return Projections.fields(
                AreaDto.class,
                areaEntity.id,
                areaEntity.depth1Nm,
                areaEntity.depth2Nm,
                areaEntity.depth3Nm,
                areaEntity.depth4Nm,
                areaEntity.useYn
        );
    }

    @Override
    public List<AreaDto> getAreaGroupBy() {
        return queryFactory
                .select(Projections.fields(
                        AreaDto.class,
                        areaEntity.depth1Nm
                ))
                .from(areaEntity)
                .groupBy(
                        areaEntity.depth1Nm
                ).fetch();
    }

    @Override
    public List<AreaDto> getMultiColumnAndRow(List<AreaEntity> areas) {
        BooleanBuilder builder = new BooleanBuilder();
        areas.forEach(area ->
                builder.or(
                        areaEntity.id.eq(area.getId())
                                .and(areaEntity.depth3Nm.eq(area.getDepth3Nm()))
                ));
        return queryFactory
                .select(getFields())
                .from(areaEntity)
                .fetch();
    }

    private List<AreaDto> getAreaList() {
        return queryFactory
                .select(getFields())
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
