package com.echo.post.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    @DisplayName("content가 280자면 생성된다")
    void acceptsMaxLength() {
        assertDoesNotThrow(() -> new Post(1L, "a".repeat(Post.MAX_CONTENT_LENGTH)));
    }

    @Test
    @DisplayName("content가 280자를 초과하면 예외")
    void rejectsTooLong() {
        assertThrows(IllegalArgumentException.class,
                () -> new Post(1L, "a".repeat(Post.MAX_CONTENT_LENGTH + 1)));
    }

    @Test
    @DisplayName("content가 null이면 예외")
    void rejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new Post(1L, null));
    }

    @Test
    @DisplayName("content가 빈 값/공백이면 예외")
    void rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new Post(1L, ""));
        assertThrows(IllegalArgumentException.class, () -> new Post(1L, "   "));
    }
}
