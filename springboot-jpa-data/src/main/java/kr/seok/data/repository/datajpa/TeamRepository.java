package kr.seok.data.repository.datajpa;

import kr.seok.data.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

}
