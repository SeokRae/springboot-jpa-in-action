# JPA API 개발과 성능 최적화
- 실무 성능 문제를 해결하기 위한 내용

## API 개발 기본
- 페이지 랜더링 Controller와 API Controller를 구분하기

### 회원 등록 API
> 회원 등록 개발 시 요청 값으로 엔티티를 반환 [saveMemberV1()](/src/main/java/kr/seok/shop/api/MemberApiController.java)
- **위 회원 등록 로직의 문제점**
    - 회원 등록에 대한 간단한 로직을 작성하고 생성된 값을 반환
    - 위 경우의 문제점은 `Presentation 계층`에 대한 로직이 `Entity`에 추가가 된다는 점
    - 실무에서 회원 엔티티를 위한 API가 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 모든 요구사항을 담기는 어렵다.
    - Presentation 계층의 로직이 엔티티에 추가되어 엔티티가 변경되면 API 스펙이 변경되는 불상사가 발생할 가능성이 있다.

**결론: API 요청 스펙이 맞는 별도의 DTO 를 파라미터로 받아야 한다.**

> 회원등록 개발 시 요청 값으로 DTO를 RequestBody에 매핑 [saveMemberV2()](/src/main/java/kr/seok/shop/api/MemberApiController.java)
- `CreateMemberRequest`를 `Member`엔티티 대신에 RequestBody와 매핑시 장점
    - `Entity`와 `Presentation 계층`을 위한 로직을 분리할 수 있다.
    - `Entity`와 API 스펙을 명확하게 분리할 수 있다.
    - `Entity`가 변해도 API 스펙이 변하지 않는다.

**결론: 실무에서 Entity를 API 스펙에 노출하면 안된다.**

### 회원 수정 API
> 회원 수정 개발 시 요청 값으로 DTO를 RequestBody에 매핑 [updateMemberV2()](/src/main/java/kr/seok/shop/api/MemberApiController.java)
- 위 회원 등록과 마찬가지로 Entity 대신 DTO를 사용하여 구현
    - 요청 값에 대한 파라미터를 DTO로 정의
    - 응답 값은 엔티티 자체를 보내는 것이 아니라 수정된 회원엔티티의 `회원ID`와 `회원명`을 반환
    
**[팁]** RESTful API의 경우 수정 요청 Method는 PATCH 또는 POST를 사용해야한다.

### 회원 조회 API
> 회원 조회 개발 시 응답 값으로 `Entity`를 직접 외부에 노출 [membersV1()](/src/main/java/kr/seok/shop/api/MemberApiController.java)
- **위 회원 조회 로직의 문제점**
    - Entity에 Presentation 계층을 위한 로직이 추가된다.
    - 기본적으로 엔티티의 모든 값이 노출된다.
    - 응답 스펙을 맞추기 위해 로직이 추가된다. (@JsonIgnore, 별도의 뷰 로직 등등)
    - 실무에서는 같은 엔티티에 대해 API가 용도에 따라 다양하게 만들어지는데, 한 엔티티에 각각의 API를 위한 프레젠테이션 응답 로직을 담기는 어렵다.
    - 엔티티가 변경되면 API 스펙이 변한다.
    - 추가로 컬렉션을 직접 반환하면 향후 API 스펙을 변경하기 어렵다.
      (별도의 Result 클래스 생성으로 해결하는 것이 필요)

**결론: API 응답 스펙에 맞추어 별도의 DTO를 반환한다.**

> 회원 조회 개발 시 응답 값으로 `Entity`를 별도의 `DTO`로 사용하는 경우 [membersV2()](/src/main/java/kr/seok/shop/api/MemberApiController.java)
- **회원 조회 로직의 장점**
    - `Entity`를 DTO로 변환하여 반환하는 경우 Entity가 변해도 API 스펙이 변하지 않는다.
    - 공통 응답 값을 반환하는 `Wrapper Class`를 작성하여 요청 데이터 외에 추가 필드를 포함하는 것이 가능하다.
    
    ```java
    /* 제네릭 형태로 어떤 타입의 형태든 담을 수 있다. */
    class Result<T> {
        private int count;
        private T data;
    }
    ```

## API 개발 고급 - 준비
어떤 기능을 개발할 때, 문제가 될 수 있는 기능은 `Create`, `Update`, `Delete`의 문제보다 `Select`에서 문제가 발생할 가능성이 높다.

### 조회용 샘플 데이터 입력
> 회원, 상품, 주문상품, 주문에 대한 개발 사이클을 테스트 할 수 있는 데이터 입력 [initDb](src/main/java/kr/seok/shop/InitDb.java)
- 해당 클래스는 @Component로 등록하여 ComponentScan을 통해 실행 시 읽을 수 있도록 한다.
- option + command + m (extract method) 인텔리제이 단축키를 통해 중복되는 코드를 제거

