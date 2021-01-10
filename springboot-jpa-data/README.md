# 실전! 스프링 데이터 JPA

## 프로젝트 환경설정

> H2 데이터베이스 설치
- 라이브러리에 맞는 버전을 설치

- db 파일로 접근
    - url: jdbc:h2:~/datajpa
    - 이 방법은 직접적으로 파일에 접근했기 떄문에 lock이 걸려 한 번에 여러 곳에서 접근할 수 없음
- 원격으로 접근
    - url: jdbc:h2:tcp://localhost/~/datajpa

> 스프링 데이터 JPA와 DB 설정, 동작확인
- application.yml 설정
- 기본 엔티티 생성 및 테스트 코드 작성
- jpa vs spring-data-jpa 맛보기 코드비교

## 예제 도메인 모델
- Member, Team 생성 및 연관관계 설정
- 생성자 레벨 Protected로 설정
- ToString은 연관관계가 설정되어 있지 않은 필드만 출력

## 공통 인터페이스 기능

> 순수 JPA 기반 리포지토리 만들기
- 기본 CRUD
  - 저장
  - 변경 > 변경감지 사용 
  - 삭제
  - 전체 조회
  - 단건 조회 
  - 카운트

- 참고
  - JPA에서 수정은 변경감지 기능을 사용
  - 트랜잭션 안에서 엔티티를 조회한 다음에 데이터를 변경하면, 트랜잭션 종료 시점에 변경감지 기능이 작동해서 변경된 엔티티를 감지하고 UPDATE SQL을 실행

- 확인사항
  - JPA 코드로 기본 CRUD 작성 및 테스트
  - 지연로딩을 이용한 수정 테스트

> 스프링 데이터 JPA 공통 인터페이스 설정
- 스프링부트 사용시 `@SpringBootApplication` 위치를 지정(해당패키지와 하위 패키지 인식) 
- 만약 위치가 달라지면 `@EnableJpaRepositories` 필요
- `org.springframework.data.repository.Repository` 를 구현한 클래스는 스캔 대상 
   - `MemberRepository` 인터페이스가 동작한 이유
   - 실제 출력해보기(Proxy)
   - `memberRepository.getClass()` class `com.sun.proxy.$ProxyXXX`

- `@Repository` 애노테이션 생략 가능
  - 컴포넌트 스캔을 스프링 데이터 JPA가 자동으로 처리 
  - JPA 예외를 스프링 예외로 변환하는 과정도 자동으로 처리

> 스프링 데이터 JPA 공통 인터페이스 적용
- 위 JPA 코드로 작성한 내용을 Spring-data-jpa에서 제공하는 `JpaRepository`를 사용하여 구현
- `JpaRepository<T, ID>`
  - `T`: 엔티티 타입
  - `ID`: 식별자 타입(PK)

> 스프링 데이터 JPA 공통 인터페이스 분석
- JpaRepository
  - data.jpa 패키지 하위에 존재하는 인터페이스
  - 대부분의 공통 메서드를 제공

- PagingAndSortingRepository
  - data 패키지 하위에 존재하는 인터페이스

- 수정 사항 확인
  - `T findOne(ID)` -> `Optional<T> findById(ID)` 변경
  
- 제네릭 타입 확인
  - `T`: 엔티티
  - `ID`: 엔티티의 식별자 타입
  - `S`: 엔티티와 그 자식 타입

- 주요 메서드

  |메서드명|설명|
  |:---|:---|
  |`save(S)`|새로운 엔티티는 저장하고 이미 있는 엔티티는 병합한다.|
  |`delete(T)`|엔티티 하나를 삭제한다. 내부에서 `EntityManager.remove()` 호출|
  |`findById(ID)`|엔티티 하나를 조회한다. 내부에서 `EntityManager.find()` 호출|
  |`getOne(ID)`|엔티티를 프록시로 조회한다. 내부에서 `EntityManager.getReference()` 호출|
  |`findAll(...)`|모든 엔티티를 조회한다. 정렬(`Sort`)이나 페이징(`Pageable`) 조건을 파라미터로 제공할수 있다.|

## 쿼리 메서드 기능
- 여러 기능
  - 메소드 이름으로 쿼리 생성
  - NamedQuery
  - @Query - 리파지토리 메소드에 쿼리 정의 파라미터 바인딩
  - 반환 타입
  - 페이징과 정렬
  - 벌크성 수정 쿼리
  - @EntityGraph

