package com.echo.follow.application;

import com.echo.follow.domain.Follow;
import com.echo.follow.infrastructure.FollowRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Follow 도메인 조회 서비스. 팔로잉/팔로워 목록 조회 유스케이스.
 * 유저 존재 여부는 user 모듈 소관이라 호출자(앱 계층)가 먼저 확인한다.
 */
@Service
@Transactional(readOnly = true)
public class FollowQueryService {

    private final FollowRepository followRepository;

    public FollowQueryService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    /** userId가 팔로우하는 관계 목록(팔로잉), 최근 팔로우 순. */
    public List<Follow> getFollowing(Long userId) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId);
    }

    /** userId를 팔로우하는 관계 목록(팔로워), 최근 팔로우 순. */
    public List<Follow> getFollowers(Long userId) {
        return followRepository.findByFolloweeIdOrderByCreatedAtDesc(userId);
    }
}
