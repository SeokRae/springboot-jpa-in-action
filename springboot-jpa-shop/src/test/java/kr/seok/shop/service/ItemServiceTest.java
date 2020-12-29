package kr.seok.shop.service;

import kr.seok.shop.domain.Book;
import kr.seok.shop.domain.Item;
import kr.seok.shop.domain.repository.ItemRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
/* 스프링 부트 띄우고 테스트(이게 없으면 @Autowired 다 실패) */
@SpringBootTest
/* 반복 가능한 테스트 지원, 각각의 테스트를 실행할 때마다 트랜잭션을 시작하고 테스트 */
@Transactional
public class ItemServiceTest {

    @Autowired
    ItemService itemService;
    @Autowired
    ItemRepository itemRepository;

    /* 상품 등록 테스트 */
    @Test
    public void 상품등록() {
        Item saveItem = new Book();
        saveItem.setName("책1");
        saveItem.setPrice(1000);
        saveItem.setStockQuantity(2000);

        itemRepository.save(saveItem);

        assertEquals(saveItem, itemRepository.findAll().get(0));
    }

}