- 쿼리 메소드 기능 3가지
  - 메소드 이름으로 쿼리 생성 
  - 메소드 이름으로 JPA NamedQuery 호출
  - @Query 어노테이션을 사용해서 리파지토리 인터페이스에 쿼리 직접 정의

> 메소드 이름으로 쿼리 생성
- 메서드 이름을 분석하여 JPQL 쿼리를 생성하고 실행
- [쿼리 메소드 필터 조건](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation)
- [스프링 데이터 JPA가 제공하는 쿼리 메소드 기능](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.details)
  - [조회](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-creation)
  - [limit](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.limit-query-result)

- 주의
  - 엔티티의 필드명이 변경되면 인터페이스에 정의한 메서드 이름도 꼭 함께 변경해야 한다.
  - 그렇지 않으면 애플리케이션을 시작하는 시점에 오류가 발생한다.
  - 애플리케이션 로딩 시점에 오류를 인지할 수 있는 것이 스프링 데이터 JPA의 매우 큰 장점이다.

> JPA NamedQuery
- `@NamedQuery` 어노테이션으로 Named 쿼리 정의
- JPA기반 쿼리 테스트
- Spring Data JPA 기반 쿼리 테스트
  - @Query를 생략하고 메서드 이름만으로 Named쿼리를 호출할 수 있다.

- 스프링 데이터 JPA는 선언한 `도메인 클래스 + .(점) + 메서드 이름`으로 Named 쿼리를 찾아서 실행 
- 만약 실행할 Named 쿼리가 없으면 메서드 이름으로 쿼리 생성 전략을 사용한다.
- 필요하면 전략을 변경할 수 있지만 권장하지 않는다.
  - [참고](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods.query-lookup-strategies)

- 주의
  - 스프링 데이터 JPA를 사용하면 실무에서 `NamedQuery`를 직접 등록해서 사용하는 일은 드물다. 
  - 대신 `@Query`를 사용해서 리파지토리 메소드에 쿼리를 직접 정의한다.

> @Query, 리포지토리 메소드에 쿼리 정의하기
- @org.springframework.data.jpa.repository.Query 어노테이션을 사용
- 실행할 메서드에 정적 쿼리를 직접 작성하므로 이름 없는 Named 쿼리라 할 수 있음
- JPA Named 쿼리처럼 애플리케이션 실행 시점에 문법 오류를 발견할 수 있음(매우 큰 장점!)
- 실무 환경
  - 메서드 이름으로 쿼리 생성 기능은 파라미터가 증가하면 메서드 이름이 지저분해진다.
  - 따라서 @Query 기능을 더 선호한다.

- 단순 값 하나 조회
  - JPA 값 타입 `@Embedded`도 이 방식으로 조회가 가능

- DTO로 직접 조회
  - DTO로 직접 조회 하려면 JPA의 new 명령어를 사용
  - 생성자가 맞는 DTO가 필요
  
- 파라미터 바인딩
  - 코드 가독성과 유지보수를 위해 이름 기반 파라미터 바인딩을 사용

- 컬렉션 파라미터 바인딩
  - Collection 타입으로 in절 지원

> [반환타입](https://docs.spring.io/spring-data/jpa/docs/current/reference/ html/#repository-query-return-types)
- 조회 결과 값에 따른 방식
  - 컬렉션
    - 결과 없음 -> 빈 컬렉션
  - 단건 조회
    - 결과 없음 -> null
    - 결과 2건 이상: `javax.persistence.NonUniqueResultException` 예외 발생

- 참고
  - `단건`으로 지정한 메서드를 호출하면 스프링 데이터 JPA는 내부에서 JPQL의 `Query.getSingleResult()` 메서드를 호출한다. 
  - 이 메서드를 호출했을 때 조회 결과가 없으면 `javax.persistence.NoResultException` 예외가 발생하는데 개발자 입장에서 다루기가 상당히 불편하다.
  - 스프링 데이터 JPA는 단건을 조회할 때 이 예외가 발생하면 예외를 무시하고 대신에 `null` 을 반환한다.

> 순수 JPA 페이징과 정렬

## 확장 기능

## 스프링 데이터 JPA 분석

## 나머지 기능들

