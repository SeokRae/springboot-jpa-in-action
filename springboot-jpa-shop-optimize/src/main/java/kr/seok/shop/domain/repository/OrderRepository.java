package kr.seok.shop.domain.repository;


import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.seok.shop.domain.*;
import kr.seok.shop.domain.Order;
import kr.seok.shop.domain.repository.query.OrderQueryDto;
import kr.seok.shop.web.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

//    public List<Order> findAll(OrderSearch orderSearch) {
//        return findAllByString(orderSearch);
//    }
    public List<Order> findAll() {
        return em.createQuery(
                "select o From o", Order.class)
                .getResultList();
    }
    /**
     * JPQL 쿼리를 문자로 생성하기는 번거롭고, 실수로 인한 버그가 충분히 발생할 수 있다
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.member m";
        boolean isFirstCondition = true;

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class) .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     * JPA Criteria는 JPA 표준 스펙이지만 실무에서 사용하기에 너무 복잡하다
     * @param orderSearch
     * @return
     */
    public List<Order> findAllByCriteria(OrderSearch orderSearch) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);

        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();

        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);

        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대 1000건
        return query.getResultList();
    }

    /* 성능 최적화가 왠만하면 여기서 해결 됨 */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o"
                        + " join fetch o.member m"
                        + " join fetch o.delivery d", Order.class)
                .getResultList();
    }

    /**
     * 주문 조회 v3 fetch join
     * - 쿼리 1번에 모두 조회
     * - SQL로 보면 중복데이터를 일단 다 조회 해버림, 때문에 데이터베이스 데이터 전송량을 보면 최적화라고 할 수가 없음
     * - 페이징이 안됨
     */
    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select distinct o from Order o"
                        + " join fetch o.member m"
                        + " join fetch o.delivery d"
                        + " join fetch o.orderItems oi"
                        + " join fetch oi.item i"
                , Order.class)
                .getResultList();
    }

    /**
     * 주문 조회 v3.1 Fetch Join 및 페이징
     * - 쿼리 3번으로 1:1:1 조회, 대신 SQL IN Query를 통해 필요한 데이터만 조회
     * - 필요한 데이터만 조회되기 때문에 v3보다 데이터 전송량에서 최적화 되었다고 할 수 있음
     * - 페이징이 가능
     */
    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery(
                "select o from Order o"
                        + " join fetch o.member m"
                        + " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * QueryDSL로 작성한 내용이 JPQL로 반환하여 쿼리를 날림
     */
    public List<Order> findAll(OrderSearch orderSearch) {
        JPAQueryFactory query = new JPAQueryFactory(em);
        QOrder order = QOrder.order;
        QMember member = QMember.member;
        return query
                .select(order)
                .from(order)
                .join(order.member, member)
                .where(
                        statusEq(orderSearch.getOrderStatus()),
                        nameLike(orderSearch.getMemberName()))
                .limit(1000)
                .fetch();
    }

    /**
     * 상태조회 동적쿼리
     */
    private BooleanExpression statusEq(OrderStatus statusCond) {
        if (statusCond == null) {
            return null;
        }
        return QOrder.order.status.eq(statusCond);
    }

    /**
     * like 동적쿼리
     */
    private BooleanExpression nameLike(String nameCond) {
        if (!StringUtils.hasText(nameCond)) {
            return null;
        }
        return QMember.member.name.like(nameCond);
    }
}
