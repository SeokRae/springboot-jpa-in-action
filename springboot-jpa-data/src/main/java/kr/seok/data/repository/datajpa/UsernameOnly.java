package kr.seok.data.repository.datajpa;

import org.springframework.beans.factory.annotation.Value;

public interface UsernameOnly {
    /* open projection -> 엔티티의 값을 모두 가져와 값을 변형하는 것 */
    @Value("#{target.username + ' : ' + target.age}")
    String getUsername();
}