### API 개발 고급 - 지연 로딩과 조회 성능 최적화 (엄청엄청 중요)
- 주문 + 배송정보 + 회원 조회시 지연로딩으로 인하여 발생하는 성능 문제점을 단계별로 개선
- **xToOne(ManyToOne, OneToOne) 관계 최적화**

> 엔티티를 직접 반환하는 잘못된 방법 [ordersV1()](src/main/java/kr/seok/shop/api/OrderSimpleApiController.java)
- `Order와 Member`, `Order와 Address`는 지연로딩 설정되어 있다.
- 위 설정 시 `API`를 호출하여 데이터를 조회할 때 xToOne 설정으로 관계가 되어 있는 클래스는 프록시 객체로 설정되게 되는데 
  jackson 라이브러리가 이 프록시 객체를 json으로 변환을 할 수 없어 예외가 발생한다.
- 이때 단순한 방법으로 이 문제를 해결하기 위해서는 [Hibernate5Module](src/main/java/kr/seok/ShopOptimizeApplication.java)을 스프링 빈으로 등록하면 해결이 가능하다.
- 라이브러리를 사용해서 빈으로 등록 시, 초기화되지 않은 프록시 객체는 노출하지 않고 초기화된 객체만 노출하게 된다. 
    - 하지만 이는 근본적인 해결책이 아니다.
- 강제로 지연로딩 설정하는 할 수도 있으나 이때 연관관계 엔티티를 확인하여 한쪽에 `@JsonIgnore`를 설정해줘야 양방향으로 계속 읽는 문제가 발생하지 않는다. 

>> **[중요]** 지연로딩을 피하기 위해 EAGER로 설정하면 안된다. 연관관계가 필요없는 경우에도 데이터를 항상 조회하여 성능 문제가 발생할 수 있다.

>> **[중요]** 항상 지연로딩을 기본으로 하고, 성능 최적화가 필요한 경우 Fetch Join을 사용하면 어느정도의 성능 문제를 해결할 수 있다.

> 엔티티를 조회하여 DTO로 변환 [ordersV2()](src/main/java/kr/seok/shop/api/OrderSimpleApiController.java)
- 일반적인 방법
- 사실상 개선되지 않은 방식
- 쿼리가 v1과 같이 1(order) + N(member) + N(address) 번 실행된다.
- N은 지연로딩
- Lazy 초기화로 Cache에 없으면 DB조회 후 값을 세팅
```java
@Data
static class SimpleOrderDto {
    // fields 설정
    public SimpleOrderDto(Order order) {
        orderId = order.getId();
        name = order.getMember().getName(); // Lazy 초기화로 Hibernate5Module이 값을 세팅
        orderDate = order.getOrderDate();
        orderStatus = order.getStatus();
        address = order.getDelivery().getAddress(); // Lazy 초기화로 Hibernate5Module이 값을 세팅
    }
}
```

> 엔티티를 DTO로 변환 - Fetch Join Optimization [ordersV3()](src/main/java/kr/seok/shop/api/OrderSimpleApiController.java)
- `join fetch`를 통해 `Entity`를 객체 그래프로 묶어 쿼리 한 번에 조회 [findAllWithMemberDelivery()](src/main/java/kr/seok/shop/domain/repository/OrderRepository.java)
- `Fetch Join`으로 `Order > Member`, `Order > Delivery`는 이미 조회된 상태이므로 지연로딩되지 않는다.
- Fetch Join에 대한 내용은 가볍지 않기 때문에 추가적인 학습 필요하다.
- 엔티티 그대로를 호출하여 필요한 필드에 대한 데이터 가공이 가능하다.

> JPA에서 DTO로 바로 조회 [ordersV4()](src/main/java/kr/seok/shop/api/OrderSimpleApiController.java)
- 일반적인 SQL을 사용할 때처럼 `원하는 값을 선택하여 조회`가 가능하다.
- `new` 명령어를 사용하여 JPQL의 결과를 DTO로 즉시 변환한다.
- SELECT 절에서 원하는 데이터를 직접 선택하므로 DB -> Application 네트워크 용량 최적화 (생각보다 미비)
- 레포지토리의 재사용성이 떨어짐, API 스펙에 맞춘 코드가 레포지토리에 들어가는 단점 [OrderSimpleQueryRepository](src/main/java/kr/seok/shop/domain/repository/OrderSimpleQueryRepository.java)

> 네 가지 버전의 정리
- 엔티티를 DTO로 변환(v3)하거나 DTO로 바로 조회(v4)하는 두 가지 방법은 각각 장단점이 있다.
- 둘 중 상황에 따라 더 나은 방법을 선택하면 된다.
- 엔티티로 조회하면 레포지토리 사용성도 좋고, 개발도 단순해진다.

> **[중요] 쿼리 방식 선택 권장 순서**

