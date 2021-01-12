package kr.seok.data.repository.datajpa;

public interface NestedClosedProjection {
    String getUsername();

    TeamInfo getTeam();

    interface TeamInfo {
        String getName();
    }
}
