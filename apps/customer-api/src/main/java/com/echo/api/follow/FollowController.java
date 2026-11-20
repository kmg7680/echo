package com.echo.api.follow;

import com.echo.common.ApiResponse;
import com.echo.follow.application.FollowQueryService;
import com.echo.follow.application.FollowService;
import com.echo.follow.domain.AlreadyFollowingException;
import com.echo.follow.domain.Follow;
import com.echo.user.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * 팔로우 HTTP API 컨트롤러.
 * X-User-Id 헤더로 팔로워를 식별한다 (인증은 범위 외).
 *
 * <p>user와 follow 두 모듈을 잇는 조립 지점이다. 유저 존재 확인은 user 모듈에,
 * 관계 처리는 follow 모듈에 맡기며 두 모듈은 서로 직접 의존하지 않는다.
 */
@RestController
@RequestMapping("/follows")
@Tag(name = "Follow", description = "팔로우 / 언팔로우 / 팔로워 조회 API")
public class FollowController {

    private final FollowService followService;
    private final FollowQueryService followQueryService;
    private final UserService userService;

    public FollowController(
            FollowService followService, FollowQueryService followQueryService, UserService userService) {
        this.followService = followService;
        this.followQueryService = followQueryService;
        this.userService = userService;
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 조회", description = "대상 유저를 팔로우하는 관계 목록을 최근 팔로우 순으로 반환합니다. 없는 유저 404.")
    public ApiResponse<List<FollowResponse>> followers(@PathVariable Long userId) {
        requireUser(userId);
        List<FollowResponse> followers = followQueryService.getFollowers(userId).stream()
                .map(FollowResponse::from)
                .toList();
        return ApiResponse.ok(followers);
    }

    @PostMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "팔로우", description = "대상 유저를 팔로우합니다. 없는 유저 404, 이미 팔로우 중이면 409.")
    public ApiResponse<FollowResponse> follow(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long targetUserId) {
        requireUser(userId);
        requireUser(targetUserId);
        try {
            Follow follow = followService.follow(userId, targetUserId);
            return ApiResponse.ok(FollowResponse.from(follow), "팔로우했습니다.");
        } catch (AlreadyFollowingException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{targetUserId}")
    @Operation(summary = "언팔로우", description = "대상 유저를 언팔로우합니다. 없는 유저이거나 팔로우 중이 아니면 404.")
    public ApiResponse<Void> unfollow(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long targetUserId) {
        requireUser(userId);
        requireUser(targetUserId);
        if (!followService.unfollow(userId, targetUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "팔로우 중이 아닙니다. targetUserId=" + targetUserId);
        }
        return ApiResponse.ok(null, "언팔로우했습니다.");
    }

    private void requireUser(Long id) {
        if (!userService.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다. id=" + id);
        }
    }
}
