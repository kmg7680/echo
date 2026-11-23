package com.echo.moderation.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.echo.moderation.domain.BannedWord;
import com.echo.moderation.infrastructure.BannedWordRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BannedWordQueryServiceTest {

    @Mock
    BannedWordRepository bannedWordRepository;

    @InjectMocks
    BannedWordQueryService bannedWordQueryService;

    @Test
    @DisplayName("본문에 금칙어가 있으면 그 단어를 반환한다")
    void found() {
        when(bannedWordRepository.findAll())
                .thenReturn(List.of(new BannedWord("foo"), new BannedWord("spam")));

        assertEquals("spam", bannedWordQueryService.findContainedWord("Buy SPAM now").orElseThrow());
    }

    @Test
    @DisplayName("금칙어가 없거나 목록이 비었으면 empty")
    void not_found() {
        when(bannedWordRepository.findAll()).thenReturn(List.of(new BannedWord("spam")));
        assertTrue(bannedWordQueryService.findContainedWord("hello").isEmpty());

        when(bannedWordRepository.findAll()).thenReturn(List.of());
        assertTrue(bannedWordQueryService.findContainedWord("spam").isEmpty());
    }
}
