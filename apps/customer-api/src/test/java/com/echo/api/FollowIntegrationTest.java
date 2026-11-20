package com.echo.api;

import com.echo.user.application.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * POST/DELETE /follows/{targetUserId} 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FollowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private Long newUser(String name) {
        return userService.create(name + System.nanoTime(), name).getId();
    }

    @Test
    @DisplayName("POST /follows/{id} — 정상 팔로우 시 201")
    void follow_success() throws Exception {
        Long a = newUser("a");
        Long b = newUser("b");

        mockMvc.perform(post("/follows/{id}", b).header("X-User-Id", a))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.followerId").value(a))
                .andExpect(jsonPath("$.data.followeeId").value(b));
    }

    @Test
    @DisplayName("POST /follows/{id} — 없는 유저면 404")
    void follow_unknownTarget_404() throws Exception {
        Long a = newUser("a");

        mockMvc.perform(post("/follows/{id}", 999999).header("X-User-Id", a))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /follows/{id} — 이미 팔로우 중이면 409")
    void follow_duplicate_409() throws Exception {
        Long a = newUser("a");
        Long b = newUser("b");
        mockMvc.perform(post("/follows/{id}", b).header("X-User-Id", a)).andExpect(status().isCreated());

        mockMvc.perform(post("/follows/{id}", b).header("X-User-Id", a))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /follows/{id} — 자기 자신이면 400")
    void follow_self_400() throws Exception {
        Long a = newUser("a");

        mockMvc.perform(post("/follows/{id}", a).header("X-User-Id", a))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /follows/{id} — 언팔로우 후 재팔로우 가능")
    void unfollow_success() throws Exception {
        Long a = newUser("a");
        Long b = newUser("b");
        mockMvc.perform(post("/follows/{id}", b).header("X-User-Id", a)).andExpect(status().isCreated());

        mockMvc.perform(delete("/follows/{id}", b).header("X-User-Id", a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/follows/{id}", b).header("X-User-Id", a))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /follows/{id} — 없는 유저 404, 팔로우 중 아니어도 404")
    void unfollow_notFound_404() throws Exception {
        Long a = newUser("a");
        Long b = newUser("b");

        mockMvc.perform(delete("/follows/{id}", 999999).header("X-User-Id", a))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/follows/{id}", b).header("X-User-Id", a))
                .andExpect(status().isNotFound());
    }
}
