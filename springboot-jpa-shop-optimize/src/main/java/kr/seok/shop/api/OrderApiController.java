package kr.seok.shop.api;

import kr.seok.shop.domain.Address;
import kr.seok.shop.domain.Order;
import kr.seok.shop.domain.OrderItem;
import kr.seok.shop.domain.OrderStatus;
import kr.seok.shop.domain.repository.OrderRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 *
 * V4.JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1+NQuery) * - 페이징 가능
 *
 * V5.JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1+1Query) * - 페이징 가능
 *  V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능...
 *
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    /**
     *
     * V1. 엔티티 직접 노출
     *
     * - 엔티티가 변하면 API 스펙이 변한다.
     * - 트랜잭션 안에서 지연 로딩 필요
     * - 양방향 연관관계 문제
     *
     * 작성 방식
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생할 가능성이 있기 때문에 양방향 걸린 Entity 찾아서 @JsonIgnore 설정 필요
     *
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
     *
     *
     * 문제점
     * - 지연로딩으로 너무 많은 SQL 실행
     * - SQL 실행 횟수
     *  - order 1 > member, address N번 > orderItem N번 > item N번
     *
     * 참고
     *  - 지연 로딩은 영속성 컨텍스트에 있으면 영속성 컨텍스트에 있는 엔티티를 사용하고 없으면 SQL를 실행
     *  - 같은 영속성 컨텍스트에서 이미 로딩한 회원 엔티티를 추가로 조회 시 SQL를 실행하지 않음
     *  - 하지만 이에 대한 효과는 미미, 개발은 항상 최악의 상황을 고려하여 계산해야 함
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
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
     *
     * 엔티티를 DTO로 변롼 - Fetch 조인 최적롸
     *
     * 효과
     *  - Fetch Join으로 SQL이 1번만 실행됨
     *  - distinct를 사용하면 1:N 조인으로 인하여 db row가 증가되게 되는데 이는 order 엔티티의 조회 수를 증가시킨다. (중복 데이터가 생겨버리는 문제)
     *  - JPA의 distinct는 SQL에 distinct를 추가하면 DB 호출에서는 걸러지지 않으나 JPA 쪽으로 넘어와서 걸러진다.
     *  - 이 결과로 결론적으로는 distinct 효과를 볼 수 있게 됨
     *
     * 문제점
     *  - 페이징이 안되는 심각한 문제
     *  - 하이버네이트는 모든 데이터를 DB에서 읽어온뒤 메모리에서 페이징을 하는데 이는 심각한 문제를 발생시킬 수 있다.
     *
     * 참고
     *  - 컬렉션 페치 조인은 1개만 사용할 수 있다. -> 이 말은 1:N 의 경우에만 사용해야함, 1:N:N 의 상황에서 사용하면 안됨
     *  - 컬렉션 둘 이상에 Fetch Join을 하는 경우 데이터의 정합성이 깨지게 된다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }
}
