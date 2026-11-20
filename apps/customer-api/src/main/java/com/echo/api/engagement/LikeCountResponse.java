package com.echo.api.engagement;

/**
 * 좋아요 수 응답 DTO.
 */
public record LikeCountResponse(Long postId, long count) {
}
