package kr.seok.querydsl.domain;

import kr.seok.querydsl.dto.AreaDto;
import kr.seok.querydsl.repository.AreaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

// repository 테스트를 위한 어노테이션
@DataJpaTest
class AreaEntityTest {

    @Autowired
    private AreaRepository areaRepository;

    @BeforeEach
    void setUp() {

        String depth1Nm = "서울특별시";
        String depth2Nm = "종로구";
        AreaEntity area1 = setArea(1100000000L, depth1Nm, "", "", "", true);
        AreaEntity area2 = setArea(1111000000L, depth1Nm, depth2Nm, "", "", true);
        AreaEntity area3 = setArea(1111010100L, depth1Nm, depth2Nm, "청운동", "", true);
        AreaEntity area4 = setArea(1111010200L, depth1Nm, depth2Nm, "신교동", "", true);
        AreaEntity area5 = setArea(1111010300L, depth1Nm, depth2Nm, "궁정동", "", true);

        areaRepository.saveAll(asList(area1, area2, area3, area4, area5));
    }

    private AreaEntity setArea(final Long areaCd,final String depth1Nm, final String depth2Nm, final String depth3Nm, final String depth4Nm, final boolean useYn) {
        return AreaEntity.builder()
                .id(areaCd)
                .depth1Nm(depth1Nm)
                .depth2Nm(depth2Nm)
                .depth3Nm(depth3Nm)
                .depth4Nm(depth4Nm)
                .useYn(useYn)
                .build();
    }

    @Test
    @DisplayName("다중 컬럼 동적 쿼리 테스트")
    void testCase7() {
        List<AreaEntity> areaList = Arrays.asList(
                setArea(1111010100L, "서울특별시", "종로구", "청운동", "", true)
                , setArea(1111010200L, "서울특별시", "종로구", "신교동", "", true)
                , setArea(1111010300L, "서울특별시", "종로구", "궁정동", "", true)
        );
        areaRepository.getMultiColumnAndRow(areaList)
                .forEach(System.out::println);
    }

    @Test
    @DisplayName("Area 엔티티 생성 테스트")
    void testCase1() {

        List<AreaEntity> areaEntities = areaRepository.findAll();

        // 사이즈
        assertThat(areaEntities.size()).isEqualTo(5);

        // Area 코드 테스트
        assertThat(areaEntities.get(0).getId()).isEqualTo(1100000000L);
        assertThat(areaEntities.get(1).getId()).isEqualTo(1111000000L);
    }

    // 그루핑 되는지 확인하기 위한 필드별 테스트
    @Test
    @DisplayName("getGroupByDepth1Nm() 테스트")
    void testCase2() {
        Map<String, Set<AreaDto>> groupByArea = areaRepository.getGroupByDepth1Nm();
        groupByArea.forEach((s, areaDtos) -> {
            areaDtos.forEach(areaDto -> {
                System.out.println("Key :: " + s + " :: Data " + areaDto);
            });
        });
    }

    @Test
    @DisplayName("getGroupByDepth2Nm() 테스트")
    void testCase3() {
        Map<String, Set<AreaDto>> groupByArea = areaRepository.getGroupByDepth2Nm();
        groupByArea.forEach((s, areaDtos) -> {
            areaDtos.forEach(areaDto -> {
                System.out.println("Key :: " + s + " :: Data " + areaDto);
            });
        });
    }
    @Test
    @DisplayName("getGroupByDepth3Nm() 테스트")
    void testCase4() {
        Map<String, Set<AreaDto>> groupByArea = areaRepository.getGroupByDepth3Nm();
        groupByArea.forEach((s, areaDtos) -> {
            areaDtos.forEach(areaDto -> {
                System.out.println("Key :: " + s + " :: Data " + areaDto);
            });
        });
    }

    @Test
    @DisplayName("Group By 테스트")
    void testCase5() {
        Map<String, Set<AreaDto>> groupByArea = areaRepository.getGroupByDepth4Nm();
        groupByArea.forEach((s, areaDtos) -> {
            areaDtos.forEach(areaDto -> {
                System.out.println("Key :: " + s + " :: Data " + areaDto);
            });
        });
    }

    @Test
    @DisplayName("Group By field 명으로 동적 쿼리 작성")
    void testCase6() {
        List<AreaDto> areaGroupBy = areaRepository.getAreaGroupBy();
        areaGroupBy.forEach(areaDto -> {
            System.out.println("Data : " + areaDto);
        });
    }
}
