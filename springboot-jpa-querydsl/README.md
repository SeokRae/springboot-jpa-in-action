# 실전 QueryDSL

## 프로젝트 환경 설정

- QueryDSL 설정
  - build.gradle 설정
  - compileQuerydsl 
  - `gitignore`에 추가

- QueryDSL 검증
  - sample `entity` 생성 및 테스트 코드 작성
  - `compileQuerydsl` 을 통해 `build`된 Q`Entity Class명`.java 파일 확인
    - gradle console 사용방법
      ```shell
      ./gradlew clean compileQuerydsl
      ```
  - Querydsl QType이 정상 동작하는지 확인


## 기본 문법

> JPQL vs Querydsl
- JPA 코드 기반 조회쿼리 작성
- Querydsl 기반 조회쿼리 작성
  - `EntityManager`로 `JPAQueryFactory` 클래스를 생성
  - queryFactory를 통해 작성한 쿼리에 QType 클래스를 파라미터로 조회
  - Querydsl은 JPQL Builder의 역할

- JPA vs Querydsl 차이

  |방식|JPQL|Querydsl|
  |:---|:---|:---|
  |작성방식|문자|코드|
  |오류체크시점|실행 시점 오류|컴파일 시점 오류|
  |파라미터 바인딩 방식|파라미터 바인딩|파라미터 바인딩 자동 처리|


> 기본 Q-Type 활용
- Q 클래스 인스턴스를 사용하는 2가지 방법
  - alias 직접 설정
  - 기본 인스턴스 사용
    - 같은 테이블을 조인해야 하는 경우가 아니면 기본 인스턴스를 사용

- 개발 시 JPQL 쿼리 로그 확인 방법
  ```yml
  spring.jpa.properties.hibernate.use_sql_comments: true
  ```

