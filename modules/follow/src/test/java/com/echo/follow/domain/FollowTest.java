package com.echo.follow.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FollowTest {

    @Test
    @DisplayName("정상 생성 시 follower/followee가 설정된다")
    void create_success() {
        Follow follow = new Follow(1L, 2L);
        assertEquals(1L, follow.getFollowerId());
        assertEquals(2L, follow.getFolloweeId());
    }

    @Test
    @DisplayName("자기 자신 팔로우는 거부한다")
    void create_self_rejected() {
        assertThrows(IllegalArgumentException.class, () -> new Follow(1L, 1L));
    }
}
