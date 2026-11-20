package com.echo.follow.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.echo.follow.domain.AlreadyFollowingException;
import com.echo.follow.domain.Follow;
import com.echo.follow.infrastructure.FollowRepository;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * FollowService 단위 테스트 — Spring 컨텍스트·Mockito 없이 인메모리 가짜 리포지토리로 검증.
 */
class FollowServiceTest {

    private List<Follow> store;
    private FollowService followService;

    @BeforeEach
    void setUp() {
        store = new ArrayList<>();
        followService = new FollowService(inMemoryRepository(store));
    }

    @Test
    @DisplayName("팔로우하면 관계가 저장되고 follower/followee가 설정된다")
    void follow_success() {
        Follow follow = followService.follow(1L, 2L);

        assertEquals(1L, follow.getFollowerId());
        assertEquals(2L, follow.getFolloweeId());
        assertEquals(1, store.size());
    }

    @Test
    @DisplayName("이미 팔로우 중이면 AlreadyFollowingException을 던지고 저장하지 않는다")
    void follow_duplicate_rejected() {
        followService.follow(1L, 2L);

        assertThrows(AlreadyFollowingException.class, () -> followService.follow(1L, 2L));
        assertEquals(1, store.size());
    }

    @Test
    @DisplayName("팔로우는 방향이 있어 맞팔(역방향)은 별개 관계로 허용한다")
    void follow_reverseDirection_allowed() {
        followService.follow(1L, 2L);

        followService.follow(2L, 1L);

        assertEquals(2, store.size());
    }

    @Test
    @DisplayName("자기 자신 팔로우는 거부하고 저장하지 않는다")
    void follow_self_rejected() {
        assertThrows(IllegalArgumentException.class, () -> followService.follow(1L, 1L));
        assertTrue(store.isEmpty());
    }

    @Test
    @DisplayName("언팔로우하면 관계를 삭제하고 true를 반환한다")
    void unfollow_existing_returnsTrue() {
        followService.follow(1L, 2L);

        assertTrue(followService.unfollow(1L, 2L));
        assertTrue(store.isEmpty());
    }

    @Test
    @DisplayName("팔로우 관계가 없으면 false를 반환하고 다른 관계는 건드리지 않는다")
    void unfollow_missing_returnsFalse() {
        followService.follow(1L, 3L);

        assertFalse(followService.unfollow(1L, 2L));
        assertEquals(1, store.size());
    }

    /** FollowService가 쓰는 4개 메서드만 인메모리 리스트로 구현한 가짜 리포지토리. */
    private static FollowRepository inMemoryRepository(List<Follow> store) {
        return (FollowRepository) Proxy.newProxyInstance(
                FollowRepository.class.getClassLoader(),
                new Class<?>[] {FollowRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "save" -> {
                        store.add((Follow) args[0]);
                        yield args[0];
                    }
                    case "delete" -> store.remove((Follow) args[0]);
                    case "existsByFollowerIdAndFolloweeId" -> find(store, (Long) args[0], (Long) args[1]).isPresent();
                    case "findByFollowerIdAndFolloweeId" -> find(store, (Long) args[0], (Long) args[1]);
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private static Optional<Follow> find(List<Follow> store, Long followerId, Long followeeId) {
        return store.stream()
                .filter(f -> f.getFollowerId().equals(followerId) && f.getFolloweeId().equals(followeeId))
                .findFirst();
    }
}
