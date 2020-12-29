# 

- gradle 의존관계 확인하기
```shell
./gradlew dependencies —configuration compileClasspath
```

- spring-boot-starter-web
    - tomcat
    - **spring-webmvc**
    
- spring-boot-starter-thymeleaf
  
- spring-boot-starter-data-jpa
    - aop
    - jdbc
        - **HikariCP 커넥션 풀**
    - **hibernate + JPA**
    - **spring-data-jpa**

- spring-boot-starter
    - spring-boot
        - spring-core
    - spring-boot-starter-logging
        - logback, slf4j

- spring-boot-starter-test
    - junit
        - 테스트 프레임워크
    - mockito
        - 목 라이브러리
    - assertj
        - 테스트 코드 지원 라이브러리
    - spring-test
        - 통합 테스트


# View 설정
- thymeleaf 템플릿 엔진
- thymeleaf 공식 사이트: https://www.thymeleaf.org/
- 스프링 공식 튜토리얼: https://spring.io/guides/gs/serving-web-content/
- 스프링부트 메뉴얼: https://docs.spring.io/spring-boot/docs/2.1.6.RELEASE/reference/html/ boot-features-developing-web-applications.html#boot-features-spring-mvc-template- engines

# h2 설정
- h2 홈페이지에서 다운 및 설치
- sh 파일 내용 확인 (자바로 실행됨을 확인)
- 파일 모드로 실행

# JPA
참고:스프링부트를 통해 복잡한 설정이 다 자동화되었다.
persistence.xml 도 없고, LocalContainerEntityManagerFactoryBean 도 없다. 
스프링 부트를 통한 추가 설정은 스프링 부트 메뉴얼을 참고하고, 
스프링 부트를 사용하지 않고 순수 스프링과 JPA 설정 방법은 자바 ORM 표준 JPA 프 로그래밍 책을 참고하자.

- [파라미터를 출력하는 외부 라이브러리](https://github.com/gavlyukovskiy/spring-boot-data-source-decorator)

# 도메인 분석

- 기능 목록
    - 회원 기능
        - 회원 등록
        - 회원 조회
    - 상품 기능
        - 상품 등록
        - 상품 수정
        - 상품 조회
    - 주문 기능 
        - 상품 주문
        - 주문 내역 조회
        - 주문 취소 
    - 기타 요구사항
        - 상품은 제고 관리가 필요하다.
        - 상품의 종류는 도서, 음반, 영화가 있다. 상품을 카테고리로 구분할 수 있다. 
      - 상품 주문시 배송 정보를 입력할 수 있다

- 운영에서 사용 지양하는 관계식
    - 다대다 @ManyToMany
    - 양방향

- 실무에서는 회원이 주문을 참조하지 않고, 주문이 회원을 참조하는 것으로 충분 하다
