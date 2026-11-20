package com.echo.follow.application;

import com.echo.follow.domain.AlreadyFollowingException;
import com.echo.follow.domain.Follow;
import com.echo.follow.infrastructure.FollowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Follow 도메인 애플리케이션 서비스. 팔로우·언팔로우 유스케이스.
 * 유저 존재 여부는 user 모듈 소관이라 호출자(앱 계층)가 먼저 확인한다.
 */
@Service
@Transactional(readOnly = true)
public class FollowService {

    private final FollowRepository followRepository;

    public FollowService(FollowRepository followRepository) {
        this.followRepository = followRepository;
    }

    /** @throws AlreadyFollowingException 이미 팔로우 중인 경우 */
    @Transactional
    public Follow follow(Long followerId, Long followeeId) {
        Follow follow = new Follow(followerId, followeeId);
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new AlreadyFollowingException(followerId, followeeId);
        }
        return followRepository.save(follow);
    }

    /** 언팔로우. 팔로우 관계가 있었으면 true, 없었으면 false(404 판단은 호출자 몫). */
    @Transactional
    public boolean unfollow(Long followerId, Long followeeId) {
        return followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .map(f -> {
                    followRepository.delete(f);
                    return true;
                })
                .orElse(false);
    }
}
