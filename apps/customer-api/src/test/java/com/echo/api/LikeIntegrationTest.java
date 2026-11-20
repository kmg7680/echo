package com.echo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.echo.post.application.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 API 통합 테스트. engagement + post 모듈 조립과 HTTP 상태 매핑을 검증한다.
 * 각 테스트는 @Transactional로 글/좋아요 데이터를 롤백해 서로 간섭하지 않는다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LikeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostService postService;

    private Long postId;

    @BeforeEach
    void setUp() {
        postId = postService.create(2L, "좋아요 테스트용 글").getId();
    }

    @Test
    @DisplayName("POST /posts/{id}/likes — 정상 좋아요 시 201")
    void like_success() throws Exception {
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    @DisplayName("POST /posts/{id}/likes — 같은 사용자가 중복 좋아요하면 409")
    void like_duplicate_409() throws Exception {
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /posts/{id}/likes — 없는 게시물이면 404")
    void like_unknownPost_404() throws Exception {
        mockMvc.perform(post("/posts/{postId}/likes", 999999).header("X-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /posts/{id}/likes — 좋아요 취소 시 200")
    void unlike_success() throws Exception {
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/posts/{postId}/likes/count", postId))
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test
    @DisplayName("DELETE /posts/{id}/likes — 좋아요 상태가 아니어도 200(멱등)")
    void unlike_idempotent_200() throws Exception {
        mockMvc.perform(delete("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /posts/{id}/likes — 없는 게시물이면 404")
    void unlike_unknownPost_404() throws Exception {
        mockMvc.perform(delete("/posts/{postId}/likes", 999999).header("X-User-Id", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /posts/{id}/likes/count — 사용자별 좋아요를 합산해 반환한다")
    void count_success() throws Exception {
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "1"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/posts/{postId}/likes", postId).header("X-User-Id", "3"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/posts/{postId}/likes/count", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.count").value(2));
    }

    @Test
    @DisplayName("GET /posts/{id}/likes/count — 좋아요가 없으면 0")
    void count_zero() throws Exception {
        mockMvc.perform(get("/posts/{postId}/likes/count", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(0));
    }

    @Test
    @DisplayName("GET /posts/{id}/likes/count — 없는 게시물이면 404")
    void count_unknownPost_404() throws Exception {
        mockMvc.perform(get("/posts/{postId}/likes/count", 999999))
                .andExpect(status().isNotFound());
    }
}
