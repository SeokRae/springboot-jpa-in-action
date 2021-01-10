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

## 공통 인터페이스 기능

## 쿼리 메서드 기능

## 확장 기능

## 스프링 데이터 JPA 분석

## 나머지 기능들

