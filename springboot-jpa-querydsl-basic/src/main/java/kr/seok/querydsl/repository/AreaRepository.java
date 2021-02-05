package kr.seok.querydsl.repository;

import kr.seok.querydsl.domain.AreaEntity;
import kr.seok.querydsl.repository.querydsl.AreaQuerydslRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<AreaEntity, Long>, AreaQuerydslRepository {
}
