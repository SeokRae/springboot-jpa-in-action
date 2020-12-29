package kr.seok.shop.domain.repository;

import kr.seok.shop.domain.Book;
import kr.seok.shop.domain.Item;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @Transactional
    public void 상품_등록_테스트() {
        // given
        Item item = new Book();
        item.setName("책1");
        item.setPrice(1000);
        item.setStockQuantity(2000);

        itemRepository.save(item);
        List<Item> itemList = itemRepository.findAll();
        Item findItem = itemList.get(0);
        assertThat(findItem.getName()).isEqualTo("책1");
        assertThat(findItem.getPrice()).isEqualTo(1000);
        assertThat(findItem.getStockQuantity()).isEqualTo(2000);

    }
}
