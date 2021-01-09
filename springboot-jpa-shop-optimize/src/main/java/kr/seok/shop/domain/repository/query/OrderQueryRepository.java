package kr.seok.shop.domain.repository.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    /**
     * 컬렉션은 별도로 조회
     * Query: 루트 1번, 컬렉션 N 번
     * 단건 조회에서 많이 사용하는 방식
     *
     * 결국 N + 1 문제가 발생
     *
     */
    public List<OrderQueryDto> findOrderQueryDtos() { //루트 조회(toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();
        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행)
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            /* OrderItems 컬렉션값 채우기 */
            o.setOrderItems(orderItems);
        });
        return result;
    }

    /**
     * 1:N 관계(컬렉션)를 제외한 나머지를 한 번에 조회
     * - SQL과 같이 서브 쿼리안에 multi row가 있을 수 없음
     * - 그래서 따로 메서드로 orderItems 호출
     */
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select "
                        + "new kr.seok.shop.domain.repository.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
                        + " from Order o"
                        + " join o.member m"
                        + " join o.delivery d"
                , OrderQueryDto.class)
                .getResultList();
    }

    /**
     * 1:N 관계인 orderItems 조회
     * - orderItem > item 관계 ToOne
     */
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select "
                        + "new kr.seok.shop.domain.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                        + " from OrderItem oi"
                        + " join oi.item i"
                        + " where oi.order.id = :orderId"
                , OrderItemQueryDto.class)
                /* 주문 Id 값으로 OrderItem 리스트를 조회*/
                .setParameter("orderId", orderId)
                .getResultList();
    }

    /**
     * 최적화
     * Query: 루트 1번, 컬렉션 1번
     *
     * 데이터를 한꺼번에 처리할 때 많이 사용하는 방식
     *
     */
    public List<OrderQueryDto> findAllByDto_optimization() { //루트 조회(toOne 코드를 모두 한번에 조회)
        List<OrderQueryDto> result = findOrders();
        //orderItem 컬렉션을 MAP 한방에 조회
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));
        //루프를 돌면서 컬렉션 추가(추가 쿼리 실행X)
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    /* order의 Id 리스트로 변환하는 메서드 */
    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());
    }

    /**
     * Query -> 루트 1번, 컬렉션 1번
     *
     * xToOne 관계들을 먼저 조회하고, 여기서 얻은 식별자 orderId로 ToMany 관계인 OrderItem을 한 번에 조회
     *
     * Map을 사용해서 매칭 성능 향상 O(1)
     */
    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new kr.seok.shop.domain.repository.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                        + " from OrderItem oi"
                        + " join oi.item i"
                        /* In 절로 아이디를 가지고 한 번에 조회 */
                        + " where oi.order.id in :orderIds"
                , OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        /* List<> -> Map<Long, List<?>> */
        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }

    /**
     * 전체 조인 쿼리를 작성하여 호출
     */
    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
            "select new kr.seok.shop.domain.repository.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"
                + " from Order o "
                + " join o.member m"
                + " join o.delivery d"
                + " join o.orderItems oi"
                + " join oi.item i"
                , OrderFlatDto.class
        ).getResultList();
    }
}
