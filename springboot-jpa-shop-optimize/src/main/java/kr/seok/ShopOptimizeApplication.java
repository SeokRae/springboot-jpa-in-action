package kr.seok;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShopOptimizeApplication {
    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }
    public static void main(String[] args) {
        SpringApplication.run(ShopOptimizeApplication.class, args);
    }
}