1. 우선 엔티티를 DTO로 변환하는 방법을 선택
2. 필요하면 Fetch Join으로 성능 최적화한다. (성능 이슈가 보통 이 부분에서 해결 가능)
3. DTO로 직접 조회하는 방식을 사용한다.
4. 최후의 방법은 JPA가 제공하는 Native SQL이나 스프링 JDBC Template을 사용하여 SQL을 직접 사용한다.

## API 개발 고급 - 컬렉션 조회 최적화
- OneToMany를 조회하고 최적화하는 내용을 정리

> 엔티티 직접 노출 [ordersV1()](src/main/java/kr/seok/shop/api/OrderApiController.java)
- **작성 방식**
  - Hibernate5Module Bean으로 등록 후 Lazy = null 처리
  - 양방향 관계 문제 시 연관관계 한쪽 엔티티의 해당 필드에 @JsonIgnore 설정으로 무한루프 해결
- **문제점**
  - 엔티티가 API에 그대로 노출되는 문제
  
> 엔티티를 DTO로 변환 [ordersV2()](src/main/java/kr/seok/shop/api/OrderApiController.java)
- **작성 방식**
  - 최우선으로 문제되는 `Entity`가 `API`로 노출되는 문제를 `DTO`로 변경
  - 하지만 전 버전과 같이 Lazy의 강제 초기화 처리로 지연로딩으로 호출
- **문제점**
  - order > member ( 1:N )
  - order > address ( 1:N )
  - order > orderItem ( 1:N )
  - order > orderItem > item (1:N:M) 처럼 연관관계가 설정되어있는 만큼 기하급수적으로 조회를 하게 됨
  - 물론 이전에 한 번 로딩한 엔티티가 있다면 캐시내에 존재하고 있기 때문에 SQL 조회는 하지 않으나 효과는 미미함

> 엔티티를 DTO로 변환 - Fetch Join [ordersV3()](src/main/java/kr/seok/shop/api/OrderApiController.java)
- **작성 방식**
  - Controller 로직은 v2와 같으나 Repository 로직이 추가됨
  - `join fetch`라는 명령어를 통해 조회 [findAllWithItem()](src/main/java/kr/seok/shop/domain/repository/OrderRepository.java)
  - Fetch Join으로 SQL 쿼리가 1번만 실행됨
- **`distinct`의 동작 방식**
  - `distinct`를 사용하지 않는 경우 1:N 조인이 있으므로 db row가 증가한다. -> 중복 데이터를 포함 (같은 order 엔티티가 N만큼 조회)
    - 마치 `outer join`과 같이 조회 됨
  - `distinct` 명령어를 추가하면 db에서 동일한 entity가 조회되는 경우 JPA에서 중복을 걸러준다.
- **문제점**
  - 페이징이 불가능

```text
중요 > 해당 부분은 Fetch Join에 대한 개념이 필요 
컬렉션 Fetch Join을 사용하면 페이징이 불가능하다. 
하이버네이트는 WARN 로그를 남기면서 모든 데이터를 DB에서 읽어오고, 메모리에서 페이징을 한다.
(이는 매우 위험한 방식)

Collection Fetch Join은 1개만 사용할 수 있다.
컬렉션 둘 이상에 Fetch Join을 사용하면 안된다.
데이터가 부정합하게 조회될 수 있다.
 
```

> 엔티티를 DTO로 변환 - 페이징과 한계 돌파 [ordersV3_page()](src/main/java/kr/seok/shop/api/OrderApiController.java)
- 이전 버전에서의 문제가 페이징이 불가능하다는 점을 개선하는 버전
  - 컬렉션을 Fetch Join하면 1:N 조인이 발생하므로 **데이터가 예측할 수 없이 증가**한다.
  - 1:N에서 1을 기준으로 페이징을 하는 것이 목적, 그런데 데이터는 N을 기준으로 row가 생성된다.
  - `Order`(1)을 기준으로 페이징 하고 싶은데, `OrderItem`(N)을 조인하면 `OrderItem`이 기준이되어 버린다.
  - **하이버네이트는 WARN 경로를 남기고 모든 DB 데이터를 읽어 메모리에서 페이징을 시도, 최악의 경우 장애로 이어질 수 있다.**

- 한계 돌파
  - 페이징 + 컬렉션 엔티티를 함께 조회하기 위해 현재 이슈를 어떻게 해결해야 할까?
  - 아래 내용을 통해 `페이징 + 컬렉션 엔티티 조회 문제`를 해결할 수 있다.
    - 단순한 코드, 성능 최적화를 얻기 위한 방법

