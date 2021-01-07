package kr.seok.shop.domain.repository.query;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

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
     * SQL과 같이 서브 쿼리안에 multi row가 있을 수 없음
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
     * orderItem > item 관계 ToOne
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

}
