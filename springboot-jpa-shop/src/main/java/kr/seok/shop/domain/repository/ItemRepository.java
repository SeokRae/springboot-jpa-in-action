package kr.seok.shop.domain.repository;

import kr.seok.shop.domain.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {

    private final EntityManager em;

    /* merge 시 특정 값이 없으면 값을 변경하지 않는게 아니라 null로 업데이트 해버림 */
    public void save(Item item) {
        if(item.getId() == null) {
            em.persist(item);
        } else {
            em.merge(item);
        }
    }

    public Item findOne(Long id) {
        return em.find(Item.class, id);
    }

    public List<Item> findAll() {
        return em.createQuery(
                "select i from Item i", Item.class)
                .getResultList();
    }
}
