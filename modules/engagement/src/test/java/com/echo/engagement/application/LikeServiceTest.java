package com.echo.engagement.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.echo.engagement.domain.AlreadyLikedException;
import com.echo.engagement.domain.Like;
import com.echo.engagement.infrastructure.LikeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * LikeService 단위 테스트 — Spring 컨텍스트 없이 리포지토리를 mock으로 검증.
 */
@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @InjectMocks
    private LikeService likeService;

    @Test
    @DisplayName("좋아요하지 않았다면 새 Like를 저장하고 반환한다")
    void like_success_saves() {
        when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenAnswer(inv -> inv.getArgument(0));

        Like result = likeService.like(10L, 1L);

        assertEquals(10L, result.getPostId());
        assertEquals(1L, result.getUserId());
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("이미 좋아요했다면 AlreadyLikedException을 던지고 저장하지 않는다")
    void like_duplicate_throws() {
        when(likeRepository.existsByPostIdAndUserId(10L, 1L)).thenReturn(true);

        assertThrows(AlreadyLikedException.class, () -> likeService.like(10L, 1L));

        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("좋아요 취소는 리포지토리 삭제에 위임한다(멱등)")
    void unlike_delegatesToRepository() {
        likeService.unlike(10L, 1L);

        verify(likeRepository).deleteByPostIdAndUserId(10L, 1L);
    }

    @Test
    @DisplayName("좋아요 수는 리포지토리 count에 위임한다")
    void countLikes_delegatesToRepository() {
        when(likeRepository.countByPostId(10L)).thenReturn(3L);

        assertEquals(3L, likeService.countLikes(10L));
    }
}
