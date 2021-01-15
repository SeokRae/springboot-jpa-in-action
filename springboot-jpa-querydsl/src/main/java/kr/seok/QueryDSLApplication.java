package kr.seok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class QueryDSLApplication {
//    @Bean
//    JPAQueryFactory jpaQueryFactory(EntityManager em) {
//        return new JPAQueryFactory(em);
//    }
    public static void main(String[] args) {
        SpringApplication.run(QueryDSLApplication.class, args);
    }
}
