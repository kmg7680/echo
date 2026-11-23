package com.echo.moderation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BannedWordTest {

    @Test
    @DisplayName("공백을 제거하고 소문자로 정규화한다")
    void normalizes() {
        assertEquals("spam", new BannedWord("  SPAM ").getWord());
    }

    @Test
    @DisplayName("빈 금칙어는 예외")
    void blank_throws() {
        assertThrows(IllegalArgumentException.class, () -> new BannedWord(" "));
        assertThrows(IllegalArgumentException.class, () -> new BannedWord(null));
    }

    @Test
    @DisplayName("대소문자 무시하고 부분 일치를 판단한다")
    void contained_in() {
        BannedWord word = new BannedWord("spam");

        assertTrue(word.isContainedIn("this is SPAM!"));
        assertFalse(word.isContainedIn("clean text"));
        assertFalse(word.isContainedIn(null));
    }
}
