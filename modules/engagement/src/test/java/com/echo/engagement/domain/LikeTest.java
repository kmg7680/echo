package com.echo.engagement.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Like 도메인 불변식 단위 테스트.
 */
class LikeTest {

    @Test
    @DisplayName("정상 생성 시 postId/userId와 createdAt이 설정된다")
    void create_success() {
        Like like = new Like(10L, 1L);

        assertEquals(10L, like.getPostId());
        assertEquals(1L, like.getUserId());
        assertNotNull(like.getCreatedAt());
    }

    @Test
    @DisplayName("postId/userId가 null이면 예외")
    void create_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> new Like(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new Like(10L, null));
    }
}
