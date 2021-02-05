package kr.seok.querydsl.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class AreaDto {
    private Long id;
    private String depth1Nm;
    private String depth2Nm;
    private String depth3Nm;
    private String depth4Nm;
    private Boolean useYn;

    @Builder
    public AreaDto(Long id, String depth1Nm, String depth2Nm, String depth3Nm, String depth4Nm, Boolean useYn) {
        this.id = id;
        this.depth1Nm = depth1Nm;
        this.depth2Nm = depth2Nm;
        this.depth3Nm = depth3Nm;
        this.depth4Nm = depth4Nm;
        this.useYn = useYn;
    }
}
