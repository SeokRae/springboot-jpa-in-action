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

- 실무에서는 @ManyToMany 를 사용하지 말자
    - @ManyToMany 는 편리한 것 같지만, 중간 테이블( CATEGORY_ITEM )에 컬럼을 추가할 수 없고, 세밀하게 쿼 리를 실행하기 어렵기 때문에 실무에서 사용하기에는 한계가 있다
    - 중간 엔티티
      ( CategoryItem 를 만들고 @ManyToOne , @OneToMany 로 매핑해서 사용하자. ) 
    - 정리하면 대다대 매핑을 일대다, 다대일 매핑으로 풀어 내서 사용하자.

- Address
    - 값 타입은 변경 불가능하게 설계해야 한다.
    - @Setter 를 제거하고, 생성자에서 값을 모두 초기화해서 변경 불가능한 클래스를 만들자.
    - JPA 스펙상 엔티티나 임베디드 타입( @Embeddable )은 자바 기본 생성자(default constructor)를 public 또는 protected 로 설정해야 한다.
    - public 으로 두는 것 보다는 protected 로 설정하는 것이 그나마 더 안전 하다.
    - JPA가 이런 제약을 두는 이유는 JPA 구현 라이브러리가 객체를 생성할 때 리플랙션 같은 기술을 사용할 수 있도록 지원해야 하기 때문이다.

> 엔티티 설계시 주의할 점
- 엔티티에는 가급적 Setter를 사용하지 말자
    - Setter가 모두 열려있다. 변경 포인트가 너무 많아서, 유지보수가 어렵다. 나중에 리펙토링으로 Setter 제거

- 모든 연관관계는 지연로딩으로 설정!
    - 즉시로딩( EAGER )은 예측이 어렵고, 어떤 SQL이 실행될지 추적하기 어렵다. 
    - 특히 JPQL을 실행할 때 N+1 문제가 자주 발생한다.
    - 실무에서 모든 연관관계는 지연로딩( LAZY )으로 설정해야 한다.
    - 연관된 엔티티를 함께 DB에서 조회해야 하면, fetch join 또는 엔티티 그래프 기능을 사용한다.
    - @XToOne(OneToOne, ManyToOne) 관계는 기본이 즉시로딩이므로 직접 지연로딩으로 설정해야 한다.

- 컬렉션은 필드에서 초기화 하자.
    - 하이버네이트는 엔티티를 영속화 할 때, 컬랙션을 감싸서 하이버네이트가 제공하는 내장 컬렉션으로 변경한다.
    - 만약 getOrders() 처럼 임의의 메서드에서 컬력션을 잘못 생성하면 하이버네이트 내부 메커니즘에 문제가 발생할 수 있다.
    - 따라서 필드레벨에서 생성하는 것이 가장 안전하고, 코드도 간결하다.

- 테이블, 컬럼명 생성 전략
    - 스프링 부트에서 하이버네이트 기본 매핑 전략을 변경해서 실제 테이블 필드명은 다름
    - https://docs.spring.io/spring-boot/docs/2.1.3.RELEASE/reference/htmlsingle/#howto-configure-hibernate-naming-strategy 
    - http://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/ Hibernate_User_Guide.html#naming
    - 하이버네이트 기존 구현: 엔티티의 필드명을 그대로 테이블 명으로 사용 ( SpringPhysicalNamingStrategy )

- 스프링 부트 신규 설정 (엔티티(필드) 테이블(컬럼))
    - 1. 카멜 케이스 언더스코어(memberPoint member_point) 
    - 2. .(점) _(언더스코어)
    - 3. 대문자 소문자

- 논리명 생성
    - 명시적으로 컬럼, 테이블명을 직접 적지 않으면 ImplicitNamingStrategy 사용
    - 테이블이나, 컬럼명을 명시하지 않을 때 논리명 적용
```yaml
# spring.jpa.hibernate.naming.implicit-strategy
spring.jpa.hibernate.naming.implicit-strategy: 
  org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy
```

- 물리명 적용
    - 모든 논리명에 적용됨, 실제 테이블에 적용 (username usernm 등으로 회사 룰로 바꿀 수 있음)
```yaml
# spring.jpa.hibernate.naming.physical-strategy
spring.jpa.hibernate.naming.physical-strategy:
  org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
```
 

# 회원 도메인, 레포지토리, 서비스 개발 및 테스트

- 구현 기능
    - 회원 등록 회원 목록 조회
- 순서
    - 회원 엔티티 코드 다시 보기
    - 회원 리포지토리 개발
    - 회원 서비스 개발
    - 회원 기능 테스트

- 생성자 주입 방식을 권장
    - 변경 불가능한 안전한 객체 생성 가능
    - 생성자가 하나면, @Autowired 를 생략할 수 있다.
    - final 키워드를 추가하면 컴파일 시점에 memberRepository 를 설정하지 않는 오류를 체크할 수 있다. 
      (보통 기본 생성자를 추가할 때 발견)

## [Test Code 작성 방법](https://martinfowler.com/bliki/GivenWhenThen.html)

- 스프링 부트는 datasource 설정이 없으면, 기본적을 메모리 DB를 사용하고, driver-class도 현재 등록된 라이브러를 보고 찾아준다. 
  추가로 ddl-auto 도 create-drop 모드로 동작한다. 따라서 데이터소스나, JPA 관련된 별도의 추가 설정을 하지 않아도 된다.

# 상품 도메인, 레포지토리, 서비스 개발 및 테스트
- 구현 기능
    - 상품 등록 
    - 상품 목록 조회 
    - 상품 수정
- 순서
    - 상품 엔티티 개발(비즈니스 로직 추가)
    - 상품 리포지토리 개발
    - 상품 서비스 개발
    - 상품 기능 테스트


# 주문 도메인, 레포지토리, 서비스 개발 및 테스트
- 구현 기능
    - 상품 주문 
    - 주문 내역 조회 
    - 주문 취소
- 순서
    - 주문 엔티티, 주문상품 엔티티 개발
    - 주문 리포지토리 개발
    - 주문 서비스 개발
    - 주문 검색 기능 개발
    - 주문 기능 테스트

- 서비스 계층 은 단순히 엔티티에 필요한 요청을 위임하는 역할을 한다.
    - 엔티티가 비즈니스 로직을 가지고 객체 지 향의 특성을 적극 활용하는 것을 [도메인 모델 패턴](https://martinfowler.com/eaaCatalog/domainModel.html)
    - 엔티티에는 비즈니스 로직이 거의 없고 서비스 계층에서 대부분 의 비즈니스 로직을 처리하는 것을 [트랜잭션 스크립트 패턴](https://martinfowler.com/eaaCatalog/transactionScript.html)

# 웹 계층 개발
- 홈 화면 
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
