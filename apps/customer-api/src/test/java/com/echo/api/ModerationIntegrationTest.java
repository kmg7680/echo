package com.echo.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.echo.moderation.domain.BannedWord;
import com.echo.moderation.infrastructure.BannedWordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * moderation API 통합 테스트 — 금칙어 필터(DB 관리)와 신고 접수/처리/조회를 검증한다.
 * @Transactional로 테스트 데이터를 롤백해 서로 간섭하지 않는다.
 */
@Transactional
class ModerationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BannedWordRepository bannedWordRepository;

    private long createPost(String content) throws Exception {
        String body = mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("content", content))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private long submitReport(long postId) throws Exception {
        String body = mockMvc.perform(post("/reports")
                .header("X-User-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("postId", postId, "reason", "스팸"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("id").asLong();
    }

    private void changeStatus(long reportId, String status) throws Exception {
        mockMvc.perform(patch("/reports/{id}/status", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"" + status + "\"}"));
    }

    @Test
    @DisplayName("POST /posts — DB에 등록된 금칙어가 포함되면 400")
    void post_with_banned_word_400() throws Exception {
        bannedWordRepository.save(new BannedWord("badword"));

        mockMvc.perform(post("/posts")
                .header("X-User-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"this has a BadWord inside\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /posts — 금칙어가 없으면 정상 등록 201")
    void post_without_banned_word_201() throws Exception {
        bannedWordRepository.save(new BannedWord("badword"));

        createPost("clean content");
    }

    @Test
    @DisplayName("POST /reports — 접수하면 201, PENDING")
    void submit_report() throws Exception {
        long postId = createPost("신고 대상");

        mockMvc.perform(post("/reports")
                .header("X-User-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("postId", postId, "reason", "스팸"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.postId").value(postId))
                .andExpect(jsonPath("$.data.reporterId").value(2))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /reports — 없는 게시물이면 404, 사유가 비면 400")
    void submit_report_errors() throws Exception {
        mockMvc.perform(post("/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"postId\":999999,\"reason\":\"스팸\"}"))
                .andExpect(status().isNotFound());

        long postId = createPost("신고 대상");
        mockMvc.perform(post("/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"postId\":" + postId + ",\"reason\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /reports/{id}/status — PENDING→REVIEWED→RESOLVED 전환")
    void change_status_flow() throws Exception {
        long reportId = submitReport(createPost("신고 대상"));

        mockMvc.perform(patch("/reports/{id}/status", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"REVIEWED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("REVIEWED"));

        mockMvc.perform(patch("/reports/{id}/status", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    @DisplayName("PATCH — 단계를 건너뛰거나 역행하면 409")
    void change_status_invalid_409() throws Exception {
        long reportId = submitReport(createPost("신고 대상"));

        mockMvc.perform(patch("/reports/{id}/status", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"RESOLVED\"}"))
                .andExpect(status().isConflict());

        changeStatus(reportId, "REVIEWED");
        mockMvc.perform(patch("/reports/{id}/status", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PENDING\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH — 없는 신고 404, 알 수 없는 상태값/누락은 400")
    void change_status_404_400() throws Exception {
        mockMvc.perform(patch("/reports/{id}/status", 999999)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"REVIEWED\"}"))
                .andExpect(status().isNotFound());

        long reportId = submitReport(createPost("신고 대상"));
        mockMvc.perform(patch("/reports/{id}/status", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"BOGUS\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/reports/{id}/status", reportId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /reports/{id} — 조회 200, 없으면 404")
    void get_report() throws Exception {
        long reportId = submitReport(createPost("신고 대상"));

        mockMvc.perform(get("/reports/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(reportId))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        mockMvc.perform(get("/reports/{id}", 999999))
                .andExpect(status().isNotFound());
    }
}
