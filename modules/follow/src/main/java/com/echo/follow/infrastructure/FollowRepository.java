package com.echo.follow.infrastructure;

import com.echo.follow.domain.Follow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Follow JPA 리포지토리. infrastructure 계층.
 */
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    /** followerId가 팔로우하는 관계 목록 (최근 팔로우 순). */
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId);

    /** followeeId를 팔로우하는 관계 목록 (최근 팔로우 순). */
    List<Follow> findByFolloweeIdOrderByCreatedAtDesc(Long followeeId);
}
