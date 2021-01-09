# 인프런 JPA 관련 프로젝트 공부

## [데이터 모델링 및 연관관계 설정](springboot-jpa-shop/README.md)
- 기능 목록
  - 회원 기능
    - [x] 회원 등록
    - [x] 회원 조회
  - 상품 기능
    - [x] 상품 등록
    - [x] 상품 수정 (영속성 관리 개념)
    - [x] 상품 조회
  - 주문 기능
    - [x] 상품 주문
    - [x] 주문 내역 조회
    - [x] 주문 취소
  - 기타 요구사항
    - [x] 상품은 재고 관리가 필요하다.
    - [x] 상품의 종류는 도서, 음반, 영화가 있다. 상품을 카테고리로 구분할 수 있다.
    - [x] 상품 주문시 배송 정보를 입력할 수 있다.

## [최적화 내용](springboot-jpa-shop-optimize/README.md)

- 최적화 목록
  - [x] 회원 등록
    - `Entity` 사용하여 발생하는 문제를 확인
  - [x] 회원 등록
    - API 응답 값으로 `Entity`를 노출하지 않도록 로직을 구분
    - DTO 사용하여 Presentation 계층과 Repository 계층을 구분
  - [x] 회원 수정 
    - `RequestBody`용 `Request DTO`를 작성 
    - `Response`값으로 어떤 값을 넘겨 줄지는 사이트 마다 다름
  - [x] 회원 조회 
    - `RequestBody`에 `Entity`를 직접 사용하여 웹 계층에 모든 값이 노출되는 문제 확인
    - 연관 관계가 걸려있는 부분이 서로 조회하여 무한루프에 빠지는 문제 확인
  - [x] 회원 조회
    - `Response`를 위한 `Wrapper Class`를 작성하여 API 스펙을 정의하는 방법 확인

  - [x] 주문 조회
    - xToOne 관계일 때 지연로딩에 대한 최적화 방법
    - 방법 1
      - `Hibernate5Module`를 사용하여 proxy 객체를 초기화 했을때 객체를 조회
      - 또는 `@JsonIgnore`를 사용하여 연관관계가 있는 엔티티 한 부분에 적용하여 무한루프에 빠지지 않도록 설정
    - 방법 2
      - `Entity` 대신 `DTO`를 작성하여 지연로딩 사용
    - 방법 3
      - `Fetch Join`으로 연관관계를 조회하는 방법, 성능 최적화는 대부분 `Fetch Join`으로 해결 가능 확인
    - 방법 4
      - `JPA`에서 `DTO`로 바로 조회하는 방법
    - 방법 5
      - `JPA`에서 `DTO`를 직접 조회하되 컬렉션 조회를 최적화하기 위해 `xToOne` 연관관계에 `Fetch Join`활용
    - 방법 6
      - `JPA`에서 `DTO`를 직접 조회하되 `SQL` 쿼리 한 번에 전체 조회를 할 수 있도록 처리

- 정리
  - 각 방식마다 장단점이 분명하게 차이가 있기때문에 현재 상황이 어떤지 확인하고 맞춰서 사용할 것

## Spring-Data-Jpa


## 단축키 꿀팁 모음
- 단축키 shift + command + c
    - 해당 폴더 경로 복사: 터미널에 복사해서 빠르게 이동
- reCompile shift + command + F9
    - Thymeleaf 사용시 recompile로 refresh (devtools dependency 필요)