> [검색 조건 쿼리](http://www.querydsl.com/static/querydsl/4.4.0/apidocs/)
- `queryFactory`의 `where`메서드에 검색 조건 `.and()`, `.or()`을 사용할 수 있다.
- `.select()`, `.from()` -> `selectFrom()` 으로 축약 가능
- where() 메서드의 경우 predicate ... 를 파라미터로 받아 AND 로 처리하는 것을 기본값으로 한다.
  - 이 경우 null 값은 무시하게 되고 이러한 방식을 이용하여 메서드 추출을 활용하여 동적쿼리를 깔끔하게 만들 수 있다.

> 결과 조회 메서드 종류

- 결과 조회 메서드

  |메서드명|설명|예외|
  |:---:|:---|:---|
  |`fetch()`|리스트 조회|데이터가 없는 경우 빈 리스트 반환|
  |`fetchOne()`|단 건 조회|결과가 없는경우: `null`, 결과가 둘 이상인 경우: `com.querydsl.core.NonUniqueResultException` 예외|
  |`fetchFirst()`|가장 상위 데이터를 조회, `limit(1).fetchOne()` 과 동일||
  |`fetchResults()`|페이징 정보 포함, total count 쿼리 추가 실행||
  |`fetchCount()`|count 쿼리로 변경해서 count 조회||

- 주의사항
  - `fetchResults()` 메서드가 페이징 정보까지 포함하지만, 페이징 쿼리에 부하가 걸리는 경우 `fetchCount()`를 따로 날리는 것이 효율적

> 정렬
- `orderBy()`
  - `desc()`, `asc()`: 일반정렬
  - `nullsLast()`, `nullsFirst()`: null 데이터 순서 부여

> 페이징
- `offset()`, `limit()`
  - `offset()`: 기본 0부터 시작
  - `limit()`: 조회할 데이터 건수
  - count 쿼리가 실행되므로 성능상 주의
  
- 주의사항
  - 데이터를 조회해야하는 쿼리는 여러 테이블을 조인해야하지만 count 쿼리는 조인이 필요없는 경우가 있다.
  - 자동화된 count 쿼리는 원본 쿼리와 같이 모두 조인하기 때문에 성능이 안나올 수 있다.
  - count 쿼리에 조인이 필요없는 성능 최적화가 필요한 경우 count 전용 쿼리를 별도로 작성할 필요성이 있다.

> [집합](http://www.querydsl.com/static/querydsl/4.4.0/apidocs/)
- aggregation
  - sum, avg, max, min과 같은 집계성 쿼리

- Tuple
  - querydsl이 제공하는 Tuple 클래스
  - 데이터의 타입에 상관없이 저장 및 조회 가능
  - 실무에서는 Tuple 대신 DTO로 바로 저장할 수 있게끔 사용하는 방식을 선호한다.

- `groupBy()`, `having()`
  - 데이터 셋에 따른 테스트를 많이 해봐야 할 듯

> 조인 - 기본 조인
- 첫 번쨰 파라미터에 조인 대상을 지정, 두 번째 파라미터에 별칭으로 사용할 Q 타입을 지정

- 조인의 종류
  - `join()`, `innerJoin()`: `inner join` 기능
  - `leftJoin()`: `left outer join` 기능
  - `rightJoin()`: `right outer join` 기능
  - `theta join`: from 절에 여러 엔티티를 선택하는 방법 
    - 이 방법만으로는 outer join이 불가능
    - `join().on()`을 통해 해결 가능

> 조인 - on 절
- on()을 활용한 조인
  - join 대상 필터링
  - 연관관계 없는 엔티티 외부 조인

- Join 대상 필터링
  - `leftJoin(member.team, team).on(team.name.eq("teamA"))`
  - on 절을 활용해 조인 대상을 필터링 할 때, 외부조인이 아니라 내부조인을 사용하면 where절에서 필터링하는 것과 기능이 동일하다.
  - 따라서 on절을 활용한 조인 대상 필터링을 사용할 때, 내부조인이면 익숙한 where 절로 해결하고, `정말 외부조인이 필요한 경우에만 이 기능을 사용`

- 연관관계 없는 엔티티 외부 조인
  - `.leftJoin(team).on(member.username.eq(team.name))`
    - `leftJoin()` 부분에 일반 조인과 다르게 엔티티가 하나만 들어가는 것을 주의
  - hibernate 5.1 부터 on을 사용한 서로 관계가 없는 필드로 내(외)부 조인하는 기능이 추가됨

- on() 절 쿼리 및 연관관계 없는 엔티티 외부조인 정리
  - inner join 활용 시: `join().where(condition)` 권장
  - outer join 사용 시: `join().on()` 권장
  - `leftJoin()`에 엔티티 하나만 들어가는 것을 확인

- 조인 비교
  - 일반 조인: `leftJoin(member.team, team)`
  - on 조인: `from(member).leftJoin(team).on(..)`

> 조인 - Fetch Join
- 성능 최적화를 위해 사용하는 방법
- Fetch Join 유무 비교
  - Fetch Join 미적용
    - 지연로딩을 이용하여 연관관계 엔티티를 각각 조회 
  - Fetch Join 적용
    - 즉시로딩으로 연관관계 엔티티를 한 번에 조회
    - `join()`, `leftJoin()` 등 조인 기능 뒤에 `fetchJoin()` 이라고 추가

- 정리
  - 활용2편의 내용을 참고
  - 자주 사용되는 내용

> 서브쿼리
- `com.querydsl.jpa.JPAExpressions` 사용
  - 서브 쿼리 eq
  - 서브 쿼리 goe
  - 서브 쿼리 여러건 처리 in
  - select 절 subquery

- from 절의 서브쿼리 한계
  - JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(`인라인 뷰`)는 **지원하지 않는다.**
  - 당연히 `Querydsl`도 **지원하지 않는다.**
  - `하이버네이트` 구현체를 사용하면 select 절의 서브쿼리는 지원한다.
  - `Querydsl`도 하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.

- **[중요] from 절의 서브쿼리 해결방안**
  - 서브쿼리를 join으로 변경한다.
    (가능한 상황도 있고, 불가능한 상황도 있다.)
  - 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
  - `NativeSQL`을 사용한다.

- 인라인 뷰의 문제점
  - 데이터를 화면에 맞추려는 쿼리가 인라인 뷰를 복잡하게 하는 경우가 있다.
  - 데이터에 집중하면 인라인 뷰를 많이 줄일 수 있다.
  - 한번에 조회되는 쿼리가 정말 중요한지? 생각해보기

> Case 문
- select, 조건절(where), order by에서 사용 가능
  - 단순한 조건
    - `when().then()`
  - 복잡한 조건
    - `new CaseBuilder().when().then().otherwise()`

- 정리
  - case 같은건 application에서 처리하는게 좋지 않을까 생각하기

> 상수, 문자 더하기
- 상수 더하기
  - `Expressions.constant(xxx)`
  - 쿼리는 기본 쿼리가 날아가고 조회된 결과 데이터에 추가 됨
  
- 문자 더하기
  - `concat()`
  - 문자가 아닌 다른 타입들은 `stringValue()`로 문자로 변환가능
  - enum 타입 같은경우에 활용할 수 있음

## 중급 문법
> 프로젝션과 결과 반환
- 기본
  - select 대상 지정: **프로젝션 대상이 하나**
    - 프로젝션 대상이 하나면 타입을 명확하게 지정할 수 있음
  - 튜플 조회: **프로젝션 대상이 둘 이상**
    - `com.querydsl.core.Tuple`
    - 프로젝션 대상이 둘 이상이면 튜플이나 DTO로 조회
  
- DTO 조회
  - 순수 JPA에서 DTO 조회
    - 순수 JPA에서 DTO를 조회할 때는 new 명령어를 사용해야한다.
    - DTO의 package 이름을 다 적어줘야 한다.
    - 생성자 방식만 지원한다.

  - Querydsl 빈 생성
    - 결과를 DTO 반환 시 사용
    - 프로퍼티 접근
    - 필드 직접 접근
    - 생성자 사용
  
- @QueryProjection
  - 생성자 + @QueryProjection
  - @QueryProjection 활용
    - 컴파일러로 타입을 체크할 수 있으므로 가장 안전한 방법
    - DTO에 QueryDSL 어노테이션을 유지해야 하는 점과 DTO까지 Q 파일을 생성해야 하는 단점

> 동적쿼리
- 동적 쿼리를 해결하는 두 가지 방법

- BooleanBuilder 사용
- Where 다중 파라미터 사용
  - where 조건에 null 값은 무시된다.
  - 메서드를 다른 쿼리에서도 재활용할 수 있다.
  - 쿼리 자체의 가독성이 높아진다.

> 수정, 삭제 벌크 연산
- `execute()` 메서드 사용 시 bulk 쿼리 호출
- 주의사항
  - 영속성 컨텍스트에 있는 엔티티를 무시하고 실행되기 때문에 배치 쿼리를 실행하고 나면 영속성 컨텍스트를 초기화 하는 것이 안전

> SQL function 호출
- 주의사항
  - SQL function은 JPA와 같이 Dialect에 등록된 내용만 호출할 수 있다.

## 실무 활용 - 순수 JPA와 Querydsl

> 순수 JPA 리포지토리와 Querydsl

- JPAQueryFactory 스프링 빈 등록
  - JPAQueryFactory 를 스프링 빈으로 등록해서 주입받아 사용가능
  - 스프링이 주입해주는 엔티티 매니저는 실제 동작 시점에 진짜 엔티티 매니저를 찾아주는 프록시용 가짜 엔티티 매니저이므로 동시성 문제는 걱정할 필요 없다.
  - 가짜 엔티티 매니저는 실제 사용 시점에 트랜잭션 단위로 실제 엔티티 매니저(영속성 컨텍스트)를 할당해준다.

> 동적 쿼리와 성능 최적화 조회
- Builder 사용
- Where 절 파라미터 사용

> 조회 API 컨트롤러 개발

## 실무 활용 - 스프링 데이터 JPA와 Querydsl
> 스프링 데이터 JPA 리포지토리로 변경
> 사용자 정의 리포지토리
> 스프링 데이터 페이징 활용
- Querydsl 페이징 연동
- CountQuery 최적화
- 컨트롤러 개발

## 스프링 데이터 JPA가 제공하는 Querydsl 기능
> 인터페이스 지원
- QuerydslPredicateExecutor

> Querydsl Web 지원
> 리포지토리 지원
- QuerydslRepositorySupport
> Querydsl 지원 클래스 직접 만들기

