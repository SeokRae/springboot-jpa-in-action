package kr.seok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

/* Auditing 설정하는 어노테이션 */
@EnableJpaAuditing
@SpringBootApplication
/* repository를 찾도록 설정하기 위해 필요하나 spring boot는 필요 없음 */
// @EnableJpaRepositories(basePackages = "kr.seok.data.repository")
public class JpaApplication {
    /**
     *  Auditing 기능 샘플 테스트 시 사용
     *  실무적으로는 securityContext에서 사용자 값을 넣어야 함
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAware<String>() {
            @Override
            public Optional<String> getCurrentAuditor() {
                return Optional.of(UUID.randomUUID().toString());
            }
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }
}
