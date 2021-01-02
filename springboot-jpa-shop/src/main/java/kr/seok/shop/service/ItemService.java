package kr.seok.shop.service;

import kr.seok.shop.domain.Item;
import kr.seok.shop.domain.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 상품 레포지토리에게 단순하게 위임하는 클래스
 */
@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }

    /**
     * 영속성 컨텍스트가 자동 변경
     * 준영속 엔티티가 1차 캐시에서 불러온 뒤에 값을 세팅한 뒤에 트랜잭션이 커밋되기 전 flush되면서 값이 변경됨
     */
    @Transactional
    public void updateItem(Long id, String name, int price, int quantity) {
        /* 준영속 엔티티를 데이터 변경하는 방법 > 1차 캐시에서 엔티티를 불러와서 값을 세팅하기만 하면 적용 */
        Item item = itemRepository.findOne(id);
        item.setName(name);
        item.setPrice(price);
        item.setStockQuantity(quantity);
        log.info("변경된 아이템 : " + item);
    }
}