- `페이징 + 컬렉션 엔티티 조회 문제`를 해결책 [findAllWithMemberDelivery()](src/main/java/kr/seok/shop/domain/repository/OrderRepository.java)
  - xToOne(OneToOne, ManyToOne) 관계를 모두 Fetch Join한다.
  - xToOne 관계는 row수를 증가시키지 않으므로 페이징 쿼리에 영향을 주지 않는다.
  - 컬렉션은 지연로딩으로 조회한다.
  - 지연로딩 성능 최적화를 위해 `hibernate.default_batch_fetch_size`, `@BatchSize`를 적용
    - `hibernate.default_batch_fetch_size`: 글로벌 설정
    - `@BatchSize`: 개별 최적화
      - **컬렉션은 컬렉션 필드에, 엔티티는 엔티티 클래스에 적용**
    - 위 두가지 옵션을 통해 컬렉션이나, 프록시 객체를 한꺼번에 설정한 size만큼 IN 쿼리로 조회한다.

- 다시 정리
  - xToOne 관계는 Fetch Join 사용
  - 컬렉션은 LAZY
  - 글로벌 설정은 `hibernate.default_batch_fetch_size`을 기본으로 설정
    - [application.yml](src/main/resources/application.yml)
  - 각 개별 설정으로 `@BatchSize`를 적용
    - [Order.orderItem](src/main/java/kr/seok/shop/domain/Order.java)
    - [Item](src/main/java/kr/seok/shop/domain/Item.java)

- 장점
  - 쿼리 호출 수가 `1 + N` -> `1 + 1`로 최적화
  - 조인보다 DB 데이터 전송량이 최적화 된다.
    (Order와 OrderItem을 조인하면 Order가 OrderItem만큼 중복해서 조회된다.)
  - Fetch Join 방식과 비교하여 쿼리 호출 수가 약간 증가하지만 DB 데이터 전송량이 감소한다.
  - 컬렉션 Fetch Join은 페이징이 불가능하지만 이 방식은 페이징이 가능하다.

- 결론
  - xToOne 관계는 Fetch Join해도 페이징에 영향을 주지 않는다.
  - 따라서 xToOne 관계는 FetchJoin으로 쿼리 수를 줄여 해결하고, 나머지는 `hibernate.default_batch_fetch_size`로 최적화 한다.

```text
[참고] hibernate.default_batch_fetch_size 크기에 관한 내용
100 ~ 1000 사이를 선택하는 것을 권장
이 전략을 SQL IN 절을 사용하는데, 데이터베이스에 따라 IN 절 파라미터를 1000으로 제한하기도 한다.
1000으로 잡으면 한 번에 1000개를 DB에서 애플리케이션에 불러오므로 DB에 순간 부하가 증가할 수 있다.
하지만 애플리케이션은 100이든 100이든 결국 전체 데이터를 로딩해야 하므로 메모리 사용량이 같다.
1000으로 설정하는 것이 성능상 가장 좋지만, 결국 DB든 애플리케이션이든 순간 부하를 어디까지 견딜 수 있으지로 결정한다.

다시 정리
- 조회된 order에 대한 엔티티 아이디를 가지고 orderItem 조회시 in 쿼리를 통해 제한적으로 조회하여 성능상 유리
- 다시 order > orderItem처럼 orderItem > item 조회시 이미 조회된 orderItem 엔티티의 Pk 값을 item 조회시 in 쿼리로 조회하여 성능 최적화 가능
- 1:N:M -> 1:1:1
```

> JPA에서 DTO로 바로조회 - 컬렉션 N 조회 (1 + N Query) [ordersV4()](src/main/java/kr/seok/shop/api/OrderApiController.java)
- 작업 방식
  - Fetch Join으로 한번에 호출 할 수 있는 xToOne 연관관계들을 먼저 조회한 뒤에 ToMany 연관관계들을 별도로 처리
  - ToOne 관계들을은 조인을 해도 데이터 Row 수가 증가하지 않는다. 
    - 때문에 db 데이터 량에서 최적화라 할 수 있음
    - ToMany 관계는 Join으로 최적화하기 어려우므로 별도로 호출
  - order > member, order > delivery 먼저 조회
  - orderId를 통해 orderItem > item 간의 ToOne 연관관계를 조회

- 장점
  - 페이징이 가능해짐, 먼저 order에 대한 페이징후에 컬렉션을 조회하면 됨
  
- 단점이라기보다 더 개선할 수 있는 부분
  - order 쿼리 1번, 컬렉션 N번 실행
  - 이보다 더 최적화 할 수 있는 방법이 있음
  

## 참고
- [Transaction 범위에서 벗어난 준영속상태](https://www.inflearn.com/questions/98643)
- [fetch vs EAGER](https://www.inflearn.com/questions/39516)
- [fetch vs EAGER2](https://www.inflearn.com/questions/30446)
- [JPA Proxy](https://www.inflearn.com/course/ORM-JPA-Basic/lecture/21708?tab=curriculum)
- [default_batch_fetch_size관련 질문](https://www.inflearn.com/questions/34469)
