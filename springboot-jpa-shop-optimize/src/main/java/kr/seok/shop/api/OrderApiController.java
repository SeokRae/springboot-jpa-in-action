package kr.seok.shop.api;

import kr.seok.shop.domain.Address;
import kr.seok.shop.domain.Order;
import kr.seok.shop.domain.OrderItem;
import kr.seok.shop.domain.OrderStatus;
import kr.seok.shop.domain.repository.OrderRepository;
import kr.seok.shop.domain.repository.query.OrderFlatDto;
import kr.seok.shop.domain.repository.query.OrderItemQueryDto;
import kr.seok.shop.domain.repository.query.OrderQueryDto;
import kr.seok.shop.domain.repository.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * <p>
     * - 엔티티가 변하면 API 스펙이 변한다.
     * - 트랜잭션 안에서 지연 로딩 필요
     * - 양방향 연관관계 문제
     * <p>
     * 작성 방식
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생할 가능성이 있기 때문에 양방향 걸린 Entity 찾아서 @JsonIgnore 설정 필요
     * <p>
     * 결론: 이렇게 하면 안됨
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll();
        for (Order order : all) {

            order.getMember().getName(); //Lazy 강제 초기화
            order.getDelivery().getAddress(); //Lazy 강제 초기환

            /* hibernate 설정에서 proxy를 강제 초기화 하여 데이터를 조회하기 위함 */
            List<OrderItem> orderItems = order.getOrderItems();
            /* 아이템 이름을 초기화 */
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제초기화
        }
        return all;
    }

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 트랜잭션 안에서 지연 로딩 필요
     * <p>
     * <p>
     * 문제점
     * - 지연로딩으로 너무 많은 SQL 실행
     * - SQL 실행 횟수
     * - order 1 > member, address N번 > orderItem N번 > item N번
     * <p>
     * 참고
     * - 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL를 실행
     * - 같은 영속성 컨텍스트에서 이미 로딩한 회원 엔티티를 추가로 조회 시 SQL를 실행하지 않음
     * - 하지만 이에 대한 효과는 미미, 개발은 항상 최악의 상황을 고려하여 계산해야 함
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * Entity와의 연결을 오두 끊기 위해 관련 Entity를 모두 DTO로 생성
     */
    @Data
    static class OrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName(); // Lazy Loading으로 member의 name 조뢰
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); // Lazy Loading으로 Delivery의 Address를 조회
            orderItems = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList()); // OrderItem을 DTO로 변환
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;//상품 명
        private int orderPrice; //주문 가격
        private int count; //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경가능) *
     * <p>
     * 엔티티를 DTO로 변롼 - Fetch 조인 최적롸
     * <p>
     * 효과
     * - Fetch Join으로 SQL이 1번만 실행됨
     * - distinct를 사용하면 1:N 조인으로 인하여 db row가 증가되게 되는데 이는 order 엔티티의 조회 수를 증가시킨다. (중복 데이터가 생겨버리는 문제)
     * - JPA의 distinct는 SQL에 distinct를 추가하면 DB 호출에서는 걸러지지 않으나 JPA 쪽으로 넘어와서 걸러진다.
     * - 이 결과로 결론적으로는 distinct 효과를 볼 수 있게 됨
     * <p>
     * 문제점
     * - 페이징이 안되는 심각한 문제
     * - 하이버네이트는 모든 데이터를 DB에서 읽어온뒤 메모리에서 페이징을 하는데 이는 심각한 문제를 발생시킬 수 있다.
     * <p>
     * 참고
     * - 컬렉션 페치 조인은 1개만 사용할 수 있다. -> 이 말은 1:N 의 경우에만 사용해야함, 1:N:N 의 상황에서 사용하면 안됨
     * - 컬렉션 둘 이상에 Fetch Join을 하는 경우 데이터의 정합성이 깨지게 된다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * V3.1 엔티티를 조회해서 DTO로 변환 페이징 고려
     * - 작업 방식
     * - ToOne 관계만 우선 모두 Fetch Join으로 최적화
     * - 컬렉션간 연관관계는 LAZY로 설정
     * - 글로벌 설정은 `hibernate.default_batch_fetch_size`을 기본으로 설정
     * - 각 개별 설정으로 `@BatchSize`를 적용
     * <p>
     * - 장점
     * - 쿼리 호출 수가 `1 + N` -> `1 + 1`로 최적화
     * - 조인보다 DB 데이터 전송량이 최적화 된다.
     * (Order와 OrderItem을 조인하면 Order가 OrderItem만큼 중복해서 조회된다.)
     * - Fetch Join 방식과 비교하여 쿼리 호출 수가 약간 증가하지만 DB 데이터 전송량이 감소한다.
     * - 컬렉션 Fetch Join은 페이징이 불가능하지만 이 방식은 페이징이 가능하다.
     * <p>
     * - 결론
     * - xToOne 관계는 Fetch Join해도 페이징에 영향을 주지 않는다.
     * - 따라서 xToOne 관계는 FetchJoin으로 쿼리 수를 줄여 해결하고, 나머지는 `hibernate.default_batch_fetch_size`로 최적화 한다.
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        return orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
    }

    /**
     * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
     * - 페이징 가능
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * V5. JPA에서 DTO로 바로 조회, 컬렉션 1조회 최적화 버전 (1 + 1 Query)
     * - 페이징 가능
     * - 데이터 select 양이 보다 줄어듦
     * - 쿼리 2번 조회
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query)
     * - 페이징 불가능...
     *
     * - 쿼리 한 번으로 조회 하는 것이 가능하다 정도만 확인
     *  - 쿼리는 한 번 이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되고 있어 상황에 따라 v5보다 느릴 수 있다.
     *  - API 스펙을 동일하게 가져오기 위해서 OrderFlatDto -> OrderQueryDto 어플리케이션에서 groupby로 묶어줘야 한다.
     *  - 어플리케이션에 추가 작업이 커진다.
     */
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();
        return flats.stream()
                .collect(
                        groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress())
                                , mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount())
                                        , toList())
                        )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
    }
}
