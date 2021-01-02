package kr.seok.shop.service;

import kr.seok.shop.domain.*;
import kr.seok.shop.domain.repository.ItemRepository;
import kr.seok.shop.domain.repository.MemberRepository;
import kr.seok.shop.domain.repository.OrderRepository;
import kr.seok.shop.web.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 1. 주문
 * 2. 취소
 * 3. 검색
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /**
     * 주문
     * 주문하는 회원식별자, 상품식별자, 주문 수량정보를 받아서 실제 주문 엔티티를 생성한 후 저장
     * @param memberId 주문하는 회원 식별자
     * @param itemId 상품 식별자
     * @param count 주문 수량 (몇 개 주문할 지)
     * @return 주문 엔티티 생성후 Id 값 반환
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        //엔티티 조회
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);
        //배송정보 생성
        Delivery delivery = new Delivery();
        delivery.setAddress(member.getAddress());
        delivery.setStatus(DeliveryStatus.READY);

        //주문상품 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);
        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);
        /**
         * CaseCade.All의 범위 팁
         */
        //주문 저장
        orderRepository.save(order);
        return order.getId();
    }

    /**
     * 주문 취소
     * 주문 식별자를 받아서 주문 엔티티를 조회한 후 주문 엔티티에 주문 취소를 요청
     * @param orderId 주문 식별자
     */
    @Transactional
    public void cancelOrder(Long orderId) {
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        order.cancel();
    }

    /**
     * 주문 검색
     */
    public List<Order> findOrders(OrderSearch orderSearch) {
        return orderRepository.findAll(orderSearch);
    }
}
