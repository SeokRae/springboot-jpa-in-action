package kr.seok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/* repository를 찾도록 설정하기 위해 필요하나 spring boot는 필요 없음 */
// @EnableJpaRepositories(basePackages = "kr.seok.data.repository")
public class JpaApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaApplication.class, args);
    }
}
