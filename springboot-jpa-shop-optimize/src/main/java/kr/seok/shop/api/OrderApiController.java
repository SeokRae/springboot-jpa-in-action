package kr.seok.shop.api;

import kr.seok.shop.domain.Order;
import kr.seok.shop.domain.OrderItem;
import kr.seok.shop.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제
 *
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 *
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경가능) *
 *
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
     * V1. 엔티티 직접 노출
     *
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     *
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
            orderItems.stream().forEach(o -> o.getItem().getName()); //Lazy 강제초기화
        }
        return all;
    }
}
