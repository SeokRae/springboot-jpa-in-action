package kr.seok.shop.domain.repository.query;

import kr.seok.shop.domain.dto.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * 직접 쿼리 요청 관련 레포지토리
 */
@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
    private final EntityManager em;

    /* repository 재사용 불가, 이미 정해진 API의 경우에만 쿼리 -> dto 사용 */
    public List<OrderSimpleQueryDto> findOrderDtos() {
        return em.createQuery(
                "select new kr.seok.shop.domain.dto.OrderSimpleQueryDto (o.id, m.name, o.orderDate, o.status, d.address)"
                        + " from Order o"
                        + " join o.member m"
                        + " join o.delivery d",
                OrderSimpleQueryDto.class
        ).getResultList();
    }
}
