package kr.seok.data.repository.projection;

public interface NestedClosedProjection {
    String getUsername();

    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
